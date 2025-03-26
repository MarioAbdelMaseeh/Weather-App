package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun getCurrentWeather(lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>?
    suspend fun getWeatherForecast(lat: Double, lon: Double, units: String): Flow<WeatherForecast?>?
    suspend fun getCityName(lat: Double, lon: Double): Flow<GeoCoderResponse?>?
    suspend fun getCoordinates(q: String): Flow<GeoCoderResponse?>?
}