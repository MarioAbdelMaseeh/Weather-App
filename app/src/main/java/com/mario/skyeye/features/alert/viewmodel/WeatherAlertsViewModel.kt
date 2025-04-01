package com.mario.skyeye.features.alert.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mario.skyeye.alarmmanager.cancelAlarm
import com.mario.skyeye.alarmmanager.setManualAlarm
import com.mario.skyeye.workmanager.WeatherAlertWorker
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.repo.Repo
import com.mario.skyeye.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class WeatherAlertsViewModel(private val repo: Repo) : ViewModel(){
    val lat : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[0].toDouble()
    val lon : Double
        get() = repo.getPreference(Constants.CURRENT_LOCATION, "0.0,0.0").split(",")[1].toDouble()
    val unit : String
        get() = repo.getPreference(Constants.TEMP_UNIT, "metric")

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms = _alarms.asStateFlow()

    private val isRainEnabled = MutableStateFlow(repo.getPreference(Constants.ALARM_RAIN, "false").toBoolean())
    val isRainEnabledState = isRainEnabled.asStateFlow()

    private val isClearSkyEnabled = MutableStateFlow(repo.getPreference(Constants.ALARM_CLEAR_SKY, "false").toBoolean())
    val isClearSkyEnabledState = isClearSkyEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
        }
    }

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
                    if(!setManualAlarm(context, calculateTriggerTime(selectedDate, startDurationTimeState), alarmId,lat, lon)){
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
                if (alarm.repeatInterval.toInt() != 0) {
                    val condition = alarm.label.slice( 19 until alarm.label.length)
                    schedulePeriodicWeatherAlert(context, condition)
                }else{
                    setManualAlarm(context, alarm.triggerTime, alarm.createdAt.toInt(),lat,lon)
                }
            } else {
                if (alarm.repeatInterval.toInt() != 0) {
                    cancelPeriodicWeatherAlert(context, alarm.label.slice( 19 until alarm.label.length))
                }else{
                    cancelAlarm(context, alarm)
                }
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
    fun updateIsRainEnabled(isEnabled: Boolean) {
        isRainEnabled.value = isEnabled
        repo.savePreference(Constants.ALARM_RAIN, isEnabled.toString())
    }
    fun updateIsClearSkyEnabled(isEnabled: Boolean) {
        isClearSkyEnabled.value = isEnabled
        repo.savePreference(Constants.ALARM_CLEAR_SKY, isEnabled.toString())
    }

    fun enablePeriodicAlarm(context: Context, condition: String) {

        viewModelScope.launch {
            val alarm = Alarm(
                triggerTime = System.currentTimeMillis(),
                isEnabled = true,
                label = "Periodic Alarm for $condition",
                repeatInterval = 1
            )
            repo.insertAlarm(alarm)
            schedulePeriodicWeatherAlert(context, condition)
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
        }
    }

    private fun schedulePeriodicWeatherAlert(context: Context, condition: String) {
        WeatherAlertWorker.schedule(
            context,
            lat,
            lon,
            unit,
            condition
        )
        if (condition == "rain") {
            repo.savePreference(
                Constants.ALARM_RAIN,
                true.toString()
            )
        } else if (condition == "clear sky") {
            repo.savePreference(
                Constants.ALARM_CLEAR_SKY,
                true.toString()
            )
        }
    }

    fun disablePeriodicAlarm(context: Context, condition: String) {
        viewModelScope.launch {

            repo.deleteAlarmByLabel("Periodic Alarm for $condition")
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
            cancelPeriodicWeatherAlert(context, condition)
        }
    }

    private fun cancelPeriodicWeatherAlert(context: Context, condition: String) {
        WeatherAlertWorker.cancel(context, condition)
        if (condition == "rain") {
            repo.savePreference(
                Constants.ALARM_RAIN,
                false.toString()
            )
            isRainEnabled.value = false
        } else if (condition == "clear sky") {
            repo.savePreference(
                Constants.ALARM_CLEAR_SKY,
                false.toString()
            )
            isClearSkyEnabled.value = false
        }
    }
    fun calculateTriggerTime(date: String, time: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val dateTimeString = "$date $time"
            dateFormat.parse(dateTimeString)?.time ?: 0L
        } catch (e: Exception) {
            Log.e("AlarmBottomSheet", "Error parsing date/time", e)
            0L
        }
    }
}
class WeatherAlertsFactory(private val repo: Repo): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherAlertsViewModel(repo) as T
    }
}
