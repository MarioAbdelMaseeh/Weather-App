package com.mario.skyeye.data.local

import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl (private val weatherDao: WeatherDao): LocalDataSource {
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



}

