package com.mario.skyeye.data.sharedprefrence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.mario.skyeye.utils.Constants
import com.mario.skyeye.utils.Constants.PREFS_NAME
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppPreference(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePreference(key: String, value: String) {
        sharedPreferences.edit{ putString(key, value).apply() }
    }

    fun getPreference(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    fun onChangeCurrentLocation(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Constants.CURRENT_LOCATION) {
                sharedPreferences.getString(key, "0.0,0.0")?.let {
                    if (it != "0.0,0.0") {
                        trySend(it)
                    }
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}