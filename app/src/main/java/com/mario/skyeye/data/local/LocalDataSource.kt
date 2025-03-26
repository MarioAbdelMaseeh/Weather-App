package com.mario.skyeye.data.local

import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?>
    suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int
    suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long
}