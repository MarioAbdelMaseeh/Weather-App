package com.mario.skyeye

import android.app.Application
import androidx.work.Configuration

import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.mario.skyeye.data.local.AppDataBase
import com.mario.skyeye.data.local.LocalDataSourceImpl
import com.mario.skyeye.data.remote.RemoteDataSourceImpl
import com.mario.skyeye.data.remote.RetrofitHelper
import com.mario.skyeye.data.repo.RepoImpl
import com.mario.skyeye.data.sharedprefrence.AppPreference


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.MAPS_API_KEY)
        }
//        val repo = RepoImpl.getInstance(RemoteDataSourceImpl(RetrofitHelper.service),LocalDataSourceImpl(AppDataBase.getInstance(this).weatherDao()), AppPreference(this))
//        val config = Configuration.Builder()
//            .setWorkerFactory(WeatherWorkerFactory(repo))
//            .build()
//        WorkManager.initialize(this, config)
    }
}