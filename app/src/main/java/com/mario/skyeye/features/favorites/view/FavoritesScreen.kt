package com.mario.skyeye.features.favorites.view

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.gson.Gson
import com.mario.skyeye.R
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.features.favorites.viewmodel.FavoritesViewModel
import com.mario.skyeye.utils.WeatherIconMapper
import com.mario.skyeye.utils.getRelativeTime
import kotlinx.coroutines.delay


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritesScreenUI(
    viewModel: FavoritesViewModel,
    snackbarHostState: SnackbarHostState,
    navToDetails: (String) -> Unit
) {
    val favoriteLocations by viewModel.favoriteLocations.collectAsStateWithLifecycle()
    viewModel.fetchFavoriteLocations()
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                            text = stringResource(R.string.saved_locations),
                            style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(R.string.manage_your_saved_locations),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                }
                when (favoriteLocations) {
                    is Response.Loading -> {
                        AnimationLoading()
                    }
                    is Response.Failure -> {
                        Text(text = "Error: ${(favoriteLocations as Response.Failure).error}", modifier = Modifier.padding(16.dp))
                    }
                    is Response.Success -> {
                        val locations = (favoriteLocations as Response.Success).data
                        if (locations?.isEmpty() == true) {
                            AnimationLoading()
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
                                        SwipeToDeleteContainer(item = it, onDelete = { viewModel.deleteLocation(it) }, snackbarHostState = snackbarHostState,
                                            onRestore = { } ) { location ->
                                            FavoriteLocationItem(viewModel = viewModel,location = location, navToDetails = navToDetails)
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
fun FavoriteLocationItem(viewModel: FavoritesViewModel, location: FavoriteLocation, navToDetails: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                onClick = {
                    val gson = Gson()
                    val json = gson.toJson(location)
                    navToDetails(json)
                }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(id = WeatherIconMapper.getWeatherIcon(location.currentWeatherResponse.weather[0].icon)),
                contentDescription = stringResource(R.string.weather_icon),
                modifier = Modifier.size(100.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = location.cityName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(
                        R.string.current_weather,
                        location.currentWeatherResponse.weather[0].description
                    ),
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
    val context = LocalContext.current
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
                message = context.getString(R.string.location_deleted),
                actionLabel = context.getString(R.string.undo),
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
                contentDescription = stringResource(R.string.delete_icon),
                tint = Color.White
            )
        }
    }
}
@Composable
private fun AnimationLoading() {
    LottieAnimation(
        composition = rememberLottieComposition(
            LottieCompositionSpec.RawRes(
                R.raw.location_lottie
            )
        ).value,
        speed = 1f,
        isPlaying = true,
    )
}


