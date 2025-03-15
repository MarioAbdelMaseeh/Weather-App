package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import retrofit2.Response
import retrofit2.http.GET

interface APIService {
    @GET("weather?lat=44.34&lon=10.99&appid=844a4cd04ffae12ec335ae0bc0ea63ec")
    suspend fun getCurrentWeather(): Response<CurrentWeatherResponse>
}

