package com.mario.skyeye.ui.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.mario.skyeye.locationState

@Composable
fun FavoritesScreenUI(viewModel: FavoritesViewModel){
    val currentLocation = LatLng(locationState.value.latitude, locationState.value.longitude)
    val markerState = rememberMarkerState(position = currentLocation)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerState.position = latLng
        }
    ) {
        Marker(
            state = markerState
        )
    }
}