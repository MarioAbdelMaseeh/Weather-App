package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoteDataSourceImpl(private val service: APIService) : RemoteDataSource {
    override suspend fun getCurrentWeather(lat: Double, lon: Double): Flow<CurrentWeatherResponse?>? {
        return flowOf(service.getCurrentWeather(lat,lon))
    }

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double
    ): Flow<WeatherForecast?>? {
        return flowOf(service.getWeatherForecast(lat,lon))
    }

}

