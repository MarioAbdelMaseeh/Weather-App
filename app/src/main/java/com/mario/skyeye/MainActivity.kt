package com.mario.skyeye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.ProductsRemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.navigation.BottomNavigationItem
import com.mario.skyeye.ui.theme.SkyEyeTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainUi()
        }
    }
    @Preview
    @Composable
    fun MainUi(){
        var selectedItem by rememberSaveable {
            mutableIntStateOf(0)
        }
        val items = listOf<BottomNavigationItem>(
            BottomNavigationItem(
                title = "Home",
                selectedIcon = R.drawable.home_filled,
                unselectedIcon = R.drawable.home
            ),
            BottomNavigationItem(
                title = "Favorites",
                selectedIcon = R.drawable.favorites_filled,
                unselectedIcon = R.drawable.favorites
            ),
            BottomNavigationItem(
                title = "Alerts",
                selectedIcon = R.drawable.alarm_clock_filled,
                unselectedIcon = R.drawable.alarm_clock
            ),
            BottomNavigationItem(
                title = "Settings",
                selectedIcon = R.drawable.settings_filled,
                unselectedIcon = R.drawable.settings
            )

        )
        Scaffold(
            bottomBar = { BottomAppBar{
                    items.forEachIndexed { index,item ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
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
            Text(text = "Hello World", modifier = Modifier.padding(innerPadding))
        }
    }
}