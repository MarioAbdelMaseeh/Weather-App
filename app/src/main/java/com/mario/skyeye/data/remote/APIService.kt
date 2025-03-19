package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String = "844a4cd04ffae12ec335ae0bc0ea63ec",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): Response<CurrentWeatherResponse>?
}

