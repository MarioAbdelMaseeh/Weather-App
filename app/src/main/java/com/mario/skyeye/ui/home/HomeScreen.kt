package com.mario.skyeye.ui.home

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.mario.skyeye.R
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreenUI(viewModel: HomeViewModel){
    val response = viewModel.currentWeatherResponse.observeAsState()
    viewModel.getCurrentWeather()
    Log.i("TAG", "HomeScreenUI:${response.value?.name} ")
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
                        text = response.value?.name ?: "No Data",
                        color = colorResource(id = R.color.black),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth()
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GlideImage(
                                    model = "https://openweathermap.org/img/wn/${response.value?.weather?.get(0)?.icon}@2x.png",
                                    contentDescription = "Weather Icon",
                                    modifier = Modifier.size(150.dp)
                                )
                                var myDate = Date()
                                Text(
                                    text = DateFormat.getDateInstance(DateFormat.FULL).format(myDate),
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = response.value?.weather?.get(0)?.description ?: "No Data",
                                    color = colorResource(id = R.color.black),
                                    fontSize = 20.sp,
                                )

                                Text(
                                    text = "${response.value?.main?.temp?.minus(273)?.toInt() ?: "No Data"} °C",
                                    color = colorResource(id = R.color.black),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxSize(),
                                    textAlign = TextAlign.Center

                                )
                                Text(
                                    text = "Feels like ${response.value?.main?.feelsLike?.minus(273)?.toInt() ?: "No Data"} °C",
                                    color = colorResource(id = R.color.black),
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
