package com.mario.skyeye.features.home.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.mario.skyeye.R
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherData
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.models.forecastDaysHelper
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.enums.TempUnit.Companion.fromUnitType
import com.mario.skyeye.features.home.viewmodel.HomeViewModel
import com.mario.skyeye.utils.WeatherIconMapper
import com.mario.skyeye.utils.LanguageManager
import com.mario.skyeye.utils.WeatherColors
import com.mario.skyeye.utils.getDayName
import com.mario.skyeye.utils.getHourFormTime
import com.mario.skyeye.utils.getRelativeTime
import com.mario.skyeye.utils.getWeatherBasedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreenUI(viewModel: HomeViewModel) {
    val weatherData = viewModel.weatherDataState.collectAsState()
    val locationState = viewModel.locationState.collectAsState()
    viewModel.locationChangeListener()
    viewModel.getLocation()

    LaunchedEffect(locationState.value) {
        if (locationState.value.latitude != 0.0 && locationState.value.longitude != 0.0) {
            viewModel.fetchWeatherData(locationState.value.latitude, locationState.value.longitude)
        }
    }
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
                        is Response.Loading -> AnimationLoading()
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

@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ForecastSection(
    value: Response<WeatherData?>,
    tempUnit: String,
    colors: WeatherColors
) {
    val weatherForecastResponse =
        (value as Response.Success<WeatherData>).data.forecastResponse
    val forecastDays = weatherForecastResponse?.forecastDaysHelper()
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.hourly_forecast),
            color = colorResource(id = R.color.black),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp)
        )
        LazyRow(
            modifier = Modifier.padding(8.dp)
        ) {
            item {
                forecastDays?.entries?.first()?.value?.forEach { forecast ->

                    HourlyForecastItem(
                        forecast,
                        fromUnitType(tempUnit)?.getTempSymbol()
                            ?: TempUnit.METRIC.getTempSymbol(),
                        colors
                    )
                }
                forecastDays?.entries?.elementAt(1)
                    ?.value?.forEach { forecast ->
                        HourlyForecastItem(
                            forecast,
                            fromUnitType(tempUnit)?.getTempSymbol()
                                ?: TempUnit.METRIC.getTempSymbol(),
                            colors
                        )
                    }
            }
        }
    }
    Text(
        text = stringResource(R.string.weekly_forecast),
        color = colorResource(id = R.color.black),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(16.dp, 8.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Start
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.light,
                        colors.dark
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            forecastDays?.entries?.forEach { (date, forecastList) ->
                ForecastDay(
                    date, forecastList,
                    fromUnitType(tempUnit)?.getTempSymbol()
                        ?: TempUnit.METRIC.getTempSymbol(),
                    colors.textColor
                )
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
        LocationHeader(currentWeatherResponse)
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

@Composable
fun LocationHeader(weather: CurrentWeatherResponse?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.marker),
            contentDescription = stringResource(R.string.location_icon),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = weather?.name.orEmpty(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ErrorText(error: Response.Failure) {
    Text(
        text = "Error: ${error.error}",
        color = Color.Red,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HourlyForecastItem(x0: WeatherForecast.Item0, unit: String, colors: WeatherColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(100.dp, 200.dp)
            .padding(8.dp, 8.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.light,
                        colors.dark
                    )
                ),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val myDate = Date(x0.dt * 1000L)
            val dateFormat = SimpleDateFormat("EE", Locale.getDefault())
            Text(
                text = dateFormat.format(myDate),
                color = colors.textColor,
                fontSize = 16.sp,

                )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = LanguageManager.formatNumberBasedOnLanguage(getHourFormTime(x0.dt.toLong())),
                color = colors.textColor,
                fontSize = 16.sp,
            )
            Image(
                painter = painterResource(
                    id = WeatherIconMapper.getWeatherIcon(
                        x0.weather[0].icon
                    )
                ),
                contentDescription = stringResource(R.string.weather_icon),
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = "${
                    LanguageManager.formatNumberBasedOnLanguage(
                        x0.main.temp.toInt().toString()
                    )
                }$unit",
                color = colors.textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ForecastDay(
    date: Int,
    forecastList: List<WeatherForecast.Item0>,
    unit: String,
    textColor: Color
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = getDayName(date.toString()),
            color = textColor,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(8.dp)
                .width(100.dp)
                .clickable(
                    enabled = true,
                    interactionSource = null,
                    indication = null,
                    onClick = {

                    }
                ),
            textAlign = TextAlign.Start,
        )
        val minTemp = forecastList.minOf { it.main.temp.toInt() }
        val maxTemp = forecastList.maxOf { it.main.temp.toInt() }
        val indexOfMinTemp = forecastList.indexOfFirst { it.main.temp.toInt() == minTemp }
        val indexOfMaxTemp = forecastList.indexOfFirst { it.main.temp.toInt() == maxTemp }
        Image(
            painter = painterResource(
                R.drawable.humidity
            ),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(15.dp),
            colorFilter = ColorFilter.tint(textColor)
        )
        Text(
            text = "${LanguageManager.formatNumberBasedOnLanguage(forecastList[indexOfMaxTemp].main.humidity.toString())} %",
            color = textColor,
        )
        Spacer(modifier = Modifier.size(32.dp))
        Image(
            painter = painterResource(
                id = WeatherIconMapper.getWeatherIcon(
                    forecastList[indexOfMaxTemp].weather[0].icon
                )
            ),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(30.dp)
        )
        Image(
            painter = painterResource(
                id = WeatherIconMapper.getWeatherIcon(
                    forecastList[indexOfMinTemp].weather[0].icon
                )
            ),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "${LanguageManager.formatNumberBasedOnLanguage(maxTemp.toString())}/${
                LanguageManager.formatNumberBasedOnLanguage(
                    minTemp.toString()
                )
            }$unit",
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDetailsBox(
    x0: CurrentWeatherResponse?,
    windSpeedUnit: String,
    colors: WeatherColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.light,
                        colors.dark
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                SmallBox(
                    R.drawable.humidity,
                    stringResource(R.string.humidity),
                    LanguageManager.formatNumberBasedOnLanguage(x0?.main?.humidity.toString()),
                    "%", colors.textColor
                )
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(
                    R.drawable.wind,
                    stringResource(R.string.wind_speed),
                    LanguageManager.formatNumberBasedOnLanguage(x0?.wind?.speed.toString()),
                    fromUnitType(windSpeedUnit)?.getWindSymbol()
                        ?: TempUnit.METRIC.getWindSymbol(), colors.textColor
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                SmallBox(
                    R.drawable.clouds,
                    stringResource(R.string.cloudiness),
                    LanguageManager.formatNumberBasedOnLanguage(x0?.clouds?.all.toString()),
                    "%",
                    colors.textColor
                )
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(
                    R.drawable.pressure,
                    stringResource(R.string.pressure),
                    LanguageManager.formatNumberBasedOnLanguage(x0?.main?.pressure.toString()),
                    stringResource(R.string.hpa),
                    colors.textColor
                )
//                SmallBox(
//                    R.drawable.eye,
//                    stringResource(R.string.visibility),
//                    LanguageManager.formatNumberBasedOnLanguage(x0?.visibility.toString()),
//                    stringResource(R.string.m),
//                    colors.textColor
//                )
            }
            Column {

                val riseTime = getHourFormTime(x0?.sys?.sunrise?.toLong() ?: 0)
                val setTime = getHourFormTime(x0?.sys?.sunset?.toLong() ?: 0)
                SmallBox(
                    R.drawable.sunrise_alt,
                    stringResource(R.string.sunrise),
                    LanguageManager.formatNumberBasedOnLanguage(riseTime.toString()),
                    "",
                    colors.textColor
                )
                Spacer(modifier = Modifier.size(8.dp))
                SmallBox(
                    R.drawable.sunset,
                    stringResource(R.string.sunset),
                    LanguageManager.formatNumberBasedOnLanguage(setTime.toString()),
                    "",
                    colors.textColor
                )
            }
        }
    }
}

@Composable
fun SmallBox(
    icon: Int?,
    name: String?,
    value: String?,
    measuringUnit: String?,
    textColor: Color
) {
    Box {
        Row {
            Image(
                painter = painterResource(icon ?: R.drawable.snow_day),
                contentDescription = name,
                modifier = Modifier.size(25.dp),
                colorFilter = ColorFilter.tint(textColor)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = name ?: "N/A",
                    color = textColor,
                    fontSize = 12.sp
                )
                Text(
                    text = "${value ?: "No Data"} ${measuringUnit ?: "N/A"}",
                    color = textColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable

fun CurrentWeatherBox(
    response: CurrentWeatherResponse?,
    unit: String,
    colors: WeatherColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.light,
                        colors.dark
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
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
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = response?.weather?.get(0)?.description ?: "No Data",
                    color = colors.textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = "${LanguageManager.formatNumberBasedOnLanguage((response?.main?.temp?.toInt() ?: "No Data").toString())}${unit}",
                    color = colors.textColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
                Text(
                    text = stringResource(
                        R.string.feels_like,
                        LanguageManager.formatNumberBasedOnLanguage(
                            (response?.main?.feelsLike?.toInt() ?: "No Data").toString()
                        ),
                        unit
                    ),
                    color = colors.textColor,
                    fontSize = 16.sp
                )
                Spacer(
                    modifier = Modifier.size(8.dp)
                )
            }
            Image(
                painter = painterResource(
                    id = WeatherIconMapper.getWeatherIcon(
                        response?.weather?.get(0)?.icon ?: "01d"
                    )
                ),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(150.dp)
            )
        }
    }
}

@Composable
fun TimeBox(
    colors: WeatherColors,
    currentWeatherResponse: CurrentWeatherResponse?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.light,
                        colors.dark
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val myDate = Date()
            val dateFormat = SimpleDateFormat("EEEE, hh:mm a", Locale.getDefault())
            Text(
                text = dateFormat.format(myDate),
                color = colors.textColor,
                fontSize = 12.sp,

                )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(
                    R.string.last_updated,
                    getRelativeTime(currentWeatherResponse?.dt ?: 0, LocalContext.current)
                ),
                color = colors.textColor,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
@Composable
fun AnimationLoading() {
    LottieAnimation(
        composition = rememberLottieComposition(
            LottieCompositionSpec.RawRes(
                R.raw.weather2_lottie
            )
        ).value,
        speed = 2f,
        isPlaying = true,
    )
}

