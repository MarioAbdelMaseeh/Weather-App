package com.mario.skyeye.ui.settings

import androidx.lifecycle.ViewModel
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.enums.Languages
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.utils.Constants
import com.mario.skyeye.utils.Constants.THEME
import com.mario.skyeye.utils.Constants.LANGUAGE
import com.mario.skyeye.utils.Constants.LOCATION
import com.mario.skyeye.utils.Constants.TEMP_UNIT
import com.mario.skyeye.utils.Constants.WIND_UNIT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow



class SettingsViewModel(
    private val repo: Repo
) : ViewModel(){
    private val _selectedTemp = MutableStateFlow(repo.getPreference(TEMP_UNIT, TempUnit.METRIC.getTempSymbol()))
    val selectedTemp = _selectedTemp.asStateFlow()
    private val _selectedWindSpeed= MutableStateFlow(repo.getPreference(WIND_UNIT, TempUnit.METRIC.getWindSymbol()))
    val selectedWindSpeed = _selectedWindSpeed.asStateFlow()
    private val _selectedLanguage= MutableStateFlow(repo.getPreference(LANGUAGE, Languages.ENGLISH.displayName))
    val selectedLanguage = _selectedLanguage.asStateFlow()
    private val _selectedLocation= MutableStateFlow(repo.getPreference(LOCATION, "GPS"))
    val selectedLocation = _selectedLocation.asStateFlow()
    private val _selectedTheme= MutableStateFlow(repo.getPreference(THEME, "System"))
    val selectedTheme = _selectedTheme.asStateFlow()

    fun updatePreference(key: String, value: String) {
        repo.savePreference(key, value)
        repo.savePreference(Constants.UPDATE, "true")
        when (key) {
            TEMP_UNIT -> _selectedTemp.value = value
            WIND_UNIT -> _selectedWindSpeed.value = value
            LANGUAGE -> _selectedLanguage.value = value
            LOCATION -> _selectedLocation.value = value
            THEME -> _selectedTheme.value = value
        }
    }
    fun getPreference(key: String, defaultValue: String): String {
       return repo.getPreference(key, defaultValue)
    }
}