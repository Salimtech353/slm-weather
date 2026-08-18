package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.ThemeMode
import com.example.data.model.WindUnit
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val context = LocalContext.current
    var apiKeyInput by remember(settings.customApiKey) { mutableStateOf(settings.customApiKey) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customize your weather units, theme, API key, and data preferences.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance
        item {
            SettingsSectionHeader(title = "Appearance", icon = Icons.Default.BrightnessMedium)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Dark",
                            isSelected = settings.themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Light",
                            isSelected = settings.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "System",
                            isSelected = settings.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Weather Background Animations Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weather Background Animations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Subtle responsive Canvas rain, snow, floating clouds, and sunburst effects",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.animationsEnabled,
                            onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("settings_animations_switch")
                        )
                    }
                }
            }
        }

        // Location & GPS Auto-detection
        item {
            SettingsSectionHeader(title = "Location & GPS", icon = Icons.Default.LocationOn)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Detect Location on App Start",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Automatically detect device GPS location and update local weather when opening app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = settings.autoDetectLocation,
                            onCheckedChange = { viewModel.setAutoDetectLocation(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("settings_auto_location_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.fetchDeviceLocation(context)
                            Toast.makeText(context, "Detecting GPS location...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_detect_gps_now")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detect & Update My Location Now",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Units
        item {
            SettingsSectionHeader(title = "Units of Measurement", icon = Icons.Default.Thermostat)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Temperature Unit
                    Text(
                        text = "Temperature Unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Celsius (°C)",
                            isSelected = settings.tempUnit == TempUnit.CELSIUS,
                            onClick = { viewModel.setTempUnit(TempUnit.CELSIUS) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Fahrenheit (°F)",
                            isSelected = settings.tempUnit == TempUnit.FAHRENHEIT,
                            onClick = { viewModel.setTempUnit(TempUnit.FAHRENHEIT) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Wind Unit
                    Text(
                        text = "Wind Speed Unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "km/h",
                            isSelected = settings.windUnit == WindUnit.KMH,
                            onClick = { viewModel.setWindUnit(WindUnit.KMH) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "m/s",
                            isSelected = settings.windUnit == WindUnit.MS,
                            onClick = { viewModel.setWindUnit(WindUnit.MS) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "mph",
                            isSelected = settings.windUnit == WindUnit.MPH,
                            onClick = { viewModel.setWindUnit(WindUnit.MPH) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Pressure Unit
                    Text(
                        text = "Pressure Unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "hPa",
                            isSelected = settings.pressureUnit == PressureUnit.HPA,
                            onClick = { viewModel.setPressureUnit(PressureUnit.HPA) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "inHg",
                            isSelected = settings.pressureUnit == PressureUnit.INHG,
                            onClick = { viewModel.setPressureUnit(PressureUnit.INHG) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // OpenWeatherMap API Key Setup
        item {
            SettingsSectionHeader(title = "OpenWeatherMap API Key", icon = Icons.Default.Key)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Weather API Key",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your OpenWeatherMap API key to fetch real-time global weather, 5-day forecasts, and air pollution metrics. An interactive sample generator is used when no key is set.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Paste OpenWeatherMap API key...", color = Color.Gray) },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x1AFFFFFF),
                            unfocusedContainerColor = Color(0x12FFFFFF),
                            focusedIndicatorColor = AccentCyan,
                            unfocusedIndicatorColor = Color(0x33FFFFFF),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_key")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.setCustomApiKey(apiKeyInput)
                                Toast.makeText(context, "API Key Saved & Weather Refreshed!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_save_api_key")
                        ) {
                            Text("Save & Apply Key", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Data & Cache Management
        item {
            SettingsSectionHeader(title = "Data & Cache", icon = Icons.Default.CleaningServices)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Offline Cache",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Purge cached weather snapshots and reload fresh data.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.clearCache()
                                Toast.makeText(context, "Cache Cleared Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_clear_cache")
                        ) {
                            Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // About Section
        item {
            SettingsSectionHeader(title = "About", icon = Icons.Default.Info)
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SLM Weather App",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Smart weather information at a glance.",
                        fontSize = 13.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Version 1.0.0 (Production Build)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Attribution: Weather data, forecasts, and air pollution metrics powered by OpenWeatherMap API under Creative Commons license.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AccentCyan else Color(0x1FFFFFFF))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}
