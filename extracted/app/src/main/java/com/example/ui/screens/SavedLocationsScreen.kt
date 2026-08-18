package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedCity
import com.example.ui.components.GlassCard
import com.example.ui.components.getWeatherEmoji
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentRose
import com.example.ui.viewmodel.WeatherViewModel
import com.example.utils.WeatherUtils
import kotlin.math.roundToInt

@Composable
fun SavedLocationsScreen(
    viewModel: WeatherViewModel,
    onCitySelected: () -> Unit,
    onNavigateSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedCities by viewModel.savedCities.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_locations_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Saved Locations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage and quickly switch between your favorite weather locations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (savedCities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved locations yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Search for a city and tap the bookmark icon to save it here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedCities, key = { it.id }) { city ->
                        SavedCityCard(
                            city = city,
                            tempUnitSymbol = settings.tempUnit.symbol,
                            onSelect = {
                                viewModel.loadWeatherForCity(city)
                                onCitySelected()
                            },
                            onDelete = { viewModel.removeLocation(city.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateSearch,
            containerColor = AccentCyan,
            contentColor = Color.Black,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 24.dp)
                .testTag("fab_add_location")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Location")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Add City", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SavedCityCard(
    city: SavedCity,
    tempUnitSymbol: String,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val cond = WeatherUtils.mapCondition(city.cachedIcon, null)
    val displayTemp = city.cachedTempC?.roundToInt()?.toString() ?: "--"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("saved_city_card_${city.name}"),
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (city.isCurrentLocation) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GPS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (city.country.isNotBlank()) "${city.country} • ${city.cachedCondition ?: "Tap to load"}" else (city.cachedCondition ?: "Tap to load"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Weather Emoji / Icon
            Text(
                text = getWeatherEmoji(cond),
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Temp
            Text(
                text = "$displayTemp$tempUnitSymbol",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("btn_delete_city_${city.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Location",
                    tint = AccentRose.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
