package com.mario.skyeye.alarm

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
        ContextCompat.startForegroundService(context!!, serviceIntent)
    }
}