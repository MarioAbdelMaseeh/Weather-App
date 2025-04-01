package com.mario.skyeye.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.data.sharedprefrence.AppPreference
import com.mario.skyeye.navigation.ScreensRoutes.DetailsScreen
import com.mario.skyeye.navigation.ScreensRoutes.FavoritesScreen
import com.mario.skyeye.navigation.ScreensRoutes.HomeScreen
import com.mario.skyeye.navigation.ScreensRoutes.MapScreen
import com.mario.skyeye.navigation.ScreensRoutes.SettingsScreen
import com.mario.skyeye.navigation.ScreensRoutes.WeatherAlertsScreen
import com.mario.skyeye.feature.alert.viewmodel.WeatherAlertsFactory
import com.mario.skyeye.feature.alert.view.WeatherAlertsScreenUI
import com.mario.skyeye.feature.details.viewmodel.DetailsFactory
import com.mario.skyeye.feature.details.view.DetailsScreenUI
import com.mario.skyeye.feature.favorites.viewmodel.FavoritesFactory
import com.mario.skyeye.feature.favorites.view.FavoritesScreenUI
import com.mario.skyeye.feature.home.viewmodel.HomeFactory
import com.mario.skyeye.feature.home.view.HomeScreenUI
import com.mario.skyeye.feature.map.viewmodel.MapFactory
import com.mario.skyeye.feature.map.view.MapUi
import com.mario.skyeye.feature.settings.view.SettingsScreen
import com.mario.skyeye.feature.settings.viewmodel.SettingsViewModel
import com.mario.skyeye.utils.PlacesClientManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavHost(
    navHostController: NavHostController,
    showMap: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
    showNavBar: MutableState<Boolean>,
    icon: MutableState<ImageVector>,
    onFabClick: MutableState<() -> Unit>
){
    var buttonAction = false
    NavHost(
        navController = navHostController,
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            showMap.value = false
            showNavBar.value = true
            HomeScreenUI(viewModel(
                factory = HomeFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao(),
                        AppDataBase.getInstance(navHostController.context).alarmDao()),
                    AppPreference(LocalContext.current)))
            ))
        }
        composable<FavoritesScreen> {
            showMap.value = true
            showNavBar.value = true
            icon.value = Icons.Default.Map
            FavoritesScreenUI(viewModel(
                factory = FavoritesFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao(),
                        AppDataBase.getInstance(navHostController.context).alarmDao()),
                    AppPreference(LocalContext.current)))
            ), snackbarHostState,
                navToDetails = { location ->
                    buttonAction = true
                    navHostController.navigate(DetailsScreen(location))
                }
            )
            onFabClick.value = {
                buttonAction = false
                navHostController.navigate(MapScreen)
            }
        }
        composable<WeatherAlertsScreen> {
            showMap.value = true
            showNavBar.value = true
            icon.value = Icons.Default.Add
            WeatherAlertsScreenUI(
                viewModel(
                    factory = WeatherAlertsFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                        LocalDataSourceImpl(AppDataBase.getInstance(navHostController.context).weatherDao(),
                            AppDataBase.getInstance(navHostController.context).alarmDao()),
                        AppPreference(LocalContext.current)),
                        )
                ),onFabClick
            )
        }
        composable<SettingsScreen> {
            showMap.value = false
            showNavBar.value = true
            SettingsScreen(SettingsViewModel(
                RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),LocalDataSourceImpl(
                    AppDataBase.getInstance(LocalContext.current).weatherDao(),
                    AppDataBase.getInstance(LocalContext.current).alarmDao()),
                    AppPreference(LocalContext.current))
            )){
                buttonAction = true
                navHostController.navigate(MapScreen)
            }
        }
        composable<MapScreen> {
            showMap.value = false
            showNavBar.value = false
            MapUi(viewModel(
                factory = MapFactory(RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),
                    LocalDataSourceImpl(AppDataBase.getInstance(LocalContext.current).weatherDao(),
                        AppDataBase.getInstance(LocalContext.current).alarmDao()),
                    AppPreference(LocalContext.current)),
                    PlacesClientManager.getClient(LocalContext.current))
            ),navHostController,
                snackbarHostState,
                 buttonAction)
        }
        composable<DetailsScreen> {
            showMap.value = false
            showNavBar.value = false
            val detailsScreen : DetailsScreen = it.toRoute()
            DetailsScreenUI(
                viewModel(
                    factory = DetailsFactory(
                        RepoImpl.getInstance(
                            RemoteDataSourceImpl(RetrofitHelper.service),
                            LocalDataSourceImpl(
                                AppDataBase.getInstance(LocalContext.current).weatherDao(),
                                AppDataBase.getInstance(LocalContext.current).alarmDao()
                            ),
                            AppPreference(LocalContext.current)
                        )
                    )
                ),
                location = detailsScreen.favoriteLocation
            )
        }
    }
}