package com.mario.skyeye.workmanager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mario.skyeye.MainActivity
import com.mario.skyeye.R
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.data.sharedprefrence.AppPreference
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

class WeatherAlertWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    private val repo by lazy {
        RepoImpl.getInstance(
            RemoteDataSourceImpl(RetrofitHelper.service),
            LocalDataSourceImpl(AppDataBase.getInstance(context).weatherDao(),AppDataBase.getInstance(context).alarmDao()),
            AppPreference(context))}

    override suspend fun doWork(): Result {
        return try {
            val lat = inputData.getDouble("LATITUDE", 0.0)
            val lon = inputData.getDouble("LONGITUDE", 0.0)
            val unit = inputData.getString("UNIT") ?: "metric"
            val condition = inputData.getString("CONDITION") ?: "Rain"

            if (checkWeatherCondition(lat, lon, unit, condition)) {
                showAlertNotification()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherAlertWorker", "Error in doWork", e)
            Result.failure()
        }
    }

    private fun showAlertNotification() {
        // 1. Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("WeatherAlertWorker", "Notification permission not granted")
                return
            }
        }

        // 2. Create notification channel with sound
        createNotificationChannel()

        // 3. Create intent for when notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            Intent.setFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Use default alarm sound (more reliable than custom sounds)
//        val soundUri =
//
//            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // 5. Build notification with sound
        val notification = NotificationCompat.Builder(applicationContext, "weather_alerts")
            .setContentTitle("Weather Alert!")
            .setContentText("Severe weather detected - tap to view")
            .setSmallIcon(R.drawable.snow_day)
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX is critical for sound
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            //.setSound(soundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setLights(Color.RED, 1000, 1000)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // 6. Show notification
        NotificationManagerCompat.from(applicationContext)
            .notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = try {
                // Try custom sound first
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${applicationContext.packageName}/${R.raw.rain}".toUri()
            } catch (e: Exception) {
                // Fallback to system default alarm sound
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            val channel = NotificationChannel(
                "weather_alerts", // Same ID used in Builder
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH // HIGH or MAX for sound
            ).apply {
                description = "Emergency weather alerts"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM) // Critical for sound
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }
    private suspend fun checkWeatherCondition(lat: Double, lon: Double, unit: String, condition: String): Boolean {
        return repo.getCurrentWeather(lat, lon, unit)
            ?.firstOrNull()
            ?.weather
            ?.firstOrNull()
            ?.description.equals(condition, ignoreCase = true)
    }

    companion object {
        private const val ALERT_NOTIFICATION_ID = 1002

        fun schedule(context: Context, lat: Double, lon: Double, unit: String, condition: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(constraints)
                .setInputData(workDataOf(
                    "LATITUDE" to lat,
                    "LONGITUDE" to lon,
                    "UNIT" to unit,
                    "CONDITION" to condition
                ))
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                condition,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        fun cancel(context: Context, condition: String) {
            WorkManager.getInstance(context).cancelUniqueWork(condition)
        }
    }
}
