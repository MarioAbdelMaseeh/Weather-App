package com.mario.skyeye.ui.details


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.google.gson.Gson
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.ui.home.ErrorText
import com.mario.skyeye.ui.home.LoadingIndicator
import com.mario.skyeye.ui.home.WeatherContent
import com.mario.skyeye.ui.home.getWeatherBasedColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailsScreenUI(viewModel: DetailsViewModel, location: String) {
    val weatherData = viewModel.weatherDataState.collectAsState()
    val favoriteLocation = Gson().fromJson(location, FavoriteLocation::class.java)
    viewModel.fetchWeatherData(favoriteLocation.latitude, favoriteLocation.longitude, favoriteLocation.cityName)

    val dynamicColors = getWeatherBasedColors(weatherData.value)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.White
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    when (weatherData.value) {
                        is Response.Loading -> LoadingIndicator()
                        is Response.Success -> WeatherContent(
                            weatherData.value,
                            viewModel.tempUnit,
                            viewModel.windSpeedUnit,
                            dynamicColors
                        )
                        is Response.Failure -> ErrorText(weatherData.value as Response.Failure)
                    }
                }
            }
        }
    }
}


