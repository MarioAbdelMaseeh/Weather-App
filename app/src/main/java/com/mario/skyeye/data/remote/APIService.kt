package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.WeatherForecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = "844a4cd04ffae12ec335ae0bc0ea63ec",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): CurrentWeatherResponse?

    @GET("data/2.5/forecast")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = "844a4cd04ffae12ec335ae0bc0ea63ec",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherForecast?

    @GET("geo/1.0/reverse")
    suspend fun getCityName(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = "844a4cd04ffae12ec335ae0bc0ea63ec"
        ): GeoCoderResponse

    @GET("geo/1.0/direct")
    suspend fun getCoordinates(
        @Query("q") q: String,
        @Query("appid") appId: String = "844a4cd04ffae12ec335ae0bc0ea63ec"
    ): GeoCoderResponse
}

