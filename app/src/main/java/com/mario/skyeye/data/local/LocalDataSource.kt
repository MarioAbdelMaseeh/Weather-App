package com.mario.skyeye.data.local

import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?>
    suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int
    suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long
    suspend fun getFavoriteLocationByCityName(cityName: String):  Flow<FavoriteLocation?>
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun deleteAlarmByLabel(label: String)
    suspend fun getAlarmByCreatedAt(createdAt: Long): Flow<Alarm?>

}