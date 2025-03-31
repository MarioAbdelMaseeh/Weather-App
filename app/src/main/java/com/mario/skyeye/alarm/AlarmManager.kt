package com.mario.skyeye.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.mario.skyeye.data.models.Alarm

fun setManualAlarm(context: Context, triggerTime: Long, alarmId: Int):Boolean {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("alarmId", alarmId)
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
            Toast.makeText(context, "Alarm Set!", Toast.LENGTH_SHORT).show()
            return true
        } else {
            // Redirect user to settings to allow exact alarms
            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(settingsIntent)
            Toast.makeText(context, "Enable exact alarms in settings", Toast.LENGTH_LONG).show()
            return false
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        Toast.makeText(context, "Alarm Set!", Toast.LENGTH_SHORT).show()
        return true
    }
}
fun cancelAlarm(context: Context, alarm: Alarm){
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.createdAt.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}