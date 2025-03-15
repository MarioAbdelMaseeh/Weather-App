package com.mario.skyeye.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    const val BASE_URL: String = "https://api.openweathermap.org/data/2.5/"
    private val retrofitInstance = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: APIService = retrofitInstance.create(APIService::class.java)
}