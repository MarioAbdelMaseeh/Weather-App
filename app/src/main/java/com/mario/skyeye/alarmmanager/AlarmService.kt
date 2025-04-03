package com.mario.skyeye.alarmmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mario.skyeye.MainActivity
import com.mario.skyeye.R
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.data.sharedprefrence.AppPreference
import com.mario.skyeye.utils.getWeatherNotification
import com.mario.skyeye.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isRunning = false
    private lateinit var repo: RepoImpl
    var alarmId by Delegates.notNull<Int>()


    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "AlarmService created")
        repo = RepoImpl.getInstance(
            RemoteDataSourceImpl(RetrofitHelper.service),
            LocalDataSourceImpl(
                AppDataBase.getInstance(applicationContext).weatherDao(),
                AppDataBase.getInstance(applicationContext).alarmDao()
            ),
            AppPreference(applicationContext)
        )
        mediaPlayer = MediaPlayer.create(this, R.raw.rain).apply {
            isLooping = true
            setOnErrorListener { _, what, extra ->
                Log.e("AlarmService", "MediaPlayer error: $what, $extra")
                stopSelf()
                true
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                stopAlarm(alarmId)
                return START_NOT_STICKY
            }
            ACTION_START_ALARM -> {
                if (!isRunning) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val lat = intent.getDoubleExtra("LATITUDE", 0.0)
                        val lon = intent.getDoubleExtra("LONGITUDE", 0.0)
                        val unit = intent.getStringExtra("UNIT") ?: "metric"
                        alarmId = intent.getIntExtra("alarmId", 0)
                        var weather: CurrentWeatherResponse? = null
                        if (isInternetAvailable(this@AlarmService)){
                            weather = repo.getCurrentWeather(lat, lon, unit)?.firstOrNull()
                        }
                        Log.i("AlarmService", "Weather fetched: ${weather?.name}")
                        withContext(Dispatchers.Main) {
                            startAlarm(weather?.weather?.get(0)?.icon?.getWeatherNotification() ?: "01d".getWeatherNotification())
                        }
                    }
                }
                return START_STICKY
            }
            ACTION_SNOOZE_ALARM -> {
                stopAlarm(alarmId)
                setManualAlarm(this, System.currentTimeMillis() + 5 * 60 * 1000, alarmId,
                    intent.getDoubleExtra("LATITUDE", 0.0),
                    intent.getDoubleExtra("LONGITUDE", 0.0)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    repo.insertAlarm(
                        Alarm(
                            triggerTime = System.currentTimeMillis() + 5 * 60 * 1000,
                            isEnabled = true,
                            createdAt = alarmId.toInt()
                        )
                    )
                    Log.d("AlarmService", "Inserting snoozed alarm with createdAt: $alarmId")
                }

                return START_NOT_STICKY
            }
            else -> {
                // Default case for legacy starts
                if (!isRunning) {
                    startAlarm()
                }
                return START_STICKY
            }
        }
    }

    private fun startAlarm(message: String = "Severe weather detected - take action!") {
        try {
            showNotification(message)
            mediaPlayer?.start()
            isRunning = true
            Log.d("AlarmService", "Alarm started successfully")
        } catch (e: Exception) {
            Log.e("AlarmService", "Error starting alarm", e)
            stopSelf()
        }
    }

    private fun stopAlarm(alarmId: Int) {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
            isRunning = false
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("AlarmService", "Deleting alarm with createdAt: $alarmId")
                repo.deleteAlarmByCreatedAt(alarmId.toLong())
            }
            stopForeground(true)
            stopSelf()
            Log.d("AlarmService", "Alarm stopped successfully")
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping alarm", e)
            // Ensure cleanup even if error occurs
            mediaPlayer?.release()
            mediaPlayer = null
            stopForeground(true)
            stopSelf()
        }
    }

    private fun showNotification(message: String) {
        createNotificationChannel(
            context = this,
            channelId = CHANNEL_ID,
            channelName = "Weather Alerts"
        )
        Log.i("AlarmService", "Showing notification with message: $message")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather Alert!")
            .setContentText(message)
            .setSmallIcon(R.drawable.base_sun)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(createOpenAppIntent())
            .addAction(
                R.drawable.base_sun,
                "Stop Alarm",
                createStopIntent()
            )
            .addAction(
                R.drawable.base_sun,
                "Snooze",
                createSnoozeIntent()
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopIntent(): PendingIntent {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(): PendingIntent{
        val intent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
//        stopAlarm(alarmId)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "SNOOZE_ALARM"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "weather_alert_channel"


    }
}

// Helper extension function
fun createNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String,
    importance: Int = NotificationManager.IMPORTANCE_HIGH
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        ).apply {
            description = "Channel for $channelName"
            setSound(null, null)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}