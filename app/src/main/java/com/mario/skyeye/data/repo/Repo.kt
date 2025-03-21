package com.mario.skyeye.data.repo

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface Repo {
    suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double): Flow<CurrentWeatherResponse?>?
    suspend fun getWeatherForecast(isOnline: Boolean, lat: Double, lon: Double): Flow<WeatherForecast?>?

}