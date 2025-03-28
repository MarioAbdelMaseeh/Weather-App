package com.mario.skyeye.navigation

import com.mario.skyeye.data.models.FavoriteLocation
import kotlinx.serialization.Serializable
@Serializable
sealed class ScreensRoutes(val route: String) {
    @Serializable
    object HomeScreen : ScreensRoutes("home")
    @Serializable
    object SettingsScreen : ScreensRoutes("settings")
    @Serializable
    object FavoritesScreen : ScreensRoutes("favorites")
    @Serializable
    object WeatherAlertsScreen : ScreensRoutes("weather_alerts")
    @Serializable
    object MapScreen : ScreensRoutes("map")
    @Serializable
    class DetailsScreen(val favoriteLocation: String) : ScreensRoutes("details")
}