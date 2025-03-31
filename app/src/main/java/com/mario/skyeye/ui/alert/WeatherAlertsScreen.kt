package com.mario.skyeye.ui.alert

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.ui.settings.SettingsCategory
import com.mario.skyeye.ui.settings.ToggleButtonGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsScreenUI(viewModel: WeatherAlertsViewModel, onFabClick: MutableState<() -> Unit>) {
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false)}
    var sheetState = rememberModalBottomSheetState()
    onFabClick.value = {
        showBottomSheet = true
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
            , containerColor = Color.White
        ) {
            AlarmBottomSheet(selectedCondition,viewModel) { showBottomSheet = false }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(condition: String,viewModel: WeatherAlertsViewModel,onClose: () -> Unit) {
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
        Spacer(modifier = Modifier.height(16.dp))

        SettingsCategory(title ="Alarm Condition" ) {
            ToggleButtonGroup(
                options = listOf(
                    "rain",
                    "clear sky",
                    "none"
                ),
                selectedOption = condition ,
                onOptionSelected = {
                    viewModel.updatePreference("alarm_condition", it)
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
                    viewModel.onSave(selectedDate, startDurationTimeState, context)
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
                Text("Save")
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
                Text("Cancel")
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
        androidx.compose.material3.TimePickerDialog(
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


