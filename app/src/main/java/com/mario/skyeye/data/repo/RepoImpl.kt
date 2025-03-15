package com.mario.skyeye.data.repo

import com.mario.skyeye.data.local.LocalDataSource
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.remote.ProductsRemoteDataSource

class RepoImpl private constructor(
    private val remoteDataSource: ProductsRemoteDataSource,
    private val localDataSource: LocalDataSource
) : Repo {
    companion object {
        private var instance: RepoImpl? = null
        fun getInstance(
            remoteDataSource: ProductsRemoteDataSource,
            localDataSource: LocalDataSource
        ): RepoImpl {
            if (instance == null) {
                instance = RepoImpl(remoteDataSource, localDataSource)
            }
            return instance!!
        }
    }

    override suspend fun getCurrentWeather(isOnline: Boolean): CurrentWeatherResponse? {
        return if (isOnline) {
            remoteDataSource.getCurrentWeather()
        } else {
            remoteDataSource.getCurrentWeather()
        }
    }


}