package com.mario.skyeye.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.mario.skyeye.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getRelativeTime(timestamp: Int , context: Context): String {
    val now = System.currentTimeMillis()
    val diff = now - (timestamp.toLong() * 1000) // Convert to milliseconds

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> context.getString(R.string.now)
        diff < TimeUnit.MINUTES.toMillis(2) -> context.getString(R.string.a_minute_ago)

        diff < TimeUnit.HOURS.toMillis(1) ->
            context.getString(
                R.string.minutes_ago,
                LanguageManager.formatNumberBasedOnLanguage("${diff / TimeUnit.MINUTES.toMillis(1)}")
            )

        diff < TimeUnit.HOURS.toMillis(2) -> context.getString(R.string.an_hour_ago)

        diff < TimeUnit.DAYS.toMillis(1) ->
            context.getString(
                R.string.hours_ago,
                LanguageManager.formatNumberBasedOnLanguage("${diff / TimeUnit.HOURS.toMillis(1)}")
            )

        diff < TimeUnit.DAYS.toMillis(2) -> context.getString(R.string.yesterday)

        diff < TimeUnit.DAYS.toMillis(7) ->
            context.getString(
                R.string.days_ago,
                LanguageManager.formatNumberBasedOnLanguage("${diff / TimeUnit.DAYS.toMillis(1)}")
            )
        else -> {
            val date = Date(timestamp.toLong() * 1000)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(date)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun getHourFormTime(timestamp: Long): String {
    val time = Instant.ofEpochSecond(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDayName(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)
    val localDate = LocalDate.parse(dateString, formatter)
    return localDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
}
