package com.mario.skyeye.features.details.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetailsViewModel(private val repo: Repo): ViewModel(){
    private val _weatherDataState: MutableStateFlow<Response<WeatherData?>> = MutableStateFlow(Response.Loading)
    val weatherDataState: StateFlow<Response<WeatherData?>> = _weatherDataState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _weatherDataState.value = Response.Failure(throwable.message.toString())
    }

    fun fetchWeatherData(lat: Double, lon: Double,cityName: String) {
        val tempUnit = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.unitType)

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val currentWeatherDeferred = async { repo.getCurrentWeather( lat, lon, tempUnit) }
                val forecastDeferred = async { repo.getWeatherForecast(lat, lon, tempUnit) }

                val currentWeatherFlow = currentWeatherDeferred.await()
                val forecastFlow = forecastDeferred.await()

                var currentWeather: CurrentWeatherResponse? = null
                var weatherForecast: WeatherForecast? = null

                currentWeatherFlow?.catch { e ->
                    _weatherDataState.value = Response.Failure(e.message.toString())
                }?.collect { response ->
                    currentWeather = response
                }

                forecastFlow?.catch { e ->
                    _weatherDataState.value = Response.Failure(e.message.toString())
                }?.collect { response ->
                    weatherForecast = response
                }

                if (currentWeather != null && weatherForecast != null) {
                    _weatherDataState.value = Response.Success(WeatherData(currentWeather, weatherForecast))
                    updateLocation(FavoriteLocation(cityName, lat, lon,
                        currentWeather!!, weatherForecast!!
                    ))

                } else {
                    _weatherDataState.value = Response.Failure("No Data")
                }
            } catch (e: Exception) {
                _weatherDataState.value = Response.Failure(e.message.toString())
            }
        }
    }


    fun updateLocation(location: FavoriteLocation){
        val tempUnit = repo.getPreference("temp_unit", "°C")
        viewModelScope.launch {
            val favoriteLocation = FavoriteLocation(location.cityName, location.latitude, location.longitude,
                repo.getCurrentWeather(location.latitude, location.longitude,tempUnit)?.firstOrNull()!!,
                repo.getWeatherForecast(location.latitude, location.longitude,tempUnit)?.firstOrNull()!!)
            repo.insertLocation(favoriteLocation)
        }
    }
    val tempUnit: String
        get() = repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol())
    val windSpeedUnit: String
        get() = repo.getPreference(Constants.WIND_UNIT, TempUnit.METRIC.getWindSymbol())
}
class DetailsFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailsViewModel(repo) as T
    }
}