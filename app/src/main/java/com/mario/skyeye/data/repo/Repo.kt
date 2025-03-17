package com.mario.skyeye.data.repo

import com.mario.skyeye.data.models.CurrentWeatherResponse

interface Repo {
    suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double): CurrentWeatherResponse?
}