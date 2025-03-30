package com.mario.skyeye.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mario.skyeye.MainActivity
import com.mario.skyeye.R

class AlarmService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.rain) // Add a sound file
        mediaPlayer.isLooping = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service started with action: ${intent?.action}")
        if (intent?.action == "STOP_ALARM") {
            stopSelf()
            return START_NOT_STICKY
        }
        // ✅ Fix: Open app when clicking notification
        val openAppIntent = Intent(this, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel(context = this)
        val notification = NotificationCompat.Builder(this, "weather_alert_channel")
            .setContentTitle("Weather Alert")
            .setContentText("It's raining! Stay safe.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.wind, "Stop", stopPendingIntent) // Stop button
            .build()
        Log.d("AlarmService", "⚡ Starting foreground service...")
        //startForeground(1, notification) // 🔹 Start foreground service BEFORE playing sound

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.activeNotifications.none { it.id == 1 }) {
                startForeground(1, notification)
            }
        }

        mediaPlayer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
fun stopAlarm(context: Context) {
    val intent = Intent(context, AlarmService::class.java)
    context.stopService(intent) // Stop the running alarm service
}
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "weather_alert_channel", // Channel ID
            "Weather Alerts", // Channel Name
            NotificationManager.IMPORTANCE_HIGH // High importance
        ).apply {
            description = "Channel for weather alerts"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel("weather_alert_channel") == null) {
            manager.createNotificationChannel(channel)
        }
    }
}
