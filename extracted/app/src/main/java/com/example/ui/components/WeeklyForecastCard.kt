package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.data.model.DailyForecast
import com.example.data.model.TempUnit
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WeeklyForecastCard(
    dailyList: List<DailyForecast>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    // Global min/max across all 7 days for range bar scaling
    val allMin = dailyList.minOfOrNull { it.minTempC } ?: 15.0
    val allMax = dailyList.maxOfOrNull { it.maxTempC } ?: 35.0
    val totalRange = max(allMax - allMin, 1.0)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_forecast_card"),
        shape = RoundedCornerShape(28.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "7-Day Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            dailyList.forEachIndexed { index, item ->
                DailyForecastRow(
                    item = item,
                    tempUnit = tempUnit,
                    globalMin = allMin,
                    globalMax = allMax,
                    totalRange = totalRange,
                    isToday = index == 0
                )
                if (index < dailyList.size - 1) {
                    HorizontalDivider(
                        color = Color(0x1AFFFFFF),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecast,
    tempUnit: TempUnit,
    globalMin: Double,
    globalMax: Double,
    totalRange: Double,
    isToday: Boolean
) {
    val minDisplay = tempUnit.convert(item.minTempC).roundToInt()
    val maxDisplay = tempUnit.convert(item.maxTempC).roundToInt()

    // Calculate start fraction and width fraction for the colored temperature range bar
    val startFraction = ((item.minTempC - globalMin) / totalRange).toFloat().coerceIn(0f, 1f)
    val endFraction = ((item.maxTempC - globalMin) / totalRange).toFloat().coerceIn(0f, 1f)
    val widthFraction = (endFraction - startFraction).coerceIn(0.15f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Name & Date
        Column(
            modifier = Modifier.width(88.dp)
        ) {
            Text(
                text = item.dayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) AccentCyan else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.dateLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weather Icon & Rain %
        Row(
            modifier = Modifier.width(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getWeatherEmoji(item.condition),
                fontSize = 20.sp
            )
            if (item.popPercentage > 20) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.popPercentage}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Min Temp
        Text(
            text = "$minDisplay°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        // Visual Temperature Range Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0x26FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(endFraction)
                    .padding(start = (startFraction * 80).dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AccentCyan,
                                AccentGold,
                                Color(0xFFF97316)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Max Temp
        Text(
            text = "$maxDisplay°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp)
        )
    }
}
