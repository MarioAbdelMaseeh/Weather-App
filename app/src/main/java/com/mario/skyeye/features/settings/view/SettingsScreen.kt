package com.mario.skyeye.features.settings.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.R
import com.mario.skyeye.enums.Languages
import com.mario.skyeye.enums.Languages.Companion.fromCode
import com.mario.skyeye.enums.Languages.Companion.fromLanguageDisplayName
import com.mario.skyeye.enums.MapHelper
import com.mario.skyeye.enums.MapHelper.Companion.fromMapDisplayName
import com.mario.skyeye.enums.MapHelper.Companion.fromMapType
import com.mario.skyeye.enums.TempUnit
import com.mario.skyeye.enums.TempUnit.Companion.fromSymbol
import com.mario.skyeye.enums.TempUnit.Companion.fromUnitType
import com.mario.skyeye.enums.TempUnit.Companion.fromWindUnitType
import com.mario.skyeye.features.settings.viewmodel.SettingsViewModel
import com.mario.skyeye.utils.Constants
import com.mario.skyeye.utils.NetworkMonitor
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel,onBack: () -> Unit, function: () -> Unit ) {
    val selectedWindSpeed by viewModel.selectedWindSpeed.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val selectedTemp by viewModel.selectedTemp.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isConnected by rememberNetworkState()

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            // Delay slightly to let user see what happened
            delay(1000)
            onBack() // Navigate back to home
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.manage_your_preferences),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            IconButton(onClick = { /* Handle back navigation */ }) {
//                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
//            }
//        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Temperature Unit Selection
            SettingsCategory(title = stringResource(R.string.temperature)) {
                ToggleButtonGroup(
                    options = listOf(
                        TempUnit.METRIC.getTempSymbol(),
                        TempUnit.IMPERIAL.getTempSymbol(),
                        TempUnit.STANDARD.getTempSymbol()
                    ),
                    selectedOption = fromUnitType(selectedTemp)?.getTempSymbol() ?: TempUnit.METRIC.getTempSymbol(),
                    onOptionSelected = {
                        viewModel.updatePreference("temp_unit", fromSymbol(it)?.unitType ?: TempUnit.METRIC.unitType)
                        viewModel.updatePreference("wind_unit", fromSymbol(it)?.unitType ?: TempUnit.METRIC.unitType)
                    }
                )
            }

            // Wind Speed Selection
            SettingsCategory(title = stringResource(R.string.wind_speed)) {
                ToggleButtonGroup(
                    options = listOf(
                        TempUnit.METRIC.getWindSymbol(),
                        TempUnit.IMPERIAL.getWindSymbol(),
                    ),
                    selectedOption = fromWindUnitType(selectedWindSpeed)?.getWindSymbol() ?: TempUnit.METRIC.getWindSymbol(),
                    onOptionSelected = {}
                )
            }

            // Language Selection
            SettingsCategory(title = stringResource(R.string.language)) {
                ToggleButtonGroup(
                    options = listOf(
                        Languages.ENGLISH.displayName,
                        Languages.ARABIC.displayName,
                        Languages.Default.displayName
                    ),
                    selectedOption = fromCode(selectedLanguage)?.displayName ?: Languages.ENGLISH.displayName,
                    onOptionSelected = {
                        if ((fromLanguageDisplayName(it)?.code
                                ?: Languages.ENGLISH.code) != viewModel.getPreference(
                                Constants.LANGUAGE,
                                Languages.ENGLISH.code
                            )
                        ){
                            viewModel.updatePreference(Constants.LANGUAGE, fromLanguageDisplayName(it)?.code ?: Languages.ENGLISH.code)
                            restartActivity(context)
                        }
                    }
                )
            }

            // Location Selection
            SettingsCategory(title = stringResource(R.string.location)) {
                ToggleButtonGroup(
                    options = listOf(
                        MapHelper.GPS.getDisplayName(),
                        MapHelper.MAP.getDisplayName()
                    ),
                    selectedOption = fromMapType(selectedLocation)?.getDisplayName() ?: MapHelper.GPS.getDisplayName(),
                    onOptionSelected = {
                        if (it == MapHelper.MAP.getDisplayName()){
                            function()
                        }else{
                            viewModel.updatePreference(Constants.LOCATION, fromMapDisplayName(it)?.mapType ?: MapHelper.GPS.mapType)
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun SettingsCategory(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ToggleButtonGroup(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3EDF5), shape = RoundedCornerShape(50)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { option ->
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (option == selectedOption) Color(0xFF16202C) else Color.Transparent,
                    contentColor = if (option == selectedOption) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text(option)
            }
        }
    }
}
fun restartActivity(context: Context) {
    val intent = (context as? Activity)?.intent
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}
@Composable
fun rememberNetworkState(): State<Boolean> {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }

    DisposableEffect(Unit) {
        networkMonitor.startMonitoring()
        onDispose { networkMonitor.stopMonitoring() }
    }

    return networkMonitor.isConnected.collectAsState()
}
