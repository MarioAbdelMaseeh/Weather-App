package com.mario.skyeye.data.sharedprefrence

import android.content.Context
import androidx.core.content.edit
import com.mario.skyeye.utils.Constants.PREFS_NAME

class AppPreference(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePreference(key: String, value: String) {
        sharedPreferences.edit{ putString(key, value) }
    }

    fun getPreference(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}