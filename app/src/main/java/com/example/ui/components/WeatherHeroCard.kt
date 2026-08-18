package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PressureUnit
import com.example.data.model.TempUnit
import com.example.data.model.WeatherCondition
import com.example.data.model.WeatherData
import com.example.data.model.WindUnit
import com.example.ui.theme.ClearDaySky
import com.example.ui.theme.ClearNightSky
import com.example.ui.theme.CloudySky
import com.example.ui.theme.FoggySky
import com.example.ui.theme.RainySky
import com.example.ui.theme.SnowySky
import com.example.ui.theme.ThunderstormSky
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun WeatherHeroCard(
    weather: WeatherData,
    tempUnit: TempUnit,
    windUnit: WindUnit,
    pressureUnit: PressureUnit,
    modifier: Modifier = Modifier
) {
    // Dynamic Sky Gradient based on condition
    val skyGradientColors = when (weather.condition) {
        WeatherCondition.CLEAR_DAY -> ClearDaySky
        WeatherCondition.CLEAR_NIGHT -> ClearNightSky
        WeatherCondition.FEW_CLOUDS_DAY, WeatherCondition.SCATTERED_CLOUDS -> listOf(Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFF93C5FD))
        WeatherCondition.FEW_CLOUDS_NIGHT -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        WeatherCondition.BROKEN_CLOUDS, WeatherCondition.OVERCAST_CLOUDS -> CloudySky
        WeatherCondition.SHOWER_RAIN, WeatherCondition.RAIN -> RainySky
        WeatherCondition.THUNDERSTORM -> ThunderstormSky
        WeatherCondition.SNOW -> SnowySky
        WeatherCondition.MIST -> FoggySky
        WeatherCondition.UNKNOWN -> ClearDaySky
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weatherEffects")

    // Animated Sun Glow / Moon Pulse
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Animated Rain / Snow progress
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePhase"
    )

    // Temperature count-up animation
    val displayedTemp by animateFloatAsState(
        targetValue = tempUnit.convert(weather.currentTempC).toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "tempCountUp"
    )

    val feelsLikeDisplay = tempUnit.convert(weather.feelsLikeC).roundToInt()
    val minTempDisplay = tempUnit.convert(weather.tempMinC).roundToInt()
    val maxTempDisplay = tempUnit.convert(weather.tempMaxC).roundToInt()
    val windDisplay = windUnit.convert(weather.windSpeedKmh).roundToInt()
    val pressureDisplay = pressureUnit.convert(weather.pressureHpa).roundToInt()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_hero_card"),
        shape = RoundedCornerShape(32.dp),
        elevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(skyGradientColors))
        ) {
            // Weather particle canvas effects
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(32.dp))
            ) {
                val width = size.width
                val height = size.height

                when {
                    weather.condition.isRainy -> {
                        // Draw animated rain drops
                        val rainCount = 45
                        val random = Random(42)
                        for (i in 0 until rainCount) {
                            val startX = (random.nextFloat() * width + particlePhase * 30f) % width
                            val baseY = random.nextFloat() * height
                            val animatedY = (baseY + particlePhase * height) % height
                            val dropLength = 18f + (i % 4) * 6f

                            drawLine(
                                color = Color.White.copy(alpha = 0.45f),
                                start = Offset(startX, animatedY),
                                end = Offset(startX - 6f, animatedY + dropLength),
                                strokeWidth = 2f
                            )
                        }
                    }
                    weather.condition.isSnowy -> {
                        // Draw animated snow flakes
                        val snowCount = 35
                        val random = Random(24)
                        for (i in 0 until snowCount) {
                            val startX = (random.nextFloat() * width + kotlin.math.sin((particlePhase * 6.28f) + i) * 12f) % width
                            val animatedY = (random.nextFloat() * height + particlePhase * height) % height
                            val radius = 3f + (i % 3) * 1.5f

                            drawCircle(
                                color = Color.White.copy(alpha = 0.6f),
                                radius = radius,
                                center = Offset(startX, animatedY)
                            )
                        }
                    }
                    weather.condition == WeatherCondition.CLEAR_NIGHT || weather.condition == WeatherCondition.FEW_CLOUDS_NIGHT -> {
                        // Twinkling stars
                        val starCount = 30
                        val random = Random(101)
                        for (i in 0 until starCount) {
                            val x = random.nextFloat() * width
                            val y = random.nextFloat() * (height * 0.7f)
                            val starAlpha = ((kotlin.math.sin((particlePhase * 6.28f) + i * 0.5f) + 1f) / 2f * 0.7f + 0.2f).coerceIn(0.1f, 0.9f)
                            drawCircle(
                                color = Color.White.copy(alpha = starAlpha),
                                radius = if (i % 5 == 0) 2.5f else 1.5f,
                                center = Offset(x, y)
                            )
                        }
                    }
                    weather.condition == WeatherCondition.CLEAR_DAY || weather.condition == WeatherCondition.FEW_CLOUDS_DAY -> {
                        // Sun radial flare in top right
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x80FDE047),
                                    Color(0x33F59E0B),
                                    Color(0x00F59E0B)
                                ),
                                center = Offset(width * 0.85f, height * 0.15f),
                                radius = 180f * pulseGlow
                            ),
                            center = Offset(width * 0.85f, height * 0.15f),
                            radius = 180f * pulseGlow
                        )
                    }
                }
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Condition Badge & City Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33000000))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = getWeatherEmoji(weather.condition),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Giant Temperature
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${displayedTemp.roundToInt()}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        lineHeight = 76.sp,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text = tempUnit.symbol,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xD9FFFFFF),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Feels Like & High / Low
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Feels like $feelsLikeDisplay${tempUnit.symbol}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xF2FFFFFF),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ",
                        color = Color(0x80FFFFFF)
                    )
                    Text(
                        text = "H: $maxTempDisplay°  L: $minTempDisplay°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xF2FFFFFF),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3 Hero Metric Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HeroMetricPill(
                        icon = Icons.Default.WaterDrop,
                        label = "Humidity",
                        value = "${weather.humidity}%"
                    )
                    HeroMetricPill(
                        icon = Icons.Default.Air,
                        label = "Wind",
                        value = "$windDisplay ${windUnit.label}"
                    )
                    HeroMetricPill(
                        icon = Icons.Default.Compress,
                        label = "Pressure",
                        value = "$pressureDisplay ${pressureUnit.label}"
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x26FFFFFF))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFE2E8F0),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xCCFFFFFF)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun getWeatherEmoji(condition: WeatherCondition): String {
    return when (condition) {
        WeatherCondition.CLEAR_DAY -> "☀️"
        WeatherCondition.CLEAR_NIGHT -> "🌙"
        WeatherCondition.FEW_CLOUDS_DAY -> "🌤️"
        WeatherCondition.FEW_CLOUDS_NIGHT -> "☁️"
        WeatherCondition.SCATTERED_CLOUDS, WeatherCondition.BROKEN_CLOUDS, WeatherCondition.OVERCAST_CLOUDS -> "☁️"
        WeatherCondition.SHOWER_RAIN -> "🌦️"
        WeatherCondition.RAIN -> "🌧️"
        WeatherCondition.THUNDERSTORM -> "⛈️"
        WeatherCondition.SNOW -> "❄️"
        WeatherCondition.MIST -> "🌫️"
        WeatherCondition.UNKNOWN -> "☀️"
    }
}
