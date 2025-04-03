package com.mario.skyeye.features.alert.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mario.skyeye.R
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.features.alert.viewmodel.WeatherAlertsViewModel
import com.mario.skyeye.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertsScreenUI(
    viewModel: WeatherAlertsViewModel,
    onFabClick: MutableState<() -> Unit>,
) {
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
        when (alarms) {
            is Response.Loading -> {
                AnimationLoading()
            }
            is Response.Failure -> {
                Text(text = (alarms as Response.Failure).error)
            }

            is Response.Success -> {
                if ((alarms as Response.Success<List<Alarm>>).data.isEmpty()) {
                    AnimationLoading()
                }else{
                    AlarmListScreen(viewModel, (alarms as Response.Success<List<Alarm>>).data, LocalContext.current)

                }
            }
        }
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
private fun AnimationLoading() {
    LottieAnimation(
        composition = rememberLottieComposition(
            com.airbnb.lottie.compose.LottieCompositionSpec.RawRes(
                R.raw.alert_lottie
            )
        ).value,
        speed = 1f,
        isPlaying = true,
        iterations = Int.MAX_VALUE
    )
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
fun AlarmBottomSheet
            (isRainEnabled: Boolean,
             isClearSkyEnabled: Boolean,
             viewModel: WeatherAlertsViewModel,
             onClose: () -> Unit
)
{
    val redColor = Color(0xFFE53935)
    val greenColor = Color(0xFF43A047)
    val context = LocalContext.current
    // State to track the selected option
    var startDurationTimeState by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val currentDateMillis = calendar.timeInMillis

    // State for showing dialogs
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var localRainEnabled by remember { mutableStateOf(isRainEnabled) }
    var localClearSkyEnabled by remember { mutableStateOf(isClearSkyEnabled) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf("") }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDateMillis,
        yearRange = IntRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + 1),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Get just the date portion (without time) for comparison
                val selectedCalendar = Calendar.getInstance().apply { timeInMillis = utcTimeMillis }
                val currentCalendar = Calendar.getInstance()

                // Reset time components to compare just dates
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                selectedCalendar.set(Calendar.MINUTE, 0)
                selectedCalendar.set(Calendar.SECOND, 0)
                selectedCalendar.set(Calendar.MILLISECOND, 0)

                currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
                currentCalendar.set(Calendar.MINUTE, 0)
                currentCalendar.set(Calendar.SECOND, 0)
                currentCalendar.set(Calendar.MILLISECOND, 0)

                // Allow today or any future date
                return !selectedCalendar.before(currentCalendar)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= calendar.get(Calendar.YEAR) &&
                        year <= calendar.get(Calendar.YEAR) + 1
            }
        }
    )

    // Time picker with constraints (initial time is current time + 1 minute)
    val startTimeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE) ,
        is24Hour = false
    )
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

    fun isTimeInPast(): Boolean {
        if (selectedDate.isEmpty() || startDurationTimeState.isEmpty()) return false

        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val dateTimeString = "$selectedDate $startDurationTimeState"

        return try {
            val selectedDateTime = dateFormat.parse(dateTimeString)
            selectedDateTime != null && selectedDateTime.before(Date())
        } catch (e: Exception) {
            false
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
                    when {
                        selectedDate.isEmpty() || startDurationTimeState.isEmpty() -> {
                            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                        }
                        isTimeInPast() -> {
                            Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT).show()
                        }
                        !PermissionUtils.hasNotificationPermission(context) -> {
                            permissionMessage = "Notification permission is required"
                            showPermissionDialog = true
                        }
                        !PermissionUtils.hasExactAlarmPermission(context) -> {
                            permissionMessage = "Exact alarm permission is required"
                            showPermissionDialog = true
                        }
                        else -> {
                            viewModel.setAlarm(selectedDate, startDurationTimeState, context)
                            onClose()
                        }
                    }
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
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text(permissionMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Open app settings to allow user to grant permissions
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



