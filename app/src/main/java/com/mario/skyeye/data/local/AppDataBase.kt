package com.mario.skyeye.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mario.skyeye.data.models.CurrentWeatherResponse
import kotlin.jvm.java

//@Database(entities = [CurrentWeatherResponse::class], version = 1)
//abstract class AppDataBase : RoomDatabase(){
//    abstract fun weatherDao(): WeatherDao
//    companion object{
//        @Volatile
//        private var INSTANCE : AppDataBase? = null
//        fun getInstance(context: Context): AppDataBase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext, AppDataBase::class.java, "weather_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}