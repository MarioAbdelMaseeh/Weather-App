package com.mario.skyeye.data.local

import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl (private val weatherDao: WeatherDao, val alarmDao: AlarmDao): LocalDataSource {
    override suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?> {
        return weatherDao.getAllFavoriteLocations()
    }

    override suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int {
        return weatherDao.delete(favoriteLocation)
    }

    override suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long {
        return weatherDao.insert(favoriteLocation)
    }

    override suspend fun getFavoriteLocationByCityName(cityName: String): Flow<FavoriteLocation?> {
        return weatherDao.getFavoriteLocationByCityName(cityName)
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }

    override suspend fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms()
    }


}

