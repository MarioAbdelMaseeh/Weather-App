package com.mario.skyeye.data.models

data class WeatherData(
    val currentWeatherResponse: CurrentWeatherResponse?,
    val forecastResponse: WeatherForecast?
)