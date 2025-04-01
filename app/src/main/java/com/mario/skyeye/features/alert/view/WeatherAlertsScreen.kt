package com.mario.skyeye.features.alert.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.R
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.features.alert.viewmodel.WeatherAlertsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsScreenUI(viewModel: WeatherAlertsViewModel, onFabClick: MutableState<() -> Unit>) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false)}
    var sheetState = rememberModalBottomSheetState()
    val isRainEnabled by viewModel.isRainEnabledState.collectAsStateWithLifecycle()
    val isClearSkyEnabled by viewModel.isClearSkyEnabledState.collectAsStateWithLifecycle()
    onFabClick.value = {
        showBottomSheet = true
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Header(title = stringResource(R.string.weather_alerts), subtitle = stringResource(R.string.manage_your_weather_notifications))
        AlarmListScreen(viewModel, alarms, LocalContext.current)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
            , containerColor = Color.White,

        ) {
            AlarmBottomSheet(isRainEnabled,isClearSkyEnabled,viewModel) { showBottomSheet = false }
        }
    }

}
@Composable
fun Header(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
@Composable
fun AlarmListScreen(
    viewModel: WeatherAlertsViewModel,
    alarms: List<Alarm>,
    context: Context
) {
    LazyColumn {
        items(alarms) { alarm ->
            AlarmItem(
                alarm = alarm,
                onToggle = { enabled ->
                    viewModel.toggleAlarm(context, alarm.copy(isEnabled = enabled))
                },
                onDelete = {
                    if (alarm.repeatInterval.toInt() != 0) {
                        val condition = alarm.label.slice( 19 until alarm.label.length)
                        viewModel.disablePeriodicAlarm(context,condition)
                        if (condition == "rain") {
                            viewModel.updateIsRainEnabled(false)
                        } else if (condition == "clear sky") {
                            viewModel.updateIsClearSkyEnabled(false)
                        }
                    }else{
                        viewModel.deleteAlarm(context, alarm)
                    }
                }
            )
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alarm.label, style = MaterialTheme.typography.titleMedium)
                Text(text = alarm.getFormattedDateTime(), style = MaterialTheme.typography.bodyMedium)
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete alarm")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(isRainEnabled: Boolean, isClearSkyEnabled: Boolean, viewModel: WeatherAlertsViewModel, onClose: () -> Unit) {
    val redColor = Color(0xFFE53935)
    val greenColor = Color(0xFF43A047)
    val context = LocalContext.current
    Log.i("AlarmBottomSheet", "AlarmBottomSheet: $isRainEnabled $isClearSkyEnabled")
    // State to track the selected option
    var startDurationTimeState by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }

    // State for showing dialogs
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var localRainEnabled by remember { mutableStateOf(isRainEnabled) }
    var localClearSkyEnabled by remember { mutableStateOf(isClearSkyEnabled) }
//    var isRainAlertEnabled by remember {  }
//    var isClearSkyAlertEnabled by remember { mutableStateOf(false) }


    // Time picker state
    val startTimeState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )

    // Date picker state
    val datePickerState = rememberDatePickerState()
    val calendar = Calendar.getInstance()

    // Interaction sources for click handling
    val dateInteractionSource = remember { MutableInteractionSource() }
    val startTimeInteractionSource = remember { MutableInteractionSource() }

    // Handle interactions
    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    LaunchedEffect(startTimeInteractionSource) {
        startTimeInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showStartTimePicker = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Add New Alert",
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 📅 Date Picker
//        Text(text = "Date", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select date") },
            label = { Text("Date") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            },
            interactionSource = dateInteractionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🕑 Start Duration
//        Text(text = "Start duration", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = startDurationTimeState,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Start time") },
            label = { Text("Start time") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Start Time"
                )
            },
            interactionSource = startTimeInteractionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp, 16.dp, 0.dp)
        ) {
            Text(
                text = "Enable Rain alert",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = localRainEnabled,
                onCheckedChange = { newValue ->
                    localRainEnabled = newValue // Update local state immediately
                    if (newValue) {
                        viewModel.enablePeriodicAlarm(context, "rain")
                    } else {
                        viewModel.disablePeriodicAlarm(context, "rain")
                    }
                    viewModel.updateIsRainEnabled(newValue)
                }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp, 16.dp, 0.dp)
        ) {
            Text(
                text = "Enable Clear Sky alert",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = localClearSkyEnabled,
                onCheckedChange = { newValue ->
                    localClearSkyEnabled = newValue // Update local state immediately
                    if (newValue) {
                        viewModel.enablePeriodicAlarm(context, "clear sky")
                    } else {
                        viewModel.disablePeriodicAlarm(context, "clear sky")
                    }
                    viewModel.updateIsClearSkyEnabled(newValue)
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            Button(
                onClick = {
                    viewModel.setAlarm(selectedDate, startDurationTimeState, context)
                    onClose()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(greenColor),
                modifier = Modifier.weight(3.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.save))
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Button(
                onClick = onClose,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(redColor),
                modifier = Modifier.weight(3.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.cancel))
            }

            Spacer(modifier = Modifier.weight(0.6f))

        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            calendar.timeInMillis = millis
                            selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(calendar.time)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = startTimeState.hour
                        val minute = startTimeState.minute
                        val amPm = if (hour < 12) "AM" else "PM"
                        val displayHour = when {
                            hour > 12 -> hour - 12
                            hour == 0 -> 12
                            else -> hour
                        }
                        startDurationTimeState =
                            "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Select Start Time") }
        ) {
            TimePicker(state = startTimeState)
        }
    }
}



