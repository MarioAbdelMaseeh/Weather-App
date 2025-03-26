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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var selectedTemp by remember { mutableStateOf("°C") }
    var selectedWindSpeed by remember { mutableStateOf("m/s") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedLocation by remember { mutableStateOf("GPS") }
    var selectedTheme by remember { mutableStateOf("System") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* Handle back navigation */ }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            // Temperature Unit Selection
            SettingsCategory(title = "Temperature") {
                ToggleButtonGroup(
                    options = listOf("°C", "°F", "°K"),
                    selectedOption = selectedTemp,
                    onOptionSelected = { selectedTemp = it }
                )
            }

            // Wind Speed Selection
            SettingsCategory(title = "Wind speed") {
                ToggleButtonGroup(
                    options = listOf("m/s", "km/h", "mph"),
                    selectedOption = selectedWindSpeed,
                    onOptionSelected = { selectedWindSpeed = it }
                )
            }
            SettingsCategory(title = "Language") {
                ToggleButtonGroup(
                    options = listOf("Arabic", "English"),
                    selectedOption = selectedLanguage,
                    onOptionSelected = { selectedLanguage = it }
                )
            }

            SettingsCategory(title = "Location") {
                ToggleButtonGroup(
                    options = listOf("GPS", "Map"),
                    selectedOption = selectedLocation,
                    onOptionSelected = { selectedLocation = it }
                )
            }

            // Theme Selection
            SettingsCategory(title = "Theme") {
                ToggleButtonGroup(
                    options = listOf("System", "Light", "Dark"),
                    selectedOption = selectedTheme,
                    onOptionSelected = { selectedTheme = it }
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