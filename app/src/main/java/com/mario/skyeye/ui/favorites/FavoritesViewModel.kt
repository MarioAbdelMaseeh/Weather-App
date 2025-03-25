package com.mario.skyeye.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.repo.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoritesViewModel(val repo: Repo) : ViewModel(){
    private val _favoriteLocations = MutableStateFlow<List<FavoriteLocation?>?>(emptyList())
    val favoriteLocations: StateFlow<List<FavoriteLocation?>?> = _favoriteLocations.asStateFlow()

    fun fetchFavoriteLocations() {
        viewModelScope.launch {
            repo.getAllLocations()
                .map { it?.sortedBy { selection -> selection?.cityName } }
                .collect { locations ->
                _favoriteLocations.value = locations
            }
        }
    }
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