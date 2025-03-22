package com.mario.skyeye.navigation

import kotlinx.serialization.Serializable

sealed class ScreensRoutes(){
    @Serializable
    object HomeScreen: ScreensRoutes()
    @Serializable
    object SettingsScreen: ScreensRoutes()
    @Serializable
    object FavoritesScreen: ScreensRoutes()
    @Serializable
    object WeatherAlertsScreen: ScreensRoutes()
}