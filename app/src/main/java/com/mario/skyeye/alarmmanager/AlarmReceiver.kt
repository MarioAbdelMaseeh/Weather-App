package com.mario.skyeye.alarmmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm triggered! Intent action: ${intent?.action}")
        // Start the foreground service to handle sound & notification
        Log.d("AlarmReceiver", "Alarm triggered!")
        val serviceIntent = Intent(context, AlarmService::class.java)
        serviceIntent.action = AlarmService.ACTION_START_ALARM
        intent?.let {
            serviceIntent.putExtra("LATITUDE", it.getDoubleExtra("latitude", 0.0))
            serviceIntent.putExtra("LONGITUDE", it.getDoubleExtra("longitude", 0.0))
            serviceIntent.putExtra("UNIT", it.getStringExtra("unit"))
        }
        ContextCompat.startForegroundService(context!!, serviceIntent)
    }
}