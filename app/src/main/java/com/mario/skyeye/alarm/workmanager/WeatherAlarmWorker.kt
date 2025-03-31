package com.mario.skyeye.alarm.workmanager

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
                //RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

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
                "WeatherAlertWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("WeatherAlertWorker")
        }
    }
}
//class WeatherAlertWorker(
//    context: Context,
//    workerParams: WorkerParameters,
//) : CoroutineWorker(context, workerParams) {
//
//    private val repo by lazy {
//        RepoImpl.getInstance(
//            RemoteDataSourceImpl(RetrofitHelper.service),
//            LocalDataSourceImpl(AppDataBase.getInstance(context).weatherDao()),
//            AppPreference(context)
//        )
//    }
//
//    override suspend fun doWork(): Result {
//        return try {
//            // Set foreground notification immediately
////            setForeground(createForegroundInfo())
//
//            val lat = inputData.getDouble("LATITUDE", 0.0)
//            val lon = inputData.getDouble("LONGITUDE", 0.0)
//            val unit = inputData.getString("UNIT") ?: "metric"
//            val condition = inputData.getString("CONDITION") ?: "Rain"
//
//            Log.i("WeatherAlertWorker", "Checking weather for $condition at ($lat, $lon)")
//            if (checkWeatherCondition(lat, lon, unit, condition)) {
//                showUserNotification()
//            }
//
//            Result.success()
//        } catch (e: Exception) {
//            Log.e("WeatherAlertWorker", "Error in doWork", e)
//            Result.failure()
//        }
//    }
//
//    private suspend fun checkWeatherCondition(lat: Double, lon: Double, unit: String, condition: String): Boolean {
//        delay(5000)
//        return repo.getCurrentWeather(lat, lon, unit)
//            ?.firstOrNull()
//            ?.weather
//            ?.firstOrNull()
//            ?.description.equals(condition, ignoreCase = true)
//    }
//
//    private fun triggerAlarm() {
//        val intent = Intent(applicationContext, AlarmService::class.java).apply {
//            action = "START_ALARM"
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            applicationContext.startForegroundService(intent)
//        } else {
//            applicationContext.startService(intent)
//        }
//        Log.i("WeatherAlertWorker", "After triggering alarm service")
//    }
//
////    private fun createForegroundInfo(): ForegroundInfo {
////        return ForegroundInfo(
////            NOTIFICATION_ID,
////            createWorkerNotification(),
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
////                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
////            } else {
////                0
////            }
////        )
////    }
//
////    private fun createWorkerNotification(): Notification {
////        createNotificationChannel(
////            context = applicationContext,
////            channelId = WORKER_CHANNEL_ID,
////            channelName = "Weather Monitoring"
////        )
////
////        return NotificationCompat.Builder(applicationContext, WORKER_CHANNEL_ID)
////            .setContentTitle("Weather Monitoring Active")
////            .setContentText("Checking for weather alerts")
////            .setSmallIcon(R.drawable.base_sun)
////            .setPriority(NotificationCompat.PRIORITY_LOW)
////            .setOngoing(true)
////            .build()
////    }
//
//    private fun showUserNotification() {
//        // Check notification permission first
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    applicationContext,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.w("WeatherAlertWorker", "Notification permission not granted")
//                return
//            }
//        }
//
//        val channelId = "weather_alerts_channel"
//        createNotificationChannel(
//            channelId,
//            "Weather Alerts",
//            NotificationManager.IMPORTANCE_HIGH)
//
//        val intent = Intent(applicationContext, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        val soundUri =
//            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//
//        val notification = NotificationCompat.Builder(applicationContext, channelId)
//            .setContentTitle("Weather Alert!")
//            .setContentText("Severe weather detected - tap to view")
//            .setSmallIcon(R.drawable.snow_day)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setSound(soundUri)
//            .setVibrate(longArrayOf(1000, 1000, 1000, 1000)) // Vibrate pattern
//            .setLights(Color.RED, 3000, 3000) // LED light
//            .setContentIntent(PendingIntent.getActivity(
//                applicationContext,
//                0,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
//            .build()
//
//        NotificationManagerCompat.from(applicationContext)
//            .notify(ALERT_NOTIFICATION_ID, notification)
//    }
//    private fun createNotificationChannel(channelId: String, name: String, importance: Int) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val soundUri = try {
//                // Try custom sound first
////                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${applicationContext.packageName}/${R.raw.rain}".toUri()
//                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            } catch (e: Exception) {
//                // Fallback to system default alarm sound
//                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            }
//            val channel = NotificationChannel(channelId, name, importance).apply {
//                description = "Channel for $name"
//                enableLights(true)
//                lightColor = Color.RED
//                enableVibration(true)
//                setSound(
//                    soundUri,
//                    AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .build()
//                )
//            }
//
//            val manager = applicationContext.getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    companion object {
//        private const val NOTIFICATION_ID = 1001
//        private const val WORKER_CHANNEL_ID = "weather_monitor_channel"
//        private const val ALERT_NOTIFICATION_ID = 1002
//
//        fun schedule(context: Context, lat: Double, lon: Double, unit: String, condition: String) {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresBatteryNotLow(true)
//                .build()
//
//            val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
//                15, TimeUnit.MINUTES, // Minimum interval
//               // 5, TimeUnit.MINUTES // Flex interval
//            ).setConstraints(constraints)
//                .setInputData(workDataOf(
//                    "LATITUDE" to lat,
//                    "LONGITUDE" to lon,
//                    "UNIT" to unit,
//                    "CONDITION" to condition
//                ))
//                .setInitialDelay(10000, TimeUnit.MILLISECONDS)
//                .build()
//            Log.i("WeatherAlertWorker", "Scheduled weather alert worker for $condition at ($lat, $lon)")
//            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                "WeatherAlertWorker",
//                ExistingPeriodicWorkPolicy.KEEP,
//                workRequest
//            )
//        }
//
//        fun cancel(context: Context) {
//            WorkManager.getInstance(context).cancelUniqueWork("WeatherAlertWorker")
//        }
//    }
//}
////class WeatherAlertWorker(
////    context: Context,
////    workerParams: WorkerParameters,
////) : CoroutineWorker(context, workerParams) {
////
////private val repo by lazy { RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
////    LocalDataSourceImpl(AppDataBase.getInstance(context).weatherDao()), AppPreference(context)) }
////
////    @RequiresApi(Build.VERSION_CODES.O)
////    override suspend fun doWork(): Result {
////        return withContext(Dispatchers.IO) {
////            try {
////                // ✅ Run WorkManager as a Foreground Service
////                setForeground(getForegroundInfo())
////
////                val lat = inputData.getDouble("LATITUDE", 0.0)
////                val lon = inputData.getDouble("LONGITUDE", 0.0)
////                val unit = inputData.getString("UNIT") ?: "metric"
////                val condition = inputData.getString("CONDITION") ?: "Rain"
////
////                Log.i(
////                    "WeatherAlertWorker",
////                    "Checking weather condition for $condition at ($lat, $lon)"
////                )
////                val shouldTriggerAlarm = checkWeatherCondition(lat, lon, unit, condition)
////
////                if (shouldTriggerAlarm) {
////                    val intent = Intent(applicationContext, AlarmService::class.java)
////                    applicationContext.startService(intent) // ✅ Now allowed
////                }
////
////                Result.success()
////            } catch (e: Exception) {
////                Log.e("WeatherAlertWorker", "❌ Error starting service: ${e.message}", e)
////                Result.failure()
////            }
////        }
////    }
////
////    override suspend fun getForegroundInfo(): ForegroundInfo {
////        val notification = createNotification(applicationContext)
////        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
////            ForegroundInfo(2, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
////        } else {
////            ForegroundInfo(2, notification)
////        }
////    }
////
////    private fun createNotification(context: Context): Notification {
////        val channelId = "weather_alert_worker_channel"
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            val channel = NotificationChannel(
////                channelId, "Weather Alert Worker",
////                NotificationManager.IMPORTANCE_LOW
////            )
////            val manager = context.getSystemService(NotificationManager::class.java)
////            manager.createNotificationChannel(channel)
////        }
////
////        return NotificationCompat.Builder(context, channelId)
////            .setContentTitle("Weather Alert Worker")
////            .setContentText("Checking weather conditions...")
////            .setSmallIcon(R.drawable.base_sun)
////            .setPriority(NotificationCompat.PRIORITY_LOW)
////            .build()
////    }
////
////
////    private suspend fun checkWeatherCondition(lat: Double, lon: Double, unit: String, condition: String): Boolean {
//        val weather = repo.getCurrentWeather(lat, lon, unit)?.firstOrNull()
//        return (weather?.weather?.get(0)?.description == condition)
//    }
//}
////class WeatherWorkerFactory(private val repo: Repo) : WorkerFactory() {
////    override fun createWorker(
////        appContext: Context,
////        workerClassName: String,
////        workerParameters: WorkerParameters
////    ): ListenableWorker? {
////        return when (workerClassName) {
////            WeatherAlertWorker::class.java.name -> WeatherAlertWorker(appContext, workerParameters, repo)
////            else -> null
////        }
////    }
////}
//fun scheduleWeatherAlerts(context: Context, lat: Double, lon: Double, unit: String, condition: String) {
//    val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(
//        20,TimeUnit.MINUTES)
//        .setConstraints(
//            Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED) // Requires internet
//                .build()
//        ).setInputData(
//            workDataOf(
//                "LATITUDE" to lat,
//                "LONGITUDE" to lon,
//                "UNIT" to unit,
//                "CONDITION" to condition
//            )
//        )
//        .setInitialDelay(5000, TimeUnit.MILLISECONDS)
//        .build()
//
//    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//        "WeatherAlertWorker",
//        ExistingPeriodicWorkPolicy.KEEP,
//        workRequest
//    )
//    Log.i("WeatherAlertWorker", "Scheduled weather alert worker for $condition at ($lat, $lon)")
//}
//fun stopWeatherAlarm(context: Context) {
//    WorkManager.getInstance(context).cancelUniqueWork("WeatherAlertWorker")
//    Log.i("WeatherAlertWorker", "Stopped weather alert worker")
//    val intent = Intent(context, AlarmService::class.java)
//    intent.action = "STOP_ALARM" // Send action to stop the service
//    context.startService(intent) // This will trigger stopSelf() inside the service
//}