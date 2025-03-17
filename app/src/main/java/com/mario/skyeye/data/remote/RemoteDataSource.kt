package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse

interface RemoteDataSource {
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse?
}