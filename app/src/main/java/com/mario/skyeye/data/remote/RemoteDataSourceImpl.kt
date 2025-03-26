package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.WeatherForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoteDataSourceImpl(private val service: APIService) : RemoteDataSource {
    override suspend fun getCurrentWeather(lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>? {
        return flowOf(service.getCurrentWeather(lat,lon, units = units))
    }

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String
    ): Flow<WeatherForecast?>? {
        return flowOf(service.getWeatherForecast(lat,lon, units = units))
    }

    override suspend fun getCityName(
        lat: Double,
        lon: Double
    ): Flow<GeoCoderResponse?>? {
        return flowOf(service.getCityName(lat,lon))
    }


    override suspend fun getCoordinates(q: String): Flow<GeoCoderResponse?>? {
        return flowOf(service.getCoordinates(q))
    }

}

