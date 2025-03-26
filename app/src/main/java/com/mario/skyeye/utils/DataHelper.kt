package com.mario.skyeye.utils

import android.content.Context
import com.mario.skyeye.R
import java.text.SimpleDateFormat
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
