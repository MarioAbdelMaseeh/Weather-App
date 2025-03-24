package com.mario.skyeye.ui.favorites

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.mario.skyeye.locationState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritesScreenUI(viewModel: FavoritesViewModel, navToMap: () -> Unit){
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navToMap()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ){
            Column(
                modifier = Modifier.fillMaxWidth(),
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = "Location",modifier = Modifier.size(40.dp))
                    Text(
                        text = "Saved Locations",
                        modifier = Modifier.padding(16.dp,8.dp),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LazyColumn {
//                    items(viewModel.savedLocations.size) { index ->
//                        val location = viewModel.savedLocations[index]
//                        Text(text = location.cityName)
//                    }
                }
            }
        }

    }
}

