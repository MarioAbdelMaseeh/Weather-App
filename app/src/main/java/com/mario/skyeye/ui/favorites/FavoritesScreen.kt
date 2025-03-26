package com.mario.skyeye.ui.favorites

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.locationState
import com.mario.skyeye.ui.WeatherIconMapper
import com.mario.skyeye.utils.getRelativeTime
import kotlinx.coroutines.delay


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritesScreenUI(
    viewModel: FavoritesViewModel,
    snackbarHostState: SnackbarHostState
) {
    val favoriteLocations by viewModel.favoriteLocations.collectAsStateWithLifecycle()
    viewModel.fetchFavoriteLocations()
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
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
                when (favoriteLocations) {
                    is Response.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,

                            ) {
                            CircularProgressIndicator()
                        }
                    }
                    is Response.Failure -> {
                        Text(text = "Error: ${(favoriteLocations as Response.Failure).error.toString()}", modifier = Modifier.padding(16.dp))
                    }
                    is Response.Success -> {
                        val locations = (favoriteLocations as Response.Success).data
                        if (locations?.isEmpty() == true) {
                            Text(text = "No saved locations", modifier = Modifier.padding(16.dp))
                        }else{
                            val favoriteLocations = (favoriteLocations as Response.Success).data
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp, 8.dp)
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
        }
    }

@Composable
fun FavoriteLocationItem(location: FavoriteLocation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                onClick = {
                    locationState.value.latitude = location.latitude
                    locationState.value.longitude = location.longitude
                }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = WeatherIconMapper.getWeatherIcon(location.currentWeatherResponse.weather[0].icon)), contentDescription = "Weather Icon", modifier = Modifier.size(100.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = location.cityName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Current Weather: ${location.currentWeatherResponse.weather[0].description}",
                    fontSize = 16.sp
                )
                Text(text = getRelativeTime(location.currentWeatherResponse.dt, LocalContext.current), fontSize = 16.sp)
            }
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
                withDismissAction = true,
                duration = SnackbarDuration.Short
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
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(durationMillis = animationDuration)
        )+ fadeIn(),
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


