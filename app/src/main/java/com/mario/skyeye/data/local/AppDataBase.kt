package com.mario.skyeye.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.WeatherConverters
import kotlin.jvm.java

@Database(entities = [FavoriteLocation::class, Alarm::class], version = 2)
@TypeConverters(WeatherConverters::class)
abstract class AppDataBase : RoomDatabase(){
    abstract fun weatherDao(): WeatherDao
    abstract fun alarmDao(): AlarmDao
    companion object{
        @Volatile
        private var INSTANCE : AppDataBase? = null
        fun getInstance(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDataBase::class.java, "weather_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}