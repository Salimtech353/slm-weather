package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.WeatherData
import com.example.data.model.WindUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.utils.WeatherUtils
import kotlin.math.roundToInt

@Composable
fun WeatherDetailsGrid(
    weather: WeatherData,
    tempUnit: TempUnit,
    windUnit: WindUnit,
    pressureUnit: PressureUnit,
    modifier: Modifier = Modifier
) {
    val windDisplay = windUnit.convert(weather.windSpeedKmh).roundToInt()
    val windDirStr = WeatherUtils.getWindDirection(weather.windDeg)
    val pressureDisplay = pressureUnit.convert(weather.pressureHpa).roundToInt()
    val feelsLikeDisplay = tempUnit.convert(weather.feelsLikeC).roundToInt()
    val dewPointDisplay = tempUnit.convert(weather.dewPointC).roundToInt()
    val uvCategory = WeatherUtils.getUvCategory(weather.uvIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_details_grid")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Weather Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Row 1: Humidity & Wind
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Humidity",
                value = "${weather.humidity}%",
                subtitle = "The dew point is $dewPointDisplay${tempUnit.symbol}",
                icon = Icons.Default.WaterDrop,
                iconTint = AccentCyan,
                modifier = Modifier.weight(1f)
            )

            // Wind card with mini compass
            GlassCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Wind",
                            tint = AccentEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wind",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "$windDisplay ${windUnit.label}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Direction: $windDirStr (${weather.windDeg}°)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        WindCompass(degrees = weather.windDeg)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Pressure & Visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Pressure",
                value = "$pressureDisplay ${pressureUnit.label}",
                subtitle = if (weather.pressureHpa > 1013) "High atmospheric pressure" else "Normal atmospheric pressure",
                icon = Icons.Default.Compress,
                iconTint = AccentPurple,
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                title = "Visibility",
                value = "${(weather.visibilityKm * 10.0).roundToInt() / 10.0} km",
                subtitle = if (weather.visibilityKm > 9) "Clear visibility conditions" else "Haze / mist detected",
                icon = Icons.Default.Visibility,
                iconTint = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: UV Index & Cloud Cover
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "UV Index",
                value = "${weather.uvIndex.roundToInt()}",
                subtitle = uvCategory,
                icon = Icons.Default.WbSunny,
                iconTint = AccentRose,
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                title = "Cloud Cover",
                value = "${weather.cloudiness}%",
                subtitle = if (weather.cloudiness > 50) "Mostly covered" else "Clear sky intervals",
                icon = Icons.Default.Cloud,
                iconTint = AccentCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 4: Feels Like & Dew Point
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailTile(
                title = "Feels Like",
                value = "$feelsLikeDisplay${tempUnit.symbol}",
                subtitle = "Humidity makes it feel ${(weather.feelsLikeC - weather.currentTempC).roundToInt()}° warmer",
                icon = Icons.Default.DeviceThermostat,
                iconTint = AccentAmber,
                modifier = Modifier.weight(1f)
            )

            DetailTile(
                title = "Dew Point",
                value = "$dewPointDisplay${tempUnit.symbol}",
                subtitle = "Moisture condensation temp",
                icon = Icons.Default.Opacity,
                iconTint = AccentEmerald,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
