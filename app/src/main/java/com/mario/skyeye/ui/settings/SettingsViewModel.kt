package com.mario.skyeye.ui.settings

import androidx.lifecycle.ViewModel
import com.mario.skyeye.data.sharedprefrence.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel(){
    private val _selectedTemp = MutableStateFlow(AppPreferences.getPreference("temp_unit", "°C"))
    val selectedTemp = _selectedTemp.asStateFlow()
    private val _selectedWindSpeed= MutableStateFlow(AppPreferences.getPreference("wind_unit", "km/h"))
    val selectedWindSpeed = _selectedWindSpeed.asStateFlow()
    private val _selectedLanguage= MutableStateFlow(AppPreferences.getPreference("language", "English"))
    val selectedLanguage = _selectedLanguage.asStateFlow()
    private val _selectedLocation= MutableStateFlow(AppPreferences.getPreference("location", "GPS"))
    val selectedLocation = _selectedLocation.asStateFlow()
    private val _selectedTheme= MutableStateFlow(AppPreferences.getPreference("theme", "System"))
    val selectedTheme = _selectedTheme.asStateFlow()

    fun updatePreference(key: String, value: String) {
        AppPreferences.savePreference(key, value)
        when (key) {
            "temp_unit" -> _selectedTemp.value = value
            "wind_unit" -> _selectedWindSpeed.value = value
            "language" -> _selectedLanguage.value = value
            "location" -> _selectedLocation.value = value
            "theme" -> _selectedTheme.value = value
        }
    }
}