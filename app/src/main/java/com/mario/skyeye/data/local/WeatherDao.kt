package com.mario.skyeye.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM favorite_locations")
    fun getAllFavoriteLocations(): Flow<List<FavoriteLocation?>?>
    @Query("SELECT * FROM favorite_locations where cityName = :cityName")
    fun getFavoriteLocationByCityName(cityName: String): Flow<FavoriteLocation?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteLocation: FavoriteLocation): Long
    @Delete
    suspend fun delete(favoriteLocation: FavoriteLocation): Int
}
