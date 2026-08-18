package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecast
import com.example.data.model.TempUnit
import com.example.data.model.WindUnit
import com.example.ui.theme.AccentCyan
import kotlin.math.roundToInt

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyForecast>,
    tempUnit: TempUnit,
    windUnit: WindUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "24-Hour Forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_forecast_list"),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(hourlyList, key = { it.epochTime }) { item ->
                HourlyForecastItem(
                    item = item,
                    tempUnit = tempUnit,
                    windUnit = windUnit
                )
            }
        }
    }
}

@Composable
fun HourlyForecastItem(
    item: HourlyForecast,
    tempUnit: TempUnit,
    windUnit: WindUnit,
    modifier: Modifier = Modifier
) {
    val tempDisplay = tempUnit.convert(item.tempC).roundToInt()
    val windDisplay = windUnit.convert(item.windSpeedKmh).roundToInt()

    val bgBrush = if (item.isCurrentHour) {
        Brush.verticalGradient(
            colors = listOf(
                AccentCyan.copy(alpha = 0.35f),
                Color(0xFF2563EB).copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x24FFFFFF),
                Color(0x0DFFFFFF)
            )
        )
    }

    val borderStrokeColor = if (item.isCurrentHour) {
        AccentCyan.copy(alpha = 0.8f)
    } else {
        Color(0x26FFFFFF)
    }

    Box(
        modifier = modifier
            .width(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .border(1.dp, borderStrokeColor, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Time Label
            Text(
                text = item.timeLabel,
                fontSize = 12.sp,
                fontWeight = if (item.isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                color = if (item.isCurrentHour) AccentCyan else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Emoji / Icon
            Text(
                text = getWeatherEmoji(item.condition),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = "$tempDisplay°",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rain Probability
            if (item.popPercentage > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Rain probability",
                        tint = AccentCyan,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.popPercentage}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentCyan
                    )
                }
            } else {
                // Wind
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Wind speed",
                        tint = Color(0x99CBD5E1),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$windDisplay",
                        fontSize = 10.sp,
                        color = Color(0x99CBD5E1)
                    )
                }
            }
        }
    }
}
