package com.mario.skyeye

import android.app.Application
import com.google.android.libraries.places.api.Places


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()){
            Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.MAPS_API_KEY)
        }
    }
}