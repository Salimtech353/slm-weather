package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.utils.WeatherUtils
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DayNightCard(
    sunriseEpoch: Long,
    sunsetEpoch: Long,
    timezoneOffsetSec: Int,
    modifier: Modifier = Modifier
) {
    val isDay = WeatherUtils.isCurrentlyDaytime(sunriseEpoch, sunsetEpoch)
    val dayProgress = WeatherUtils.calculateDayProgress(sunriseEpoch, sunsetEpoch, timezoneOffsetSec)
    val animatedProgress by animateFloatAsState(
        targetValue = dayProgress,
        animationSpec = tween(durationMillis = 1200),
        label = "solarProgress"
    )

    val sunriseStr = if (sunriseEpoch > 0) {
        WeatherUtils.formatEpochTime(sunriseEpoch, timezoneOffsetSec, "h:mm a")
    } else "5:32 AM"

    val sunsetStr = if (sunsetEpoch > 0) {
        WeatherUtils.formatEpochTime(sunsetEpoch, timezoneOffsetSec, "h:mm a")
    } else "6:42 PM"

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_night_card"),
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
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (isDay) AccentAmber else AccentPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sun & Moon Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isDay) "Daylight Active" else "Night Time",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDay) AccentAmber else AccentPurple
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Arc representing Solar Path
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val w = size.width
                val h = size.height
                val startX = 40f
                val endX = w - 40f
                val baseY = h - 20f
                val arcHeight = h - 35f

                // Draw Dashed Baseline
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(startX - 20f, baseY),
                    end = Offset(endX + 20f, baseY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Solar Arc Path (Semi-Ellipse / Quadratic Bezier)
                val arcPath = Path().apply {
                    moveTo(startX, baseY)
                    quadraticTo(w / 2f, baseY - arcHeight * 2f, endX, baseY)
                }

                // Draw Track Arc
                drawPath(
                    path = arcPath,
                    color = Color(0x33FFFFFF),
                    style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                )

                // Calculate celestial body (Sun or Moon) position along the quadratic bezier curve
                val t = animatedProgress.coerceIn(0f, 1f)
                val oneMinusT = 1f - t
                val p0x = startX
                val p0y = baseY
                val p1x = w / 2f
                val p1y = baseY - arcHeight * 2f
                val p2x = endX
                val p2y = baseY

                val currentX = oneMinusT * oneMinusT * p0x + 2 * oneMinusT * t * p1x + t * t * p2x
                val currentY = oneMinusT * oneMinusT * p0y + 2 * oneMinusT * t * p1y + t * t * p2y

                // Glow around Sun / Moon
                drawCircle(
                    color = (if (isDay) AccentAmber else AccentCyan).copy(alpha = 0.35f),
                    radius = 16f,
                    center = Offset(currentX, currentY)
                )

                // Celestial Body Center
                drawCircle(
                    color = if (isDay) AccentAmber else Color(0xFFE2E8F0),
                    radius = 8f,
                    center = Offset(currentX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(currentX, currentY)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sunrise & Sunset Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "🌅 Sunrise",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunriseStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🌇 Sunset",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunsetStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
