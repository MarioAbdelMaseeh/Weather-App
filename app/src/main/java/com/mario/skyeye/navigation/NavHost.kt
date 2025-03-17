package com.mario.skyeye.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.navigation.ScreensRoutes.FavoritesScreen
import com.mario.skyeye.navigation.ScreensRoutes.HomeScreen
import com.mario.skyeye.navigation.ScreensRoutes.SettingsScreen
import com.mario.skyeye.navigation.ScreensRoutes.WeatherAlertsScreen
import com.mario.skyeye.ui.favorites.FavoritesFactory
import com.mario.skyeye.ui.favorites.FavoritesScreenUI
import com.mario.skyeye.ui.home.HomeFactory
import com.mario.skyeye.ui.home.HomeScreenUI

@Composable
fun SetupNavHost(navHostController: NavHostController){
    NavHost(
        navController = navHostController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreenUI(viewModel(
                factory = HomeFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service)))
            ))
        }
        composable<FavoritesScreen> {
            FavoritesScreenUI(viewModel(
                factory = FavoritesFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service)))
            ))
        }
        composable<WeatherAlertsScreen> {
            //WeatherAlertsScreenUI()
        }
        composable<SettingsScreen> {

        }
    }

}