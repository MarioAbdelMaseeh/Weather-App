package com.mario.skyeye.data.remote

import com.mario.skyeye.data.models.CurrentWeatherResponse

class ProductsRemoteDataSourceImpl(private val service: APIService) : ProductsRemoteDataSource {
    override suspend fun getCurrentWeather(): CurrentWeatherResponse? {
        return service.getCurrentWeather().body()
    }

}

