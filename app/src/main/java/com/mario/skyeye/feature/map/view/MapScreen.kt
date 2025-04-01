package com.mario.skyeye.feature.map.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace
import com.google.android.libraries.places.compose.autocomplete.models.toPlaceDetails
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.mario.skyeye.R
import com.mario.skyeye.enums.MapHelper
import com.mario.skyeye.feature.map.viewmodel.MapViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun MapUi(viewModel: MapViewModel, navController: NavController, snackbarHostState: SnackbarHostState, buttonAction: Boolean) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val predictions by viewModel.predictions.collectAsStateWithLifecycle()
    val cityName by viewModel.cityName.collectAsStateWithLifecycle()
    val cameraPositionState = rememberCameraPositionState {
        position = viewModel.cameraPositionState.value
    }
    val markerState = rememberMarkerState(
        position = LatLng(
            selectedLocation.latitude,
            selectedLocation.longitude
        )
    )
    LaunchedEffect(selectedLocation) {
        selectedLocation.let {
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
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start
        ){
            // Back Button (Top-Left Corner)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(8.dp, 32.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(1.dp, Color.Gray, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Search Bar (Autocomplete TextField)
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                PlacesAutocompleteTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    searchText = searchQuery,
                    predictions = predictions.map { it.toPlaceDetails() },
                    onQueryChanged = { viewModel.updateSearchQuery(it) },
                    onSelected = { place: AutocompletePlace ->
                        Log.i("TAG", "MapUi: ${place.primaryText}, ${place.secondaryText}")
                        viewModel.getCoordinates("${place.primaryText}, ${place.secondaryText}")
                        viewModel.updateCityName("${place.primaryText}, ${place.secondaryText}")
                    },
                )
            }

        }

        // Bottom Card with Location Info & Buttons
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(320.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Location Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.city, cityName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                // Buttons (Save to Favorites)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (buttonAction) {
                                viewModel.setDefaultLocation(selectedLocation)
                                viewModel.updatePreference("location", MapHelper.MAP.mapType)


                            }else
                            viewModel.saveLocation(selectedLocation)
                                  },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save City")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (buttonAction) stringResource(R.string.set_as_default_location) else
                            stringResource(R.string.add_to_favorites)
                        )
                    }
                }
            }
        }
    }
}
