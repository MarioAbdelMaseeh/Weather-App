package com.mario.skyeye.data.repo
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.remote.RemoteDataSource

class RepoImpl private constructor(
    private val remoteDataSource: RemoteDataSource,
    //private val localDataSource: LocalDataSource
) : Repo {
    companion object {
        private var instance: RepoImpl? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            //localDataSource: LocalDataSource
        ): RepoImpl {
            if (instance == null) {
                instance = RepoImpl(remoteDataSource)
            }
            return instance!!
        }
    }

    override suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double): CurrentWeatherResponse? {
        return if (isOnline) {
            remoteDataSource.getCurrentWeather(lat,lon)
        } else {
            remoteDataSource.getCurrentWeather(lat,lon)
        }
    }


}