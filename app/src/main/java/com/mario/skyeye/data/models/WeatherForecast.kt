package com.mario.skyeye.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherForecast(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Item0>,
    val message: Int
){
    data class City(
        val coord: Coord,
        val country: String,
        val id: Int,
        val name: String,
        val population: Int,
        val sunrise: Int,
        val sunset: Int,
        val timezone: Int
    )
    data class Item0(
        val clouds: Clouds,
        val dt: Int,
        val dt_txt: String,
        val main: Main,
        val pop: Double,
        val rain: Rain,
        val sys: Sys,
        val visibility: Int,
        val weather: List<Weather>,
        val wind:Wind
    )

    data class Coord(
        val lat: Double,
        val lon: Double
    )

    data class Clouds(
        val all: Int
    )

    data class Main(
        val feels_like: Double,
        val grnd_level: Int,
        val humidity: Int,
        val pressure: Int,
        val sea_level: Int,
        val temp: Double,
        val temp_kf: Double,
        val temp_max: Double,
        val temp_min: Double
    )

    data class Rain(
        val `3h`: Double
    )

    data class Sys(
        val pod: String
    )

    data class Weather(
        val description: String,
        val icon: String,
        val id: Int,
        val main: String
    )

    data class Wind(
        val deg: Int,
        val gust: Double,
        val speed: Double
    )

}
fun WeatherForecast.forecastDaysHelper(): Map<Int, List<WeatherForecast.Item0>> {
    val forecastMap = mutableMapOf<Int, MutableList<WeatherForecast.Item0>>()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    for (item in list) {
        val dateKey = dateFormat.format(Date(item.dt * 1000L)).replace("-", "").toInt()
        if (forecastMap[dateKey] == null) {
            forecastMap[dateKey] = mutableListOf()
        }
        forecastMap[dateKey]?.add(item)
    }
    return forecastMap.mapValues { it.value.take(8) }
}

