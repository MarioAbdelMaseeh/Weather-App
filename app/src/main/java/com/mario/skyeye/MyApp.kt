package com.mario.skyeye

import android.app.Application
import com.mario.skyeye.data.sharedprefrence.PreferencesManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }
}