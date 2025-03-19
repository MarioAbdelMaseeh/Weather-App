package com.mario.skyeye.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.repo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repo): ViewModel(){
    private val _currentWeatherState: MutableStateFlow<Response<CurrentWeatherResponse?>> = MutableStateFlow(Response.Loading)
    val currentWeatherState: StateFlow<Response<CurrentWeatherResponse?>> = _currentWeatherState.asStateFlow()
    private val _weatherForecastState: MutableStateFlow<Response<WeatherForecast?>> = MutableStateFlow(Response.Loading)
    val weatherForecastState: StateFlow<Response<WeatherForecast?>> = _weatherForecastState.asStateFlow()
    private val _message: MutableSharedFlow<String> = MutableSharedFlow()
    val message: SharedFlow<String> = _message.asSharedFlow()

    fun getCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getCurrentWeather(true, lat, lon)
                response?.catch { e ->
                    _currentWeatherState.value = Response.Failure(e)
                }?.collect { weatherResponse ->
                    if (weatherResponse != null) {
                        _currentWeatherState.value =
                            Response.Success<CurrentWeatherResponse>(weatherResponse)
                    }else{
                        _currentWeatherState.value = Response.Failure(Throwable("No Data"))
                    }
                }
            } catch (e: Exception) {
                _currentWeatherState.value = Response.Failure(e)
            }
        }
    }
    fun getWeatherForecast(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getWeatherForecast(true, lat, lon)
                response?.catch { e ->
                    _weatherForecastState.value = Response.Failure(e)
                }?.collect { weatherForecast ->
                    if (weatherForecast != null) {
                        _weatherForecastState.value =
                            Response.Success<WeatherForecast>(weatherForecast)
                    } else {
                        _weatherForecastState.value = Response.Failure(Throwable("No Data"))
                    }
                }
            }catch (e: Exception) {
                _weatherForecastState.value = Response.Failure(e)
            }
        }
    }

}
class HomeFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}