package com.mario.skyeye.ui.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp,0.dp)) {

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
                Row(verticalAlignment = Alignment.CenterVertically,
                    ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "City: $cityName", style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        viewModel.saveLocation(selectedLocation)
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save City")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Favorites")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}