package com.mario.skyeye

import android.app.Application
import com.mario.skyeye.data.sharedprefrence.AppPreferences

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
    }
}