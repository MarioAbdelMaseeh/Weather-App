package com.mario.skyeye.data.repo
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

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

    override suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double): Flow<CurrentWeatherResponse?>? {
        return if (isOnline) {
            remoteDataSource.getCurrentWeather(lat,lon)
        } else {
            remoteDataSource.getCurrentWeather(lat,lon)
        }
    }

    override suspend fun getWeatherForecast(
        isOnline: Boolean,
        lat: Double,
        lon: Double
    ): Flow<WeatherForecast?>? {
        return if (isOnline) {
            remoteDataSource.getWeatherForecast(lat,lon)
        } else {
            remoteDataSource.getWeatherForecast(lat,lon)
        }
    }
}