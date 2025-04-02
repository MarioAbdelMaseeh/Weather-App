package com.mario.skyeye.features.details.view


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.google.gson.Gson
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherData
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.enums.TempUnit.Companion.fromUnitType
import com.mario.skyeye.features.details.viewmodel.DetailsViewModel
import com.mario.skyeye.features.home.view.CurrentWeatherBox
import com.mario.skyeye.features.home.view.ErrorText
import com.mario.skyeye.features.home.view.ForecastSection
import com.mario.skyeye.features.home.view.LoadingIndicator
import com.mario.skyeye.features.home.view.TimeBox
import com.mario.skyeye.features.home.view.WeatherDetailsBox
import com.mario.skyeye.utils.WeatherColors
import com.mario.skyeye.utils.getWeatherBasedColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailsScreenUI(
    viewModel: DetailsViewModel,
    location: String,
    onBackPressed: () -> Unit
) {
    val weatherData = viewModel.weatherDataState.collectAsState()
    val favoriteLocation = remember { Gson().fromJson(location, FavoriteLocation::class.java) }

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherData(
            favoriteLocation.latitude,
            favoriteLocation.longitude,
            favoriteLocation.cityName
        )
    }

    val dynamicColors = getWeatherBasedColors(weatherData.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Custom Header with Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }

                    Text(
                        text = favoriteLocation.cityName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Weather Content
            when (weatherData.value) {
                is Response.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        LoadingIndicator()
                    }
                }
                is Response.Success -> {
                    WeatherContent(
                        weatherData.value,
                        viewModel.tempUnit,
                        viewModel.windSpeedUnit,
                        dynamicColors
                    )
                }
                is Response.Failure -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        ErrorText(weatherData.value as Response.Failure)
                    }
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherContent(
    weatherData: Response<WeatherData?>,
    tempUnit: String,
    windSpeedUnit: String,
    colors: WeatherColors
) {
    val currentWeatherResponse =
        (weatherData as Response.Success<WeatherData?>).data?.currentWeatherResponse

    Column {
        CurrentWeatherBox(
            currentWeatherResponse,
            fromUnitType(tempUnit)?.getTempSymbol() ?: TempUnit.METRIC.getTempSymbol(),
            colors
        )
        TimeBox(colors, currentWeatherResponse)
        WeatherDetailsBox(currentWeatherResponse, windSpeedUnit, colors)
        ForecastSection(weatherData, tempUnit, colors)
    }
}

//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalGlideComposeApi::class)
//@Composable
//fun DetailsScreenUI(viewModel: DetailsViewModel, location: String) {
//    val weatherData = viewModel.weatherDataState.collectAsState()
//    val favoriteLocation = Gson().fromJson(location, FavoriteLocation::class.java)
//    viewModel.fetchWeatherData(favoriteLocation.latitude, favoriteLocation.longitude, favoriteLocation.cityName)
//
//    val dynamicColors = getWeatherBasedColors(weatherData.value)
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                color = Color.White
//            )
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                item {
//                    when (weatherData.value) {
//                        is Response.Loading -> LoadingIndicator()
//                        is Response.Success -> WeatherContent(
//                            weatherData.value,
//                            viewModel.tempUnit,
//                            viewModel.windSpeedUnit,
//                            dynamicColors
//                        )
//                        is Response.Failure -> ErrorText(weatherData.value as Response.Failure)
//                    }
//                }
//            }
//        }
//    }
//}
//
//
