package com.mario.skyeye.data.models

import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable

@Entity(tableName = "favorite_locations", primaryKeys = ["cityName"])
@TypeConverters(WeatherConverters::class)
 data class FavoriteLocation(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val currentWeatherResponse: CurrentWeatherResponse,
    val forecastResponse: WeatherForecast)

class WeatherConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCurrentWeatherResponse(value: CurrentWeatherResponse): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCurrentWeatherResponse(value: String): CurrentWeatherResponse {
        val type = object : TypeToken<CurrentWeatherResponse>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromWeatherForecast(value: WeatherForecast): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherForecast(value: String): WeatherForecast {
        val type = object : TypeToken<WeatherForecast>() {}.type
        return gson.fromJson(value, type)
    }
}