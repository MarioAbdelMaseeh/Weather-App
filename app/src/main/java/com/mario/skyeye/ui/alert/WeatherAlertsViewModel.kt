package com.mario.skyeye.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.utils.Constants


class WeatherAlertsViewModel(private val repo: Repo) : ViewModel(){
    val lat : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[0].toDouble()
    val lon : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[1].toDouble()
    val unit : String
        get() = repo.getPreference(Constants.TEMP_UNIT, "metric")

}
class WeatherAlertsFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherAlertsViewModel(repo) as T
    }
}
