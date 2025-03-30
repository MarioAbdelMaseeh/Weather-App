package com.mario.skyeye.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherData

@Composable
fun getWeatherBasedColors(weatherData: Response<WeatherData?>): WeatherColors {
    return when ((weatherData as? Response.Success)?.data?.currentWeatherResponse?.weather?.get(0)?.icon) {
        "01d" -> WeatherColors(Color(0xFFB3E5FC), Color(0xFF81D4FA)) // Baby blue for sunny day
        "01n" -> WeatherColors(Color(0xFF0D47A1), Color(0xFF1A237E)) // Deep blue for clear night
        "02d", "03d", "04d" -> WeatherColors(
            Color(0xFFB0BEC5),
            Color(0xFF90A4AE)
        ) // Soft gray for cloudy days
        "02n", "03n", "04n" -> WeatherColors(
            Color(0xFF37474F),
            Color(0xFF263238)
        ) // Darker gray for cloudy nights
        "09d", "10d" -> WeatherColors(
            Color(0xFF78909C),
            Color(0xFF607D8B)
        ) // Muted blue-gray for rainy days
        "09n", "10n" -> WeatherColors(
            Color(0xFF263238),
            Color(0xFF1C313A)
        ) // Dark blue-gray for rainy nights
        "11d" -> WeatherColors(Color(0xFF455A64), Color(0xFF263238)) // Dark stormy tones for day
        "11n" -> WeatherColors(
            Color(0xFF1B1B1B),
            Color(0xFF000000)
        ) // Almost black for stormy night
        "13d" -> WeatherColors(Color(0xFFE0F7FA), Color(0xFFB2EBF2)) // Icy blue for snowy day
        "13n" -> WeatherColors(
            Color(0xFF90A4AE),
            Color(0xFF78909C)
        ) // Dimmed icy blue for snowy night
        "50d" -> WeatherColors(Color(0xFFCFD8DC), Color(0xFFB0BEC5)) // Misty, foggy look for day
        "50n" -> WeatherColors(Color(0xFF424242), Color(0xFF212121)) // Dark misty night
        else -> WeatherColors(Color(0xFFCFD8DC), Color(0xFFB0BEC5)) // Neutral fallback for day
    }
}

fun getContrastingTextColor(bgColor: Color): Color {
    val luminance = bgColor.luminance()
    return if (luminance < 0.5) Color.White else Color.Black
}

data class WeatherColors(
    val light: Color,
    val dark: Color,
    val textColor: Color = getContrastingTextColor(light)
)