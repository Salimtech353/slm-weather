package com.example.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.UnitsConfig
import com.example.data.model.WindUnit
import com.example.ui.components.AirQualityCard
import com.example.ui.components.DayNightCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HourlyForecastSection
import com.example.ui.components.TemperatureGraphCard
import com.example.ui.components.WeatherDashboardSkeleton
import com.example.ui.components.WeatherDetailsGrid
import com.example.ui.components.WeatherHeroCard
import com.example.ui.components.WeeklyForecastCard
import com.example.ui.theme.AccentCyan
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import com.example.utils.LocationHelper
import com.example.utils.WeatherUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onNavigateSearch: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            viewModel.fetchDeviceLocation(context)
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    // Auto-detect location on launch if setting is enabled and permission is granted
    androidx.compose.runtime.LaunchedEffect(settings.autoDetectLocation) {
        if (settings.autoDetectLocation && LocationHelper.hasLocationPermission(context)) {
            viewModel.fetchDeviceLocation(context)
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshCurrent() },
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_pull_refresh")
    ) {
        if (uiState.isLoading && uiState.weatherData == null) {
            WeatherDashboardSkeleton()
        } else if (uiState.errorMessage != null && uiState.weatherData == null) {
            ErrorStateView(
                message = uiState.errorMessage ?: "Unable to load weather data",
                onRetry = { viewModel.refreshCurrent() }
            )
        } else {
            val result = uiState.weatherData ?: return@PullToRefreshBox
            val weather = result.current

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_weather_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Bar (Location, GPS, Search, Refresh, Settings)
                item {
                    HomeHeaderBar(
                        cityName = weather.cityName,
                        country = weather.country,
                        lastUpdatedEpoch = weather.lastUpdatedEpoch,
                        isFromCache = result.isFromCache,
                        isSampleFallback = result.isSampleFallback,
                        isGpsActive = uiState.isGpsActive || uiState.selectedCity?.isCurrentLocation == true,
                        onGpsClick = {
                            if (LocationHelper.hasLocationPermission(context)) {
                                viewModel.fetchDeviceLocation(context)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        onSearchClick = onNavigateSearch,
                        onRefreshClick = { viewModel.refreshCurrent() },
                        onSettingsClick = onNavigateSettings
                    )
                }

                // Weather Hero Card
                item {
                    WeatherHeroCard(
                        weather = weather,
                        tempUnit = settings.tempUnit,
                        windUnit = settings.windUnit,
                        pressureUnit = settings.pressureUnit
                    )
                }

                // 24-Hour Forecast Section
                item {
                    HourlyForecastSection(
                        hourlyList = result.hourly,
                        tempUnit = settings.tempUnit,
                        windUnit = settings.windUnit
                    )
                }

                // 24-Hour Temperature Graph
                item {
                    TemperatureGraphCard(
                        hourlyList = result.hourly,
                        tempUnit = settings.tempUnit
                    )
                }

                // 7-Day Forecast Section
                item {
                    WeeklyForecastCard(
                        dailyList = result.daily,
                        tempUnit = settings.tempUnit
                    )
                }

                // Air Quality Section
                item {
                    AirQualityCard(airQuality = result.airQuality)
                }

                // Day & Night Solar Progress Tracker
                item {
                    DayNightCard(
                        sunriseEpoch = weather.sunriseTimeEpoch,
                        sunsetEpoch = weather.sunsetTimeEpoch,
                        timezoneOffsetSec = weather.timezoneOffsetSec
                    )
                }

                // Weather Details 8-Grid
                item {
                    WeatherDetailsGrid(
                        weather = weather,
                        tempUnit = settings.tempUnit,
                        windUnit = settings.windUnit,
                        pressureUnit = settings.pressureUnit
                    )
                }

                // Footer Attribution
                item {
                    WeatherAttributionFooter(onSettingsClick = onNavigateSettings)
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderBar(
    cityName: String,
    country: String,
    lastUpdatedEpoch: Long,
    isFromCache: Boolean,
    isSampleFallback: Boolean,
    isGpsActive: Boolean,
    onGpsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val timeDiffMinutes = ((System.currentTimeMillis() - lastUpdatedEpoch) / 60000).coerceAtLeast(0)
    val updateLabel = when {
        isFromCache -> "Cached • $timeDiffMinutes min ago"
        isSampleFallback -> "Demo Mode (Configure API Key)"
        timeDiffMinutes == 0L -> "Updated just now"
        timeDiffMinutes == 1L -> "Updated 1 min ago"
        else -> "Updated $timeDiffMinutes mins ago"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Location Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGpsActive) Icons.Default.MyLocation else Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (country.isNotBlank() && country != "GPS") "$cityName, $country" else cityName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                if (isGpsActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                }
            }
            Text(
                text = updateLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp)
            )
        }

        // Action Buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.Default.MyLocation,
                description = "Current GPS Location",
                onClick = onGpsClick,
                testTag = "btn_gps_location"
            )
            Spacer(modifier = Modifier.width(6.dp))
            HeaderIconButton(
                icon = Icons.Default.Search,
                description = "Search City",
                onClick = onSearchClick,
                testTag = "btn_search_city"
            )
            Spacer(modifier = Modifier.width(6.dp))
            HeaderIconButton(
                icon = Icons.Default.Refresh,
                description = "Refresh Weather",
                onClick = onRefreshClick,
                testTag = "btn_refresh_weather"
            )
            Spacer(modifier = Modifier.width(6.dp))
            HeaderIconButton(
                icon = Icons.Default.Settings,
                description = "Settings",
                onClick = onSettingsClick,
                testTag = "btn_settings"
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x26FFFFFF))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Unable to Load Weather Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("btn_retry_weather")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WeatherAttributionFooter(onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SLM Weather App",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Powered by OpenWeatherMap API & Air Pollution API",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
