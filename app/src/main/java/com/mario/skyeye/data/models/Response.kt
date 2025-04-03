package com.mario.skyeye.data.models

sealed class Response<out T> {
    data object Loading : Response<Nothing>()
    data class Success<T>(val data: T) : Response<T>()
    data class Failure(val error: String) : Response<Nothing>()
}