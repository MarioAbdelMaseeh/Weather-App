package com.mario.skyeye.features.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherData
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.utils.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repo): ViewModel(){
    private val _locationState: MutableStateFlow<LatLng> = MutableStateFlow(LatLng(0.0,0.0))
    val locationState: StateFlow<LatLng> = _locationState.asStateFlow()

    private val _weatherDataState: MutableStateFlow<Response<WeatherData?>> = MutableStateFlow(Response.Loading)
    val weatherDataState: StateFlow<Response<WeatherData?>> = _weatherDataState.asStateFlow()

    init {
        getLocation()
        locationChangeListener()
    }
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            fetchWeatherDataLocal()
    }

    private fun fetchWeatherDataLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getFavoriteLocationByCityName("home").catch {
            _weatherDataState.value = Response.Failure(it.message.toString())
            }.collect {
                if (it != null) {
                    _weatherDataState.value =
                        Response.Success(
                            WeatherData(
                                it.currentWeatherResponse,
                                it.forecastResponse
                            )
                        )
                } else {
                    _weatherDataState.value = Response.Failure("No data")
                }
            }
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        val tempUnit = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.unitType)

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val currentWeatherDeferred = async { repo.getCurrentWeather( lat, lon, tempUnit) }
                val forecastDeferred = async { repo.getWeatherForecast(lat, lon, tempUnit) }

                val currentWeather = currentWeatherDeferred.await()?.firstOrNull()
                var weatherForecast = forecastDeferred.await()?.firstOrNull()

                if (currentWeather != null && weatherForecast != null) {
                    _weatherDataState.value = Response.Success(WeatherData(currentWeather, weatherForecast))
                    repo.insertLocation(FavoriteLocation("home",lat,lon,
                        currentWeather, weatherForecast))
                } else {
                    fetchWeatherDataLocal()
                }
            } catch (e: Exception) {
                fetchWeatherDataLocal()
            }
        }
    }


//    fun updateHomeScreen(): String{
//       val flag =  repo.getPreference(Constants.UPDATE,"false")
//        return flag
//    }
//    fun setUpdateHomeScreen(flag: String){
//        repo.savePreference(Constants.UPDATE,flag)
//    }
    fun locationChangeListener(){
        viewModelScope.launch {
            repo.onChangeCurrentLocation().collect {
                val location = it.split(",")
                _locationState.value = LatLng(location[0].toDouble(), location[1].toDouble())
                Log.i("HomeViewModel", "getLocation: ${_locationState.value}")
            }
        }
    }
    fun getLocation(){
        val location = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")
        _locationState.value = LatLng(location[0].toDouble(), location[1].toDouble())
    }
    val tempUnit: String
        get() = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol())
    val windSpeedUnit: String
        get() = repo.getPreference(Constants.WIND_UNIT, TempUnit.METRIC.getWindSymbol())
}
class HomeFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}