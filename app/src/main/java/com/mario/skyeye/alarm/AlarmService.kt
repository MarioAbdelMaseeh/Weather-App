package com.mario.skyeye.alarm

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

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "AlarmService created")
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
                stopAlarm()
                return START_NOT_STICKY
            }
            ACTION_START_ALARM -> {
                if (!isRunning) {
                    startAlarm()
                }
                return START_STICKY
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

    private fun startAlarm() {
        try {
            showNotification()
            mediaPlayer?.start()
            isRunning = true
            Log.d("AlarmService", "Alarm started successfully")
        } catch (e: Exception) {
            Log.e("AlarmService", "Error starting alarm", e)
            stopSelf()
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
            isRunning = false
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

    private fun showNotification() {
        createNotificationChannel(
            context = this,
            channelId = CHANNEL_ID,
            channelName = "Weather Alerts"
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather Alert!")
            .setContentText("Severe weather detected - take action!")
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

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_ALARM = "START_ALARM"
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "weather_alert_channel"

        fun start(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_ALARM
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
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