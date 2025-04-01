package com.mario.skyeye.utils

import com.mario.skyeye.R
import java.util.Locale

object WeatherIconMapper {
    private val weatherIconMap = mapOf(
        "01d" to R.drawable.base_sun,
        "01n" to R.drawable.base_moon,
        "02d" to R.drawable.cloudy_day,
        "02n" to R.drawable.cloudy_night,
        "03d" to R.drawable.very_cloudy_day,
        "03n" to R.drawable.very_cloudy_night,
        "04d" to R.drawable.very_cloudy_day,
        "04n" to R.drawable.very_cloudy_night,
        "09d" to R.drawable.rainy_day,
        "09n" to R.drawable.rainy_night,
        "10d" to R.drawable.rainy_day,
        "10n" to R.drawable.rainy_night,
        "11d" to R.drawable.th_day,
        "11n" to R.drawable.th_night,
        "13d" to R.drawable.snow_day,
        "13n" to R.drawable.snow_night,
        "50d" to R.drawable.mist_day,
        "50n" to R.drawable.mist_night
    )
    fun getWeatherIcon(iconCode: String): Int {
        return weatherIconMap[iconCode] ?: R.drawable.base_sun
    }
}
fun String.getWeatherNotification(): String {
    val notifications = mapOf(
        "01d" to mapOf(
            "en" to "Clear sky during the day! Enjoy the sunshine. ☀️",
            "ar" to "سماء صافية خلال النهار! استمتع بأشعة الشمس. ☀️",
        ),
        "01n" to mapOf(
            "en" to "Clear night sky! Perfect for stargazing. 🌙",
            "ar" to "سماء صافية في الليل! مثالية لمشاهدة النجوم. 🌙",
        ),
        "02d" to mapOf(
            "en" to "A few clouds in the sky, but still a nice day! ⛅",
            "ar" to "بعض السحب في السماء، لكن الجو لا يزال جميلاً! ⛅",
        ),
        "02n" to mapOf(
            "en" to "Partly cloudy night! Enjoy the cool breeze. 🌌",
            "ar" to "ليلة غائمة جزئيًا! استمتع بالنسيم البارد. 🌌",
        ),
        "03d" to mapOf(
            "en" to "Scattered clouds today. ☁️",
            "ar" to "غيوم متفرقة اليوم. ☁️",
        ),
        "03n" to mapOf(
            "en" to "Scattered clouds at night. 🌥️",
            "ar" to "غيوم متفرقة في الليل. 🌥️",
        ),
        "04d" to mapOf(
            "en" to "Broken clouds covering the sky. 🌥️",
            "ar" to "غيوم متقطعة تغطي السماء. 🌥️",
        ),
        "04n" to mapOf(
            "en" to "Broken clouds tonight. Might feel chilly! 🌙",
            "ar" to "غيوم متقطعة الليلة. قد يكون الجو باردًا! 🌙",
        ),
        "09d" to mapOf(
            "en" to "Shower rain expected. Carry an umbrella! 🌧️",
            "ar" to "متوقع هطول أمطار غزيرة. احمل مظلة! 🌧️",
        ),
        "09n" to mapOf(
            "en" to "Shower rain at night. Stay warm! 🌧️",
            "ar" to "أمطار غزيرة في الليل. ابقَ دافئًا! 🌧️",
        ),
        "10d" to mapOf(
            "en" to "Rain expected during the day. Don't forget your raincoat! ☔",
            "ar" to "من المتوقع هطول أمطار خلال النهار. لا تنس معطف المطر! ☔",
        ),
        "10n" to mapOf(
            "en" to "Rainy night ahead. Stay dry! ☔",
            "ar" to "ليلة ماطرة قادمة. ابقَ جافًا! ☔",
        ),
    )
    val language = Locale.getDefault().language
    return notifications[this]?.get(language) ?: this
}