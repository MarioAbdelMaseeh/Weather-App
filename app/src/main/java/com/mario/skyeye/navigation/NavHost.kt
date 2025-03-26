package com.mario.skyeye.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.libraries.places.api.Places
import com.mario.skyeye.BuildConfig
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.navigation.ScreensRoutes.FavoritesScreen
import com.mario.skyeye.navigation.ScreensRoutes.HomeScreen
import com.mario.skyeye.navigation.ScreensRoutes.MapScreen
import com.mario.skyeye.navigation.ScreensRoutes.SettingsScreen
import com.mario.skyeye.navigation.ScreensRoutes.WeatherAlertsScreen
import com.mario.skyeye.ui.favorites.FavoritesFactory
import com.mario.skyeye.ui.favorites.FavoritesScreenUI
import com.mario.skyeye.ui.home.HomeFactory
import com.mario.skyeye.ui.home.HomeScreenUI
import com.mario.skyeye.ui.map.MapFactory
import com.mario.skyeye.ui.map.MapUi
import com.mario.skyeye.ui.settings.SettingsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavHost(navHostController: NavHostController){
    NavHost(
        navController = navHostController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreenUI(viewModel(
                factory = HomeFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao())))
            ))
        }
        composable<FavoritesScreen> {
            FavoritesScreenUI(viewModel(
                factory = FavoritesFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao())))
            ),
                navToMap = { navHostController.navigate(MapScreen) })
        }
        composable<WeatherAlertsScreen> {
            //WeatherAlertsScreenUI()
        }
        composable<SettingsScreen> {
            SettingsScreen()
        }
        composable<MapScreen> {
            Places.initializeWithNewPlacesApiEnabled(navHostController.context, BuildConfig.MAPS_API_KEY)
            MapUi(viewModel(
                factory = MapFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao())),
                    Places.createClient(navHostController.context))
            ),context = navHostController.context)

        }
    }

}