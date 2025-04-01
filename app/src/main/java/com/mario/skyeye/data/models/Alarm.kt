package com.mario.skyeye.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "alarms")
data class Alarm(
    val triggerTime: Long, // Timestamp in milliseconds
    val label: String = "Weather Alert",
    val isEnabled: Boolean = true,
    val repeatInterval: Long = 0, // Repeat interval in hours (zero for one-time alarms)
    @PrimaryKey
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDateTime(): String {
        return SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(triggerTime))
    }
}