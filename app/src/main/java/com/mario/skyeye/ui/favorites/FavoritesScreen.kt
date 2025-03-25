package com.mario.skyeye.ui.favorites

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.coroutines.delay


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritesScreenUI(viewModel: FavoritesViewModel, navToMap: () -> Unit) {
    val favoriteLocations by viewModel.favoriteLocations.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.fetchFavoriteLocations()
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navToMap()
                },
                shape = androidx.compose.foundation.shape.CircleShape,
                containerColor = Color(0xFF007AFF),
                modifier = Modifier.size(70.dp)
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = "Add")
            }
        }
        ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = "Location",
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Saved Locations",
                        modifier = Modifier.padding(16.dp, 8.dp),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp, 0.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp)
                ) {

                    items(favoriteLocations?.size ?: 0,
                        key = { index -> favoriteLocations?.get(index)?.cityName ?: "" }) { index ->
                        val location = favoriteLocations?.get(index)
                        location?.let {
                            SwipeToDeleteContainer(item = it, onDelete = { viewModel.deleteLocation(it) }, snackbarHostState = snackbarHostState, onRestore = { viewModel.fetchFavoriteLocations()} ) { location ->
                                FavoriteLocationItem(location = location)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteLocationItem(location: FavoriteLocation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),

        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = location.cityName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Latitude: ${location.latitude}", fontSize = 16.sp)
            Text(text = "Longitude: ${location.longitude}", fontSize = 16.sp)
            Text(
                text = "Current Weather: ${location.currentWeatherResponse.weather[0].description}",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    animationDuration: Int = 500,
    onRestore: (T) -> Unit = {},
    snackbarHostState: SnackbarHostState,
    content: @Composable (T) -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    var canSwipe by remember { mutableStateOf(true) }
    val swipeToDismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                isDeleted = true
                canSwipe = false
                true
            } else {
                false
            }
        }
    )
    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            val result = snackbarHostState.showSnackbar(
                message = "Location deleted",
                actionLabel = "Undo",
                withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed) {
                onRestore(item)
                canSwipe = true
                isDeleted = false
            } else {
                delay(animationDuration.toLong())
                onDelete(item)
            }
        }
    }
    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = animationDuration)
        ) + fadeOut()
    ) {
        if (canSwipe){
            SwipeToDismissBox(
                state = swipeToDismissState,
                backgroundContent = { DeleteLocationBackground(swipeToDismissState) },
                enableDismissFromStartToEnd = false
            ) {
                content(item)
            }
        }else{
            LaunchedEffect(Unit) {
                swipeToDismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            content(item)
        }

    }

}

@Composable
fun DeleteLocationBackground(
    swipeDismissState: SwipeToDismissBoxState
) {
    val color = if (swipeDismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
        Color.Red
    } else {
        Color.Transparent
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Icon",
                tint = Color.White
            )
        }
    }

}


