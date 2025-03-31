package com.mario.skyeye.ui.alert

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.alarm.cancelAlarm
import com.mario.skyeye.alarm.setManualAlarm
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class WeatherAlertsViewModel(private val repo: Repo) : ViewModel(){
    val lat : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[0].toDouble()
    val lon : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[1].toDouble()
    val unit : String
        get() = repo.getPreference(Constants.TEMP_UNIT, "metric")
    private val _selectedCondition = MutableStateFlow(repo.getPreference(Constants.ALARM_CONDITION, "none"))
    val selectedCondition = _selectedCondition.asStateFlow()

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms = _alarms.asStateFlow()

    fun setAlarm(selectedDate: String, startDurationTimeState: String, context: Context) {
        when {
            selectedDate.isEmpty() || startDurationTimeState.isEmpty() -> {
                Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
            }
            calculateTriggerTime(selectedDate, startDurationTimeState) <= 0 -> {
                Toast.makeText(context, "Invalid date/time selection", Toast.LENGTH_SHORT).show()
            }
            else -> {
                viewModelScope.launch {
                    val alarm = Alarm(
                        triggerTime = calculateTriggerTime(selectedDate, startDurationTimeState),
                        isEnabled = true,
                    )
                    val alarmId = repo.insertAlarm(alarm).toInt()
                    if(!setManualAlarm(context, calculateTriggerTime(selectedDate, startDurationTimeState), alarmId)){
                        Toast.makeText(context, "Failed to set alarm", Toast.LENGTH_SHORT).show()
                        repo.deleteAlarm(alarm)
                    }
                    repo.getAllAlarms().collect {
                        _alarms.value = it
                    }
                }
            }
        }
    }
    fun toggleAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch {
            if (alarm.isEnabled) {
                setManualAlarm(context, alarm.triggerTime, alarm.createdAt.toInt())
            } else {
                cancelAlarm(context, alarm)
            }
            repo.updateAlarm(alarm)
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
            Toast.makeText(context, "Alarm ${if (alarm.isEnabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }
    fun deleteAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch {
            cancelAlarm(context, alarm)
            repo.deleteAlarm(alarm)
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
            Toast.makeText(context, "Alarm canceled", Toast.LENGTH_SHORT).show()
        }
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
