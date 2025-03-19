package com.mario.skyeye.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mario.skyeye.R
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.locationState
import com.mario.skyeye.ui.WeatherIconMapper
import java.text.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreenUI(viewModel: HomeViewModel){
    val response = viewModel.currentWeatherState.collectAsState()
    viewModel.getCurrentWeather(locationState.value.latitude, locationState.value.longitude)
    when(response.value){
        is Response.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Response.Success -> {
            val currentWeatherResponse = (response.value as Response.Success<CurrentWeatherResponse?>).data
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(id = R.color.white),
                                colorResource(id = R.color.teal_700)
                            )
                        )
                    )
            )
            {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        item {
                            Text(
                                text = currentWeatherResponse?.name.toString(),
                                color = colorResource(id = R.color.black),
                                fontSize = 24.sp,
                                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxSize(),
                                textAlign = TextAlign.Center
                            )
                            CurrentWeatherBox(currentWeatherResponse)
                            Spacer(modifier = Modifier.size(16.dp))
                            WeatherDetailsBox(currentWeatherResponse)
                        }
                    }
                }
            }

        }
        is Response.Failure -> {
            Text(text = "Error: ${(response.value as Response.Failure).error.message}")
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDetailsBox(x0: CurrentWeatherResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.teal_700),
                        colorResource(id = R.color.teal_700)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ){
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start){
                SmallBox(R.drawable.snow_day, "Humidity", x0?.main?.humidity.toString(),"%")
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(R.drawable.th_day, "Wind Speed", x0?.wind?.speed.toString(),"m/s")
            }
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start) {
                SmallBox(
                    R.drawable.very_cloudy_day,
                    "Pressure",
                    x0?.main?.pressure.toString(),
                    "hPa"
                )
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(R.drawable.cloudy_day, "Visibility", x0?.visibility.toString(),"m")
            }
            Column {

                val riseTime = getHourFormTime(x0?.sys?.sunrise?.toLong() ?: 0)
                val setTime = getHourFormTime(x0?.sys?.sunset?.toLong() ?: 0)
                SmallBox(R.drawable.base_sun, "Sunrise", riseTime.toString(),"")
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(R.drawable.base_sun, "Sunset", setTime.toString(),"")
            }
        }
    }
}

@Composable
private fun SmallBox(icon: Int?, name: String?, value: String?, measuringUnit: String?) {
    Box {
        Row {
            Image(
                painter = painterResource(icon ?: R.drawable.snow_day),
                contentDescription = name,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(text = name ?: "N/A", color = colorResource(id = R.color.black), fontSize = 12.sp)
                Text(
                    text = "${value ?: "No Data"} ${measuringUnit ?: "N/A"}",
                    color = colorResource(id = R.color.black),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherBox(response: CurrentWeatherResponse?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colorResource(id = R.color.teal_700),
                        colorResource(id = R.color.teal_700)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(
                        id = WeatherIconMapper.getWeatherIcon(
                            response?.weather?.get(
                                0
                            )?.icon ?: "01d"
                        )
                    ),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(150.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = response?.weather?.get(0)?.description ?: "No Data",
                    color = colorResource(id = R.color.black),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = "${response?.main?.temp?.toInt() ?: "No Data"} °C",
                    color = colorResource(id = R.color.black),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center

                )
                Text(
                    text = "Feels like ${
                        response?.main?.feelsLike?.toInt() ?: "No Data"
                    } °C",
                    color = colorResource(id = R.color.black),
                    fontSize = 16.sp,
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                var myDate = Date()
                Text(
                    text = DateFormat.getDateInstance(DateFormat.FULL).format(myDate),
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun getHourFormTime(timestamp: Long): String {
    val time = Instant.ofEpochSecond(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("hh:mm a"))
}
