package com.mario.skyeye.ui

import com.mario.skyeye.R

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