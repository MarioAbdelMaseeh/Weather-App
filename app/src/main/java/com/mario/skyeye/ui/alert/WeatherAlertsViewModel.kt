package com.mario.skyeye.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.ui.details.DetailsViewModel

class WeatherAlertsViewModel(private val repo: Repo) : ViewModel(){

}
class WeatherAlertsFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherAlertsViewModel(repo) as T
    }
}
