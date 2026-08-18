package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AirQualityData
import com.example.ui.theme.AqiGood
import com.example.ui.theme.AqiHazardous
import com.example.ui.theme.AqiModerate
import com.example.ui.theme.AqiSensitive
import com.example.ui.theme.AqiUnhealthy
import com.example.ui.theme.AqiVeryUnhealthy

@Composable
fun AirQualityCard(
    airQuality: AirQualityData,
    modifier: Modifier = Modifier
) {
    val aqiColor = when (airQuality.aqiIndex) {
        1 -> AqiGood
        2 -> AqiModerate
        3 -> AqiSensitive
        4 -> AqiUnhealthy
        5 -> AqiVeryUnhealthy
        else -> AqiHazardous
    }

    val aqiFraction = (airQuality.aqiValue / 300f).coerceIn(0.05f, 0.95f)
    val animatedFraction by animateFloatAsState(
        targetValue = aqiFraction,
        animationSpec = tween(durationMillis = 1000),
        label = "aqiPin"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("air_quality_card"),
        shape = RoundedCornerShape(28.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "Air Quality Index",
                    tint = aqiColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Air Quality",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(aqiColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AQI ${airQuality.aqiValue}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = aqiColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Status
            Text(
                text = airQuality.categoryName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = aqiColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Spectrum Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background Gradient Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AqiGood,
                                    AqiModerate,
                                    AqiSensitive,
                                    AqiUnhealthy,
                                    AqiVeryUnhealthy,
                                    AqiHazardous
                                )
                            )
                        )
                )

                // Indicator Thumb Pin
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val indicatorOffset = (maxWidth - 18.dp) * animatedFraction
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .size(18.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, aqiColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pollutants 6-Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutantChip(label = "PM2.5", value = "${airQuality.pm2_5}", unit = "µg/m³")
                PollutantChip(label = "PM10", value = "${airQuality.pm10}", unit = "µg/m³")
                PollutantChip(label = "O3", value = "${airQuality.o3}", unit = "µg/m³")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutantChip(label = "NO2", value = "${airQuality.no2}", unit = "µg/m³")
                PollutantChip(label = "SO2", value = "${airQuality.so2}", unit = "µg/m³")
                PollutantChip(label = "CO", value = "${airQuality.co}", unit = "µg/m³")
            }
        }
    }
}

@Composable
private fun PollutantChip(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                fontSize = 9.sp,
                color = Color(0x80CBD5E1)
            )
        }
    }
}
