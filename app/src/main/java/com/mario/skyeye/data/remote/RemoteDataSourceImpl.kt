package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse

class RemoteDataSourceImpl(private val service: APIService) : RemoteDataSource {
    override suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentWeatherResponse? {
        return service.getCurrentWeather(lat,lon).body()
    }

}

