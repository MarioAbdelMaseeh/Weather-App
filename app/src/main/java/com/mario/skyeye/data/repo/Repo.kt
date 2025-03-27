package com.mario.skyeye.data.repo

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.GeoCoderResponseItem
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface Repo {
    suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>?
    suspend fun getWeatherForecast(isOnline: Boolean, lat: Double, lon: Double, units: String): Flow<WeatherForecast?>?
    suspend fun getCityName( lat: Double, lon: Double): Flow<GeoCoderResponse?>?
    suspend fun getCoordinates( q: String): Flow<GeoCoderResponse?>?
    suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?>
    suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int
    suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long
    fun savePreference(key: String, value: String)
    fun getPreference(key: String, defaultValue: String): String
}