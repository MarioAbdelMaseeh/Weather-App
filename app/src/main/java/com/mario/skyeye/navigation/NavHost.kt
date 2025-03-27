package com.mario.skyeye.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.libraries.places.api.Places
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.data.sharedprefrence.AppPreference
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
import com.mario.skyeye.ui.settings.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavHost(
    navHostController: NavHostController,
    showMap: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
    showNavBar: MutableState<Boolean>
){
    NavHost(
        navController = navHostController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            showMap.value = false
            showNavBar.value = true
            HomeScreenUI(viewModel(
                factory = HomeFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao()),
                    AppPreference(LocalContext.current)))
            ))
        }
        composable<FavoritesScreen> {
            showMap.value = true
            showNavBar.value = true
            FavoritesScreenUI(viewModel(
                factory = FavoritesFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao()),
                    AppPreference(LocalContext.current)))
            ), snackbarHostState )
        }
        composable<WeatherAlertsScreen> {
            showMap.value = false
            showNavBar.value = true
            //WeatherAlertsScreenUI()
        }
        composable<SettingsScreen> {
            showMap.value = false
            showNavBar.value = true
            SettingsScreen(SettingsViewModel(
                RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),LocalDataSourceImpl(AppDataBase.getInstance(LocalContext.current).weatherDao()), AppPreference(LocalContext.current))
            ))
        }
        composable<MapScreen> {
            showMap.value = false
            showNavBar.value = false
            MapUi(viewModel(
                factory = MapFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao()),
                    AppPreference(LocalContext.current)),
                    Places.createClient(navHostController.context))
            ),navHostController)

        }
    }

}