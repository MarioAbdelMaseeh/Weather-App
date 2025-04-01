package com.mario.skyeye.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mario.skyeye.data.models.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAlarm(alarm: Alarm)
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<Alarm>>
    @Query("Delete FROM alarms WHERE label = :label")
    suspend fun deleteAlarmByLabel(label: String)
    @Query("SELECT * FROM alarms WHERE createdAt = :createdAt")
    fun getAlarmByCreatedAt(createdAt: Long): Flow<Alarm?>

}