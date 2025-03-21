package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeatherResponse?>?
    suspend fun getWeatherForecast(lat: Double, lon: Double): Flow<WeatherForecast?>?

}