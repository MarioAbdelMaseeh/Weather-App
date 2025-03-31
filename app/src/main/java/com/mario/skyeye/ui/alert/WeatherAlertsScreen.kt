package com.mario.skyeye.ui.alert

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.mario.skyeye.ui.settings.SettingsCategory
import com.mario.skyeye.ui.settings.ToggleButtonGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsScreenUI(viewModel: WeatherAlertsViewModel, onFabClick: MutableState<() -> Unit>) {
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false)}
    var sheetState = rememberModalBottomSheetState()
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
            , containerColor = Color.White
        ) {
            AlertSettingsBottomSheet(selectedCondition,viewModel) { showBottomSheet = false }
            //AlarmBottomSheet(selectedCondition,viewModel) { showBottomSheet = false }
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
                    viewModel.deleteAlarm(context, alarm)
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
fun AlertSettingsBottomSheet(
    condition: String,
    viewModel: WeatherAlertsViewModel,
    onClose: () -> Unit
) {
    val tabTitles = listOf("Time Alerts", "Weather Alerts")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
//        Text(
//            text = "Alert Settings",
//            fontSize = 20.sp,
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        )

        // Tab Row
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> TimeAlarmSection(viewModel, onClose)
            1 -> WeatherConditionSection(condition, viewModel, onClose)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAlarmSection(
    viewModel: WeatherAlertsViewModel,
    onClose: () -> Unit
) {
        val redColor = Color(0xFFE53935)
    val greenColor = Color(0xFF43A047)
    val context = LocalContext.current

    // State to track the selected option
    var startDurationTimeState by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }

    // State for showing dialogs
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
    val endTimeInteractionSource = remember { MutableInteractionSource() }

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

    LaunchedEffect(endTimeInteractionSource) {
        endTimeInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showEndTimePicker = true
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
        Spacer(modifier = Modifier.height(16.dp))

        // 📅 Date Picker
        Text(text = "Date", style = MaterialTheme.typography.bodyMedium)
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

        Spacer(modifier = Modifier.height(16.dp))

        // 🕑 Start Duration
        Text(text = "Start duration", style = MaterialTheme.typography.bodyMedium)
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

        Spacer(modifier = Modifier.height(24.dp))
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(redColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    viewModel.setAlarm(selectedDate, startDurationTimeState, context)
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(greenColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }

    // Dialogs
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
                ) { Text("OK") }
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

@Composable
fun WeatherConditionSection(
    condition: String,
    viewModel: WeatherAlertsViewModel,
    onClose: () -> Unit
) {
    val greenColor = Color(0xFF43A047)
    val redColor = Color(0xFFE53935)

    // State for notification toggle
    var isNotificationEnabled by remember { mutableStateOf(false) }

    // State for notification preferences
    var playSound by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }
    var muteAtNight by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Weather condition toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Enable $condition alerts",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isNotificationEnabled,
                onCheckedChange = { isNotificationEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification preferences
        if (isNotificationEnabled) {
            Column {
                Text(
                    text = "Notification Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Sound toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Sound")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Play sound",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = playSound,
                        onCheckedChange = { playSound = it }
                    )
                }

                // Vibrate toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Vibration, "Vibrate")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Vibrate",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it }
                    )
                }

                // Mute at night toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NightsStay, "Mute at night")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Mute between 10PM-7AM",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = muteAtNight,
                        onCheckedChange = { muteAtNight = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(redColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
//                    viewModel.setWeatherAlert(
//                        condition = condition,
//                        enabled = isNotificationEnabled,
//                        playSound = playSound,
//                        vibrate = vibrate,
//                        muteAtNight = muteAtNight
//                    )
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(greenColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AlarmBottomSheet(condition: String,viewModel: WeatherAlertsViewModel,onClose: () -> Unit) {
//    val redColor = Color(0xFFE53935)
//    val greenColor = Color(0xFF43A047)
//    val context = LocalContext.current
//
//    // State to track the selected option
//    var startDurationTimeState by remember { mutableStateOf("") }
//    var selectedDate by remember { mutableStateOf("") }
//
//    // State for showing dialogs
//    var showStartTimePicker by remember { mutableStateOf(false) }
//    var showEndTimePicker by remember { mutableStateOf(false) }
//    var showDatePicker by remember { mutableStateOf(false) }
//
//    // Time picker state
//    val startTimeState = rememberTimePickerState(
//        initialHour = 12,
//        initialMinute = 0,
//        is24Hour = false
//    )
//
//    // Date picker state
//    val datePickerState = rememberDatePickerState()
//    val calendar = Calendar.getInstance()
//
//    // Interaction sources for click handling
//    val dateInteractionSource = remember { MutableInteractionSource() }
//    val startTimeInteractionSource = remember { MutableInteractionSource() }
//    val endTimeInteractionSource = remember { MutableInteractionSource() }
//
//    // Handle interactions
//    LaunchedEffect(dateInteractionSource) {
//        dateInteractionSource.interactions.collect { interaction ->
//            if (interaction is PressInteraction.Release) {
//                showDatePicker = true
//            }
//        }
//    }
//
//    LaunchedEffect(startTimeInteractionSource) {
//        startTimeInteractionSource.interactions.collect { interaction ->
//            if (interaction is PressInteraction.Release) {
//                showStartTimePicker = true
//            }
//        }
//    }
//
//    LaunchedEffect(endTimeInteractionSource) {
//        endTimeInteractionSource.interactions.collect { interaction ->
//            if (interaction is PressInteraction.Release) {
//                showEndTimePicker = true
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Add New Alert",
//            fontSize = 20.sp,
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 📅 Date Picker
//        Text(text = "Date", style = MaterialTheme.typography.bodyMedium)
//        OutlinedTextField(
//            value = selectedDate,
//            onValueChange = {},
//            readOnly = true,
//            placeholder = { Text("Select date") },
//            label = { Text("Date") },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.DateRange,
//                    contentDescription = "Select Date"
//                )
//            },
//            interactionSource = dateInteractionSource,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 🕑 Start Duration
//        Text(text = "Start duration", style = MaterialTheme.typography.bodyMedium)
//        OutlinedTextField(
//            value = startDurationTimeState,
//            onValueChange = {},
//            readOnly = true,
//            placeholder = { Text("Start time") },
//            label = { Text("Start time") },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.AccessTime,
//                    contentDescription = "Start Time"
//                )
//            },
//            interactionSource = startTimeInteractionSource,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp)
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Buttons
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Spacer(modifier = Modifier.weight(0.6f))
//
//            Button(
//                onClick = {
//                    viewModel.setAlarm(selectedDate, startDurationTimeState, context)
//                    onClose()
//                },
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(greenColor),
//                modifier = Modifier.weight(3.2f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Check,
//                    contentDescription = "Save"
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Save")
//            }
//
//            Spacer(modifier = Modifier.weight(0.6f))
//
//            Button(
//                onClick = onClose,
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(redColor),
//                modifier = Modifier.weight(3.2f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close"
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Cancel")
//            }
//
//            Spacer(modifier = Modifier.weight(0.6f))
//
//        }
//    }
//
//    // Date Picker Dialog
//    if (showDatePicker) {
//        DatePickerDialog(
//            onDismissRequest = { showDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let { millis ->
//                            calendar.timeInMillis = millis
//                            selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//                                .format(calendar.time)
//                        }
//                        showDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showDatePicker = false }) {
//                    Text("Cancel")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // Start Time Picker Dialog
//    if (showStartTimePicker) {
//        androidx.compose.material3.TimePickerDialog(
//            onDismissRequest = { showStartTimePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        val hour = startTimeState.hour
//                        val minute = startTimeState.minute
//                        val amPm = if (hour < 12) "AM" else "PM"
//                        val displayHour = when {
//                            hour > 12 -> hour - 12
//                            hour == 0 -> 12
//                            else -> hour
//                        }
//                        startDurationTimeState =
//                            "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
//                        showStartTimePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showStartTimePicker = false }) {
//                    Text("Cancel")
//                }
//            },
//            title = { Text("Select Start Time") }
//        ) {
//            TimePicker(state = startTimeState)
//        }
//    }
//}
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


