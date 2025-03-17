package com.mario.skyeye.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.repo.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repo): ViewModel(){
    private val mutableCurrentWeatherResponse: MutableLiveData<CurrentWeatherResponse> = MutableLiveData()
    val currentWeatherResponse: LiveData<CurrentWeatherResponse> = mutableCurrentWeatherResponse
    private val mutableMessage: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> = mutableMessage
    fun getCurrentWeather(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getCurrentWeather(true, lat, lon)
                if (response != null) {
                    mutableCurrentWeatherResponse.postValue(response)
                } else {
                    mutableMessage.postValue("Error")
                }
            }catch (ex: Exception){
                mutableMessage.postValue(ex.message)
            }
        }
    }
}
class HomeFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}