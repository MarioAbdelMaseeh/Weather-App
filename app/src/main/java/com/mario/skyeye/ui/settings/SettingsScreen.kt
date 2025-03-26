package com.mario.skyeye.ui.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mario.skyeye.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val selectedWindSpeed by viewModel.selectedWindSpeed.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val selectedTemp by viewModel.selectedTemp.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* Handle back navigation */ }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Temperature Unit Selection
            SettingsCategory(title = stringResource(R.string.temperature)) {
                ToggleButtonGroup(
                    options = listOf(
                        stringResource(R.string.celsius),
                        stringResource(R.string.fahrenheit),
                        stringResource(R.string.kelvin)
                    ),
                    selectedOption = selectedTemp,
                    onOptionSelected = { viewModel.updatePreference("temp_unit", it) }
                )
            }

            // Wind Speed Selection
            SettingsCategory(title = stringResource(R.string.wind_speed)) {
                ToggleButtonGroup(
                    options = listOf(
                        stringResource(R.string.meters_per_second),
                        stringResource(R.string.kilometers_per_hour),
                        stringResource(R.string.miles_per_hour)
                    ),
                    selectedOption = selectedWindSpeed,
                    onOptionSelected = { viewModel.updatePreference("wind_unit", it) }
                )
            }

            // Language Selection
            SettingsCategory(title = stringResource(R.string.language)) {
                ToggleButtonGroup(
                    options = listOf(
                        stringResource(R.string.arabic),
                        stringResource(R.string.english)
                    ),
                    selectedOption = selectedLanguage,
                    onOptionSelected = { viewModel.updatePreference("language", it) }
                )
            }

            // Location Selection
            SettingsCategory(title = stringResource(R.string.location)) {
                ToggleButtonGroup(
                    options = listOf(
                        stringResource(R.string.gps),
                        stringResource(R.string.map)
                    ),
                    selectedOption = selectedLocation,
                    onOptionSelected = { viewModel.updatePreference("location", it) }
                )
            }

            // Theme Selection
            SettingsCategory(title = stringResource(R.string.theme)) {
                ToggleButtonGroup(
                    options = listOf(
                        stringResource(R.string.system),
                        stringResource(R.string.light),
                        stringResource(R.string.dark)
                    ),
                    selectedOption = selectedTheme,
                    onOptionSelected = { viewModel.updatePreference("theme", it) }
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
