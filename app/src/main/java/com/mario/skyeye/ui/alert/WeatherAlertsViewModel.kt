package com.mario.skyeye.ui.alert

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mario.skyeye.alarm.setManualAlarm
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class WeatherAlertsViewModel(private val repo: Repo) : ViewModel(){
    val lat : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[0].toDouble()
    val lon : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[1].toDouble()
    val unit : String
        get() = repo.getPreference(Constants.TEMP_UNIT, "metric")
    private val _selectedCondition = MutableStateFlow(repo.getPreference(Constants.ALARM_CONDITION, "none"))
    val selectedCondition = _selectedCondition.asStateFlow()

    fun onSave(selectedDate: String, startDurationTimeState: String, context: Context) {
        if (selectedDate.isEmpty() || startDurationTimeState.isEmpty()) {
            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val triggerTime = calculateTriggerTime(selectedDate, startDurationTimeState)
        if (triggerTime <= 0) {
            Toast.makeText(context, "Invalid date/time selection", Toast.LENGTH_SHORT).show()
            return
        }
        setManualAlarm(context, triggerTime)
    }

    fun updatePreference(key: String, value: String) {
        repo.savePreference(key, value)
        repo.savePreference(Constants.UPDATE, "true")
        when (key) {
            Constants.ALARM_CONDITION -> _selectedCondition.value = value
        }
    }
    fun getPreference(key: String, defaultValue: String): String {
        return repo.getPreference(key, defaultValue)
    }

}
class WeatherAlertsFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherAlertsViewModel(repo) as T
    }
}
