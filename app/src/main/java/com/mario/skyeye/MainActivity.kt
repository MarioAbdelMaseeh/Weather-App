package com.mario.skyeye

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mario.skyeye.navigation.BottomNavigationItem
import com.mario.skyeye.navigation.ScreensRoutes
import com.mario.skyeye.navigation.SetupNavHost
const val REQUEST_CODE_LOCATION = 5005
lateinit var locationState: MutableState<Location>
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            locationState = remember { mutableStateOf(Location(LocationManager.GPS_PROVIDER))}
            geocoder = Geocoder(this)
            MainUi()
            Log.i("TAG", "onCreate:${locationState.value.latitude} ${locationState.value.longitude}")


        }
    }
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
                route = ScreensRoutes.SettingsScreen
            )
        )

        Scaffold(
            bottomBar = { BottomAppBar{
                    items.forEachIndexed { index,item ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                navController.navigate(item.route)
                            },
                            icon = {
                                Icon(
                                    painter = if(selectedItem == index){
                                        painterResource(id = item.selectedIcon)
                                    }else{
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
        ) {
            innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                SetupNavHost(navController)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(checkPermission()){
            if(isLocationEnabled()){
                getLocation()
            }else{
                enableGps()
            }
        }else{
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == 123){
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
    fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
       val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000)
           .setWaitForAccurateLocation(true)
           .build()
        val locationCallback = object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                val location = p0.lastLocation
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0){
                    locationState.value = location
                    Log.i("TAG", "getLocation: ${location.latitude} ${location.longitude}")
                    fusedLocationClient.removeLocationUpdates(this)
                }else{
                    Toast.makeText(this@MainActivity, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (checkPermission()){
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback , Looper.myLooper())
        }
    }
}