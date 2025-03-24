package com.mario.skyeye.ui.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.autocomplete.models.toPlaceDetails
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.mario.skyeye.locationState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun MapUi(viewModel: MapViewModel, context: Context) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val predictions by viewModel.predictions.collectAsStateWithLifecycle()
    val cityName by viewModel.cityName.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = viewModel.cameraPositionState.value
    }
    val markerState = rememberMarkerState(
        position = LatLng(
            locationState.value.latitude,
            locationState.value.longitude
        )
    )
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            markerState.position = it
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 10f))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                viewModel.selectLocation(latLng)
                viewModel.getCityName(latLng)
            }
        ) {
            Marker(state = markerState)
        }

        // Search Box
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(8.dp),
            ) {
                PlacesAutocompleteTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    searchText = searchQuery,
                    predictions = predictions.map { it.toPlaceDetails() },
                    onQueryChanged = { viewModel.updateSearchQuery(it) },
                    onSelected = { place: AutocompletePlace ->
                        viewModel.getCoordinates("${place.primaryText}, ${place.secondaryText ?: ""}")
                        viewModel.updateCityName("${place.primaryText}, ${place.secondaryText ?: ""}")
                    },
                )
            }
        }

        // City & Coordinates Info
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "City: $cityName", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraPositionState,
//        onMapClick = { latLng ->
//            viewModel.selectLocation(latLng)
//            viewModel.getCityName(latLng)
//        }
//    ) {
//        Marker(
//            state = markerState
//        )
//    }
//    PlacesAutocompleteTextField(
//        modifier = Modifier.fillMaxWidth(),
//        searchText = searchQuery,
//        predictions = predictions.map { it.toPlaceDetails() },
//        onQueryChanged = {
//            viewModel.updateSearchQuery(it)
//        },
//        onSelected = { autocompletePlace: AutocompletePlace ->
//            viewModel.getCoordinates(autocompletePlace.primaryText.toString()+","+autocompletePlace.secondaryText.toString())
//            viewModel.updateCityName(autocompletePlace.primaryText.toString()+","+autocompletePlace.secondaryText.toString())
//        },
//    )
//    Card {
//        Text(text = "City Name: $cityName")
//        Text(text = "Selected Location: ${selectedLocation?.latitude}, ${selectedLocation?.longitude}")
//    }
//
//}