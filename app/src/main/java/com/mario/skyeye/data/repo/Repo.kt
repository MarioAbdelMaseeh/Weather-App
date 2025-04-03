package com.mario.skyeye.data.repo

import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.GeoCoderResponseItem
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface Repo {
    suspend fun getCurrentWeather( lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>?
    suspend fun getWeatherForecast(lat: Double, lon: Double, units: String): Flow<WeatherForecast?>?
    suspend fun getCityName( lat: Double, lon: Double): Flow<GeoCoderResponse?>?
    suspend fun getCoordinates( q: String): Flow<GeoCoderResponse?>?
    suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?>
    suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int
    suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long
    suspend fun getFavoriteLocationByCityName(cityName: String): Flow<FavoriteLocation?>
    fun savePreference(key: String, value: String)
    fun getPreference(key: String, defaultValue: String): String
    fun onChangeCurrentLocation(): Flow<String>

    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun deleteAlarmByLabel(label: String)
    suspend fun getAlarmByCreatedAt(createdAt: Long): Flow<Alarm?>
    suspend fun deleteAlarmByCreatedAt(createdAt: Long)

}