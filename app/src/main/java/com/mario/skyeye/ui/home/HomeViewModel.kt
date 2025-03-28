package com.mario.skyeye.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherData
import com.mario.skyeye.data.models.WeatherForecast
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
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repo): ViewModel(){
    private val _locationState: MutableStateFlow<LatLng> = MutableStateFlow(LatLng(0.0,0.0))
    val locationState: StateFlow<LatLng> = _locationState.asStateFlow()
//    private val _currentWeatherState: MutableStateFlow<Response<CurrentWeatherResponse?>> = MutableStateFlow(Response.Loading)
//    val currentWeatherState: StateFlow<Response<CurrentWeatherResponse?>> = _currentWeatherState.asStateFlow()
//    private val _weatherForecastState: MutableStateFlow<Response<WeatherForecast?>> = MutableStateFlow(Response.Loading)
//    val weatherForecastState: StateFlow<Response<WeatherForecast?>> = _weatherForecastState.asStateFlow()

    private val _weatherDataState: MutableStateFlow<Response<WeatherData?>> = MutableStateFlow(Response.Loading)
    val weatherDataState: StateFlow<Response<WeatherData?>> = _weatherDataState.asStateFlow()


    init {
        getLocation()
        locationChangeListener()
    }
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _weatherDataState.value = Response.Failure(throwable)
    }
    fun fetchWeatherData(lat: Double, lon: Double,) {
        val tempUnit = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol())

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val currentWeatherDeferred = async { repo.getCurrentWeather(true, lat, lon, tempUnit) }
                val forecastDeferred = async { repo.getWeatherForecast(true, lat, lon, tempUnit) }

                val currentWeatherFlow = currentWeatherDeferred.await()
                val forecastFlow = forecastDeferred.await()

                var currentWeather: CurrentWeatherResponse? = null
                var weatherForecast: WeatherForecast? = null

                currentWeatherFlow?.catch { e ->
                    _weatherDataState.value = Response.Failure(e)
                }?.collect { response ->
                    currentWeather = response
                }

                forecastFlow?.catch { e ->
                    _weatherDataState.value = Response.Failure(e)
                }?.collect { response ->
                    weatherForecast = response
                }

                if (currentWeather != null && weatherForecast != null) {
                    _weatherDataState.value = Response.Success(WeatherData(currentWeather, weatherForecast))
                } else {
                    _weatherDataState.value = Response.Failure(Throwable("No Data"))
                }
            } catch (e: Exception) {
                _weatherDataState.value = Response.Failure(e)
            }
        }
    }


//    fun getCurrentWeather(lat: Double, lon: Double) {
//        val tempUnit = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol())
//        Log.i("HomeViewModel", "getCurrentWeather: $lat, $lon, $tempUnit")
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response = repo.getCurrentWeather(true, lat, lon,(tempUnit))
//                response?.catch { e ->
//                    _currentWeatherState.value = Response.Failure(e)
//                }?.collect { weatherResponse ->
//                    if (weatherResponse != null) {
//                        _currentWeatherState.value =
//                            Response.Success<CurrentWeatherResponse>(weatherResponse)
//                    }else{
//                        _currentWeatherState.value = Response.Failure(Throwable("No Data"))
//                    }
//                }
//            } catch (e: Exception) {
//                _currentWeatherState.value = Response.Failure(e)
//            }
//        }
//    }
//    fun getWeatherForecast(lat: Double, lon: Double) {
//        val tempUnit = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol())
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response = repo.getWeatherForecast(true, lat, lon, (tempUnit))
//                response?.catch { e ->
//                    _weatherForecastState.value = Response.Failure(e)
//                }?.collect { weatherForecast ->
//                    if (weatherForecast != null) {
//                        _weatherForecastState.value =
//                            Response.Success<WeatherForecast>(weatherForecast)
//                    } else {
//                        _weatherForecastState.value = Response.Failure(Throwable("No Data"))
//                    }
//                }
//            }catch (e: Exception) {
//                _weatherForecastState.value = Response.Failure(e)
//            }
//        }
//    }
    fun updateHomeScreen(): String{
       val flag =  repo.getPreference(Constants.UPDATE,"false")
        return flag
    }
    fun setUpdateHomeScreen(flag: String){
        repo.savePreference(Constants.UPDATE,flag)
    }
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