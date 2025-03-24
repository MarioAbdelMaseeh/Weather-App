package com.mario.skyeye.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFindAutocompletePredictions
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.locationState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MapViewModel (private val repo: Repo, private val placesClient: PlacesClient) : ViewModel(){
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _predictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val predictions = _predictions.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _cityName = MutableStateFlow("")
    val cityName = _cityName.asStateFlow()

    private val cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(LatLng(locationState.value.latitude, locationState.value.longitude), 10f))
    val cameraPositionState = cameraPosition.asStateFlow()

    init {
        observeSearchQuery()
        getCityName(LatLng(locationState.value.latitude, locationState.value.longitude))
    }
    fun updateSearchQuery(query: String){
        _searchQuery.value = query
    }
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery(){
        _searchQuery.debounce(500.milliseconds)
            .filter { it.isNotEmpty() }
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
                    typesFilter = listOf(PlaceTypes.LOCALITY, PlaceTypes.COUNTRY)
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
        viewModelScope.launch {
            repo.getCoordinates(query)?.collect{
                Log.i("TAG", "getCoordinates: ${it?.get(0)?.lat} ${it?.get(0)?.lon} ")
                if (it != null) {
                    Log.i("TAG", "getCoordinates: ${it[0].lat} ${it[0].lon} ")
                    selectLocation(LatLng(it[0].lat, it[0].lon))
                    cameraPosition.value = CameraPosition.fromLatLngZoom(LatLng(it[0].lat, it[0].lon), 10f)
                }
            }
            clearPredictions()
        }
    }
    fun getCityName(latLng: LatLng){
        viewModelScope.launch {
            repo.getCityName(latLng.latitude, latLng.longitude)?.collect{
                if (it != null) {
                    _cityName.value = it[0].name
                }
            }
        }
    }


}
class MapFactory(private val repo: Repo, private val placesClient: PlacesClient): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(repo, placesClient) as T
    }
}