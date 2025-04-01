package com.mario.skyeye.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.enums.Languages
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val repo: Repo
) : ViewModel(){
    private val _selectedTemp =
        MutableStateFlow(repo.getPreference(Constants.TEMP_UNIT, TempUnit.METRIC.getTempSymbol()))
    val selectedTemp = _selectedTemp.asStateFlow()
    private val _selectedWindSpeed=
        MutableStateFlow(repo.getPreference(Constants.WIND_UNIT, TempUnit.METRIC.getWindSymbol()))
    val selectedWindSpeed = _selectedWindSpeed.asStateFlow()
    private val _selectedLanguage=
        MutableStateFlow(repo.getPreference(Constants.LANGUAGE, Languages.ENGLISH.displayName))
    val selectedLanguage = _selectedLanguage.asStateFlow()
    private val _selectedLocation= MutableStateFlow(repo.getPreference(Constants.LOCATION, "GPS"))
    val selectedLocation = _selectedLocation.asStateFlow()
    private val _selectedTheme= MutableStateFlow(repo.getPreference(Constants.THEME, "System"))
    val selectedTheme = _selectedTheme.asStateFlow()

    fun updatePreference(key: String, value: String) {
        repo.savePreference(key, value)
        repo.savePreference(Constants.UPDATE, "true")
        when (key) {
            Constants.TEMP_UNIT -> _selectedTemp.value = value
            Constants.WIND_UNIT -> _selectedWindSpeed.value = value
            Constants.LANGUAGE -> _selectedLanguage.value = value
            Constants.LOCATION -> _selectedLocation.value = value
            Constants.THEME -> _selectedTheme.value = value
        }
    }
    fun getPreference(key: String, defaultValue: String): String {
       return repo.getPreference(key, defaultValue)
    }
}