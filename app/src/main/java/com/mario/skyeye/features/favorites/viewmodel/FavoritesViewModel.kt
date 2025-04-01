package com.mario.skyeye.features.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.repo.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoritesViewModel(val repo: Repo) : ViewModel(){
    private val _favoriteLocations = MutableStateFlow<Response<List<FavoriteLocation?>?>>(Response.Loading)
    val favoriteLocations: StateFlow<Response<List<FavoriteLocation?>?>> = _favoriteLocations.asStateFlow()

    fun fetchFavoriteLocations() {
        viewModelScope.launch {
            repo.getAllLocations()
                .map { it?.sortedBy { selection -> selection?.cityName }
                    ?.filterNot { it?.cityName == "home" } }
                .catch { e ->
                    _favoriteLocations.value = Response.Failure(e.message.toString())
                }
                .collect { locations ->
                _favoriteLocations.value = Response.Success<List<FavoriteLocation?>?>(locations)
            }
        }
    }
//    fun updateFavoriteLocations() {
//        viewModelScope.launch {
//            repo.getAllLocations()
//                .collect { locations ->
//                    locations?.forEach { selection ->
//                        selection?.let { safeSelection ->
//                            val weatherDeferred = async {
//                                repo.getCurrentWeather(
//                                    true,
//                                    safeSelection.latitude,
//                                    safeSelection.longitude
//                                )?.firstOrNull()
//                            }
//
//                            val forecastDeferred = async {
//                                repo.getWeatherForecast(
//                                    true,
//                                    safeSelection.latitude,
//                                    safeSelection.longitude
//                                )?.firstOrNull()
//                            }
//
//                            val weather = weatherDeferred.await()
//                            val forecast = forecastDeferred.await()
//
//                            if (weather != null && forecast != null) {
//                                repo.insertLocation(
//                                    FavoriteLocation(
//                                        safeSelection.cityName,
//                                        safeSelection.latitude,
//                                        safeSelection.longitude,
//                                        weather,
//                                        forecast
//                                    )
//                                )
//                            }
//                        }
//                    }
//                }
//            repo.getAllLocations()
//                .map { it?.sortedBy { selection -> selection?.cityName } }
//                .catch { e ->
//                    _favoriteLocations.value = Response.Failure(e)
//                }
//                .collect { locations ->
//                    _favoriteLocations.value = Response.Success<List<FavoriteLocation?>?>(locations)
//                }
//        }
//    }
    fun deleteLocation(location: FavoriteLocation) {
        viewModelScope.launch {
            repo.deleteLocation(location)
            fetchFavoriteLocations()
        }
    }

}
class FavoritesFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repo) as T
    }
}