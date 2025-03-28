package com.mario.skyeye.data.repo
import com.mario.skyeye.data.local.LocalDataSource
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.remote.RemoteDataSource
import com.mario.skyeye.data.sharedprefrence.AppPreference
import kotlinx.coroutines.flow.Flow

class RepoImpl private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val sharedPreferences: AppPreference
) : Repo {
    companion object {
        private var instance: RepoImpl? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
            localDataSource: LocalDataSource,
            sharedPreferences: AppPreference
        ): RepoImpl {
            if (instance == null) {
                instance = RepoImpl(remoteDataSource, localDataSource, sharedPreferences)
            }
            return instance!!
        }
    }

    override suspend fun getCurrentWeather(isOnline: Boolean, lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>? {
        return if (isOnline) {
            remoteDataSource.getCurrentWeather(lat,lon, units)
        } else {
            remoteDataSource.getCurrentWeather(lat,lon, units)
        }
    }

    override suspend fun getWeatherForecast(
        isOnline: Boolean,
        lat: Double,
        lon: Double,
        units: String
    ): Flow<WeatherForecast?>? {
        return if (isOnline) {
            remoteDataSource.getWeatherForecast(lat,lon, units)
        } else {
            remoteDataSource.getWeatherForecast(lat,lon, units)
        }
    }

    override suspend fun getCityName(
        lat: Double,
        lon: Double
    ): Flow<GeoCoderResponse?>? {
        return remoteDataSource.getCityName(lat,lon)
    }

    override suspend fun getCoordinates(q: String): Flow<GeoCoderResponse?>? {
        return remoteDataSource.getCoordinates(q)
    }

    override suspend fun getAllLocations(): Flow<List<FavoriteLocation?>?> {
        return localDataSource.getAllLocations()
    }

    override suspend fun deleteLocation(favoriteLocation: FavoriteLocation): Int {
        return localDataSource.deleteLocation(favoriteLocation)
    }

    override suspend fun insertLocation(favoriteLocation: FavoriteLocation): Long {
        return localDataSource.insertLocation(favoriteLocation)
    }

    override fun savePreference(key: String, value: String) {
        sharedPreferences.savePreference(key, value)
    }

    override fun getPreference(key: String, defaultValue: String): String {
        return sharedPreferences.getPreference(key, defaultValue)
    }

    override fun onChangeCurrentLocation(): Flow<String> {
        return sharedPreferences.onChangeCurrentLocation()
    }
}