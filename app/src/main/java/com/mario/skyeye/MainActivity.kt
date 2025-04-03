package com.mario.skyeye

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mario.skyeye.enums.Languages
import com.mario.skyeye.enums.MapHelper
import com.mario.skyeye.navigation.BottomNavigationItem
import com.mario.skyeye.navigation.ScreensRoutes
import com.mario.skyeye.navigation.ScreensRoutes.SettingsScreen
import com.mario.skyeye.navigation.SetupNavHost
import com.mario.skyeye.utils.Constants
import com.mario.skyeye.utils.Constants.REQUEST_CODE_LOCATION
import com.mario.skyeye.utils.NetworkMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    lateinit var showMap : MutableState<Boolean>
    lateinit var banner : MutableState<Boolean>
    lateinit var message : MutableState<String>
    lateinit var snackbarHostState: SnackbarHostState
    lateinit var showNavBar : MutableState<Boolean>
    lateinit var onFabClick: MutableState<() -> Unit>
    lateinit var icon: MutableState<ImageVector>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var networkMonitor: NetworkMonitor
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        applyLanguage(getLanguage())
        networkMonitor = NetworkMonitor(
            context = this
        )
        networkMonitor.startMonitoring()
        setContent {
            snackbarHostState = remember { SnackbarHostState() }
            showMap = remember { mutableStateOf(false) }
            banner = remember { mutableStateOf(false) }
            showNavBar = remember { mutableStateOf(false) }
            message = remember { mutableStateOf("") }
            onFabClick = remember { mutableStateOf({}) }
            icon = remember { mutableStateOf(Icons.Default.Map) }
            geocoder = Geocoder(this)
            MainUi()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Preview
    @Composable
    fun MainUi(){
        val navController = rememberNavController()
        var selectedItem by rememberSaveable {
            mutableIntStateOf(0)
        }
        val items = listOf<BottomNavigationItem>(
            BottomNavigationItem(
                title = getString(R.string.home),
                selectedIcon = R.drawable.home_filled,
                unselectedIcon = R.drawable.home,
                route = ScreensRoutes.HomeScreen
            ),
            BottomNavigationItem(
                title = getString(R.string.favorites),
                selectedIcon = R.drawable.favorites_filled,
                unselectedIcon = R.drawable.favorites,
                route = ScreensRoutes.FavoritesScreen
            ),
            BottomNavigationItem(
                title = getString(R.string.alerts),
                selectedIcon = R.drawable.alarm_clock_filled,
                unselectedIcon = R.drawable.alarm_clock,
                route = ScreensRoutes.WeatherAlertsScreen
            ),
            BottomNavigationItem(
                title = getString(R.string.settings),
                selectedIcon = R.drawable.settings_filled,
                unselectedIcon = R.drawable.settings,
                route = SettingsScreen
            )
        )
        val isConnected by rememberNetworkState()
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showNavBar.value) {
                    BottomAppBar {
                        val currentRoute = navController.currentBackStackEntryFlow
                            .collectAsState(initial = navController.currentBackStackEntry)
                            .value?.destination?.route?.substringAfterLast(".")
                        val isConnected by networkMonitor.isConnected.collectAsState()
                        items.forEachIndexed { index, item ->
                            val itemRoute = item.route::class.simpleName
                            val isSettingsItem = item.route == SettingsScreen
                            val isEnabled = !isSettingsItem || isConnected
                            NavigationBarItem(
                                selected = currentRoute == itemRoute,
                                onClick = {
                                    if (isSettingsItem && !isConnected) {
                                        lifecycleScope.launch {
                                            snackbarHostState.showSnackbar("Internet required for settings")
                                        }
                                    }else if (currentRoute != item.route::class.simpleName) {
                                            selectedItem = index
                                            navController.popBackStack(
                                                item.route,
                                                inclusive = false
                                            ) // Clears duplicate screens
                                            navController.navigate(item.route) {
                                                launchSingleTop =
                                                    true // Ensures only one instance exists
                                                restoreState =
                                                    true    // Restores state when navigating back
                                            }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = if (currentRoute == itemRoute) {
                                            painterResource(id = item.selectedIcon)
                                        } else {
                                            painterResource(id = item.unselectedIcon)
                                        },
                                        contentDescription = item.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(text = item.title)
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (showMap.value){
                    FloatingActionButton(
                        onClick = {
                            onFabClick.value()
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        containerColor = Color(0xFF007AFF),
                        modifier = Modifier.size(60.dp),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    ) {
                        Icon(imageVector = icon.value, contentDescription = "Add")
                    }
                }
            },
            topBar = {
                Column {
                    NetworkStatusIndicator(isConnected)
                }
            }
        ) {
            innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                SetupNavHost(navController, showMap, snackbarHostState, showNavBar,icon ,onFabClick, networkMonitor)
            }
        }
    }
    @Composable
    fun NetworkStatusIndicator(isConnected: Boolean = true) {
        AnimatedVisibility(
            visible = !isConnected,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No internet connection",
                        color = Color.White
                    )
                }
            }
        }
    }
    @Composable
    fun rememberNetworkState(): State<Boolean> {
        val context = LocalContext.current
        val networkMonitor = remember { NetworkMonitor(context) }

        DisposableEffect(Unit) {
            networkMonitor.startMonitoring()
            onDispose { networkMonitor.stopMonitoring() }
        }

        return networkMonitor.isConnected.collectAsState()
    }
    override fun onStart() {
        super.onStart()
        if (sharedPreferences.getString(
                Constants.LOCATION,
                MapHelper.GPS.mapType
            ) == MapHelper.GPS.mapType
        ) {
            Log.i("TAG", "onStart: ")
            if (checkPermission()) {
                if (isLocationEnabled()) {
                    getLocation()
                } else {
                    enableGps()
                }
            } else {
                requestPermission()
            }
        }
        lifecycleScope.launch {
            onChangeGPS().collect {
                if (it == MapHelper.GPS.mapType) {
                    if (checkPermission()) {
                        if (isLocationEnabled()) {
                            getLocation()
                            sharedPreferences.edit{putString(Constants.UPDATE,"true")}
                        } else {
                            enableGps()
                        }
                    } else {
                        requestPermission()
                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_CODE_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                getLocation()
            }
        }
    }


    fun checkPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ,REQUEST_CODE_LOCATION
        )
    }
    fun enableGps() {
        Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager:LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun getLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
       val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000)
           .setWaitForAccurateLocation(true)
           .build()
        val locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val location = p0.lastLocation
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0){
                    sharedPreferences.edit{ putString(Constants.CURRENT_LOCATION, "${location.latitude},${location.longitude}") }
                    Log.i("TAG", "getLocation: ${location.latitude} ${location.longitude}")
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        if (checkPermission()){
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback , Looper.myLooper())
        }
    }
    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun onChangeGPS(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Constants.LOCATION) {
                sharedPreferences.getString(key, MapHelper.GPS.mapType)?.let {
                    if (it == MapHelper.GPS.mapType) {
                        trySend(it)
                    }
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    fun getLanguage(): String {
        val storedLanguage = sharedPreferences.getString(Constants.LANGUAGE, "en") ?: "en"
        return if (storedLanguage.isEmpty()) Locale.getDefault().language else storedLanguage
    }

}
