package com.mario.skyeye.feature.map.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFindAutocompletePredictions
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.utils.Constants
import com.mario.skyeye.utils.PlacesClientManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MapViewModel (private val repo: Repo, private val placesClient: PlacesClient) : ViewModel(){

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _predictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val predictions = _predictions.asStateFlow()

    private val _selectedLocation = MutableStateFlow(LatLng(0.0,0.0))
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _cityName = MutableStateFlow("")
    val cityName = _cityName.asStateFlow()

    private val cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(LatLng(_selectedLocation.value.latitude, _selectedLocation.value.longitude), 10f))
    val cameraPositionState = cameraPosition.asStateFlow()

    init {
        observeSearchQuery()
        getCurrentLocation()
        getCityName(_selectedLocation.value)
    }
    fun updateSearchQuery(query: String){
        _searchQuery.value = query
    }
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery(){
        _searchQuery.debounce(500.milliseconds)
            .distinctUntilChanged()
            .onEach {
                query -> fetchPredictions(query)
            }
            .launchIn(viewModelScope)
    }

    private fun fetchPredictions(query: String){
        viewModelScope.launch {
            try {
                val response = placesClient.awaitFindAutocompletePredictions {
                    FindAutocompletePredictionsRequest.builder().typesFilter = listOf(PlaceTypes.LOCALITY, PlaceTypes.COUNTRY)
                    this.query = query
                }
                _predictions.value = response.autocompletePredictions
            }catch (e: Exception){
                _predictions.value = emptyList()
            }
        }
    }
    fun selectLocation(latLng: LatLng){
        _selectedLocation.value = latLng
        cameraPosition.value = CameraPosition.fromLatLngZoom(latLng, 10f)
    }
    fun updateCityName(cityName: String){
        _cityName.value = cityName
    }
    fun clearPredictions(){
        _predictions.value = emptyList()
    }
    fun getCoordinates(query: String){
        val flag = true
        viewModelScope.launch {
            repo.getCoordinates(query)?.catch { e ->
                Log.i("TAG", "getCoordinates: ${e.message}")
            }
                ?.collect{
                if (it != null) {
                    if (it.isEmpty() && flag) {
                        flag == false
                        getCoordinates(query.substringBefore(","))
                    }else if (it.isNotEmpty()) {
                        selectLocation(LatLng(it[0].lat, it[0].lon))
                        cameraPosition.value = CameraPosition.fromLatLngZoom(LatLng(it[0].lat, it[0].lon), 10f)
                    }
                }
            }
            clearPredictions()
        }
    }
    fun getCityName(latLng: LatLng){
        viewModelScope.launch {
            repo.getCityName(latLng.latitude, latLng.longitude)?.collect{
                if (it != null && it.isNotEmpty()) {
                    _cityName.value = it[0].name
                }
            }
        }
    }
    fun saveLocation(latLng: LatLng?){
        val tempUnit = repo.getPreference("temp_unit", "°C")
        viewModelScope.launch {
            if (latLng != null){
                val currentWeatherResponse = repo.getCurrentWeather( latLng.latitude, latLng.longitude,(tempUnit))?.first()
                repo.insertLocation(FavoriteLocation(cityName.value, latLng.latitude, latLng.longitude, currentWeatherResponse!!, repo.getWeatherForecast( latLng.latitude, latLng.longitude, (tempUnit))?.first()!!))
            }else{
                Log.i("TAG", "saveLocation: Location is null")
            }
        }
    }
    fun getCurrentLocation(){
        repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").let {
            val location = it.split(",")
            _selectedLocation.value = LatLng(location[0].toDouble(), location[1].toDouble())
        }
    }
    fun setDefaultLocation(latLng: LatLng){
        viewModelScope.launch {
            repo.savePreference(Constants.CURRENT_LOCATION, "${latLng.latitude},${latLng.longitude}")
        }
    }
    fun updatePreference(key: String, value: String){
        viewModelScope.launch {
            repo.savePreference(key, value)
        }
    }
    override fun onCleared() {
        super.onCleared()
        PlacesClientManager.shutdown()
    }

}
class MapFactory(private val repo: Repo, private val placesClient: PlacesClient): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(repo, placesClient) as T
    }
}