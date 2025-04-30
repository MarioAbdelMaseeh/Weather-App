package com.mario.skyeye.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.mario.skyeye.alarmmanager.AlarmService.Companion.ACTION_START_ALARM
import com.mario.skyeye.data.models.Alarm

fun setManualAlarm(context: Context, triggerTime: Long, alarmId: Int, lat: Double, lon: Double):Boolean {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("alarmId", alarmId)
        putExtra("latitude", lat)
        putExtra("longitude", lon)
        action = ACTION_START_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            return true
        } else {
            // Redirect user to settings to allow exact alarms
            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(settingsIntent)
            return false
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        return true
    }
}
fun cancelAlarm(context: Context, alarmId: Int, lat: Double, lon: Double ){
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("alarmId", alarmId)
        putExtra("latitude", lat)
        putExtra("longitude", lon)
        action = ACTION_START_ALARM
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}