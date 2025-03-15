package com.mario.skyeye.navigation

sealed class ScreensRoutes{
    object HomeScreen: ScreensRoutes()
    object SettingsScreen: ScreensRoutes()
    object FavoritesScreen: ScreensRoutes()
    object WeatherAlertsScreen: ScreensRoutes()
}