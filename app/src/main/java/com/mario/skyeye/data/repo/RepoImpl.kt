package com.mario.skyeye.data.repo
import com.mario.skyeye.data.local.LocalDataSource
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.GeoCoderResponse
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.remote.RemoteDataSource
import com.mario.skyeye.data.sharedprefrence.AppPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    override suspend fun getCurrentWeather(lat: Double, lon: Double, units: String): Flow<CurrentWeatherResponse?>? {
        return remoteDataSource.getCurrentWeather(lat,lon, units)
    }

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String
    ): Flow<WeatherForecast?>? {
        return remoteDataSource.getWeatherForecast(lat,lon, units)
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

    override suspend fun getFavoriteLocationByCityName(cityName: String): Flow<FavoriteLocation?> {
        return localDataSource.getFavoriteLocationByCityName(cityName)
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

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return localDataSource.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        localDataSource.updateAlarm(alarm)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        localDataSource.deleteAlarm(alarm)
    }

    override suspend fun getAllAlarms(): Flow<List<Alarm>> {
        return localDataSource.getAllAlarms()
    }

    override suspend fun deleteAlarmByLabel(label: String) {
        localDataSource.deleteAlarmByLabel(label)
    }

    override suspend fun getAlarmByCreatedAt(createdAt: Long): Flow<Alarm?> {
        return localDataSource.getAlarmByCreatedAt(createdAt)
    }

}