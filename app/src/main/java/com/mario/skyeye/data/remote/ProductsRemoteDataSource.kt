package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse

interface ProductsRemoteDataSource {
    suspend fun getCurrentWeather(): CurrentWeatherResponse?
}