package com.mario.skyeye.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.repo.Repo

class FavoritesViewModel(repo: Repo) : ViewModel(){
    private val mutableCurrentWeatherResponse: MutableLiveData<CurrentWeatherResponse> = MutableLiveData()
    val currentWeatherResponse: LiveData<CurrentWeatherResponse> = mutableCurrentWeatherResponse
    private val mutableMessage: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> = mutableMessage
}
class FavoritesFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoritesViewModel(repo) as T
    }
}