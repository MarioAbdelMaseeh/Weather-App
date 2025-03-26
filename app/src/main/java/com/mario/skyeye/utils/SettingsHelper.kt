package com.mario.skyeye.utils

fun getUnitType(tempUnit: String): String {
    return when(tempUnit){
        "°C" -> "metric"
        "°F" -> "imperial"
        "°K" -> "standard"
        else -> "metric"
    }
}