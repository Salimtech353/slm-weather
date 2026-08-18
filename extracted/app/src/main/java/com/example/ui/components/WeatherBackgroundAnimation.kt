package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data structures for high-performance Canvas particle rendering without heap allocations during draw
 */
private class RainParticle(
    var xRatio: Float,
    var yRatio: Float,
    var length: Float,
    var speed: Float,
    var alpha: Float,
    var strokeWidth: Float
)

private class SnowParticle(
    var xRatio: Float,
    var yRatio: Float,
    var radius: Float,
    var fallSpeed: Float,
    var swayAmp: Float,
    var swayFreq: Float,
    var swayOffset: Float,
    var alpha: Float,
    var isStar: Boolean
)

private class CloudParticle(
    var xRatio: Float,
    var yRatio: Float,
    var scale: Float,
    var speed: Float,
    var alpha: Float
)

private class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,
    val phase: Float
)

private class SunMoteParticle(
    var xRatio: Float,
    var yRatio: Float,
    var radius: Float,
    var speed: Float,
    var alpha: Float,
    var phase: Float
)

/**
 * Dynamic Atmospheric & Particle Weather Background
 * Features:
 * - Condition-reactive dynamic sky gradients (smooth cross-fading)
 * - Canvas-drawn particle physics (Rain, Clouds, Snow, Thunderstorm lightning, Stars, Fog, Sun rays)
 * - Responsive to wind speed and day/night mode
 * - Subtle opacities ensuring zero visual interference with foreground cards & typography
 */
@Composable
fun WeatherBackgroundAnimation(
    condition: WeatherCondition,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    windSpeedKmh: Double = 12.0,
    animationsEnabled: Boolean = true
) {
    // Determine dynamic atmospheric sky gradient colors based on weather condition & dark mode
    val skyColors = remember(condition, isDarkTheme) {
        getSkyGradientColors(condition, isDarkTheme)
    }

    val animatedTopColor by animateColorAsState(
        targetValue = skyColors[0],
        animationSpec = tween(1200),
        label = "topSkyColor"
    )
    val animatedMidColor by animateColorAsState(
        targetValue = skyColors[1],
        animationSpec = tween(1200),
        label = "midSkyColor"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = skyColors[2],
        animationSpec = tween(1200),
        label = "bottomSkyColor"
    )

    val backgroundGradient = remember(animatedTopColor, animatedMidColor, animatedBottomColor) {
        Brush.verticalGradient(
            colors = listOf(animatedTopColor, animatedMidColor, animatedBottomColor)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .testTag("weather_background_animation")
    ) {
        if (animationsEnabled) {
            when {
                condition.isStormy -> {
                    ThunderstormCanvasEffect(windSpeedKmh = windSpeedKmh, isDarkTheme = isDarkTheme)
                }
                condition.isRainy -> {
                    RainCanvasEffect(
                        isHeavy = condition == WeatherCondition.SHOWER_RAIN,
                        windSpeedKmh = windSpeedKmh,
                        isDarkTheme = isDarkTheme
                    )
                }
                condition.isSnowy -> {
                    SnowCanvasEffect(windSpeedKmh = windSpeedKmh, isDarkTheme = isDarkTheme)
                }
                condition.isCloudy -> {
                    CloudsCanvasEffect(
                        isOvercast = condition == WeatherCondition.OVERCAST_CLOUDS || condition == WeatherCondition.BROKEN_CLOUDS,
                        isDarkTheme = isDarkTheme,
                        windSpeedKmh = windSpeedKmh
                    )
                }
                condition == WeatherCondition.MIST -> {
                    MistFogCanvasEffect(isDarkTheme = isDarkTheme)
                }
                condition == WeatherCondition.CLEAR_NIGHT || (!condition.isDay && isDarkTheme) -> {
                    NightSkyCanvasEffect()
                }
                else -> {
                    // CLEAR_DAY / Sunny
                    ClearDaySunCanvasEffect(isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

/**
 * Returns a 3-stop rich atmospheric gradient matching the current weather condition
 */
private fun getSkyGradientColors(condition: WeatherCondition, isDarkTheme: Boolean): List<Color> {
    return if (isDarkTheme) {
        when (condition) {
            WeatherCondition.CLEAR_DAY -> listOf(Color(0xFF0F2B48), Color(0xFF133B5C), Color(0xFF0B192C))
            WeatherCondition.CLEAR_NIGHT -> listOf(Color(0xFF060B17), Color(0xFF0D172A), Color(0xFF152238))
            WeatherCondition.FEW_CLOUDS_DAY, WeatherCondition.SCATTERED_CLOUDS -> listOf(Color(0xFF102A45), Color(0xFF1E3A5F), Color(0xFF0B1728))
            WeatherCondition.FEW_CLOUDS_NIGHT -> listOf(Color(0xFF0B1220), Color(0xFF131D31), Color(0xFF1B2A47))
            WeatherCondition.BROKEN_CLOUDS, WeatherCondition.OVERCAST_CLOUDS -> listOf(Color(0xFF16202E), Color(0xFF223042), Color(0xFF111827))
            WeatherCondition.SHOWER_RAIN, WeatherCondition.RAIN -> listOf(Color(0xFF0F1E36), Color(0xFF192F4D), Color(0xFF0D1829))
            WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF120E24), Color(0xFF22173B), Color(0xFF0B0914))
            WeatherCondition.SNOW -> listOf(Color(0xFF122238), Color(0xFF1B324E), Color(0xFF1F3A59))
            WeatherCondition.MIST -> listOf(Color(0xFF182230), Color(0xFF253346), Color(0xFF121B26))
            WeatherCondition.UNKNOWN -> listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
        }
    } else {
        when (condition) {
            WeatherCondition.CLEAR_DAY -> listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC), Color(0xFFE0F2FE))
            WeatherCondition.CLEAR_NIGHT -> listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
            WeatherCondition.FEW_CLOUDS_DAY, WeatherCondition.SCATTERED_CLOUDS -> listOf(Color(0xFF60A5FA), Color(0xFF93C5FD), Color(0xFFE2E8F0))
            WeatherCondition.FEW_CLOUDS_NIGHT -> listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B))
            WeatherCondition.BROKEN_CLOUDS, WeatherCondition.OVERCAST_CLOUDS -> listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFFF1F5F9))
            WeatherCondition.SHOWER_RAIN, WeatherCondition.RAIN -> listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFE2E8F0))
            WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF475569), Color(0xFF6B7280), Color(0xFFCBD5E1))
            WeatherCondition.SNOW -> listOf(Color(0xFFBAE6FD), Color(0xFFE0F2FE), Color(0xFFF8FAFC))
            WeatherCondition.MIST -> listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0), Color(0xFFF8FAFC))
            WeatherCondition.UNKNOWN -> listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9), Color(0xFFFFFFFF))
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 1. RAIN ANIMATION EFFECT
// ------------------------------------------------------------------------------------------------
@Composable
private fun RainCanvasEffect(
    isHeavy: Boolean,
    windSpeedKmh: Double,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val dropCount = if (isHeavy) 90 else 55
    val drops = remember(isHeavy) {
        val rand = Random(42)
        List(dropCount) {
            RainParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                length = rand.nextFloat() * (if (isHeavy) 26f else 18f) + 12f,
                speed = rand.nextFloat() * (if (isHeavy) 0.024f else 0.016f) + 0.012f,
                alpha = rand.nextFloat() * 0.45f + 0.25f,
                strokeWidth = rand.nextFloat() * (if (isHeavy) 1.8f else 1.2f) + 0.8f
            )
        }
    }

    var frameTick by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val windFactor = (windSpeedKmh.toFloat() / 30f).coerceIn(-1.5f, 1.5f)
                    for (drop in drops) {
                        drop.yRatio += drop.speed * (dt * 60f)
                        drop.xRatio += (drop.speed * 0.35f * windFactor) * (dt * 60f)
                        if (drop.yRatio > 1.05f) {
                            drop.yRatio = -0.05f
                            drop.xRatio = Random.nextFloat()
                        }
                        if (drop.xRatio > 1.05f) drop.xRatio = -0.05f
                        if (drop.xRatio < -0.05f) drop.xRatio = 1.05f
                    }
                    frameTick = (frameTick + 1f) % 1000f
                }
                lastTime = time
            }
        }
    }

    val rainColor = if (isDarkTheme) Color(0xFF93C5FD) else Color(0xFF60A5FA)
    val windSlant = (windSpeedKmh.toFloat() * 0.35f).coerceIn(-15f, 15f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw gentle rain mist ripples at bottom
        for (i in 0 until 4) {
            val rippleY = h * (0.85f + i * 0.04f)
            val rippleAlpha = (0.06f - i * 0.012f).coerceAtLeast(0.01f)
            drawCircle(
                color = rainColor.copy(alpha = rippleAlpha),
                radius = w * (0.25f + i * 0.15f),
                center = Offset(w * (0.3f + i * 0.18f), rippleY),
                style = Stroke(width = 1.5f)
            )
        }

        // Draw raindrops
        for (drop in drops) {
            val startX = drop.xRatio * w
            val startY = drop.yRatio * h
            val endX = startX + windSlant
            val endY = startY + drop.length

            drawLine(
                color = rainColor.copy(alpha = drop.alpha),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = drop.strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 2. THUNDERSTORM ANIMATION EFFECT (Heavy Rain + Lightning Flashes + Dark Storm Clouds)
// ------------------------------------------------------------------------------------------------
@Composable
private fun ThunderstormCanvasEffect(
    windSpeedKmh: Double,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var lightningAlpha by remember { mutableFloatStateOf(0f) }
    var lightningX by remember { mutableFloatStateOf(0.5f) }

    // Intermittent lightning flash trigger
    LaunchedEffect(Unit) {
        while (isActive) {
            kotlinx.coroutines.delay(Random.nextLong(3200, 7500))
            lightningX = Random.nextFloat() * 0.8f + 0.1f
            // Quick dramatic flash pulse (pulse 1)
            lightningAlpha = 0.55f
            kotlinx.coroutines.delay(70)
            lightningAlpha = 0.15f
            kotlinx.coroutines.delay(50)
            // Pulse 2
            lightningAlpha = 0.75f
            kotlinx.coroutines.delay(100)
            // Fade out
            val steps = 8
            for (s in 0..steps) {
                lightningAlpha = 0.75f * (1f - s.toFloat() / steps)
                kotlinx.coroutines.delay(25)
            }
            lightningAlpha = 0f
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Rain layer
        RainCanvasEffect(isHeavy = true, windSpeedKmh = windSpeedKmh + 15.0, isDarkTheme = isDarkTheme)

        // Lightning flash overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (lightningAlpha > 0.01f) {
                // Ambient sky flash
                drawRect(
                    color = Color(0xFFEDE9FE).copy(alpha = (lightningAlpha * 0.5f).coerceIn(0f, 1f))
                )

                // Stylized branching lightning bolt
                val startX = size.width * lightningX
                val path = Path().apply {
                    moveTo(startX, 0f)
                    var curX = startX
                    var curY = 0f
                    val segments = 6
                    val segHeight = size.height * 0.55f / segments
                    for (i in 0 until segments) {
                        curX += (Random.nextFloat() - 0.48f) * 45f
                        curY += segHeight
                        lineTo(curX, curY)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFFFFFF).copy(alpha = lightningAlpha),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    color = Color(0xFFA78BFA).copy(alpha = lightningAlpha * 0.6f),
                    style = Stroke(width = 9f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 3. FLOATING CLOUDS ANIMATION EFFECT
// ------------------------------------------------------------------------------------------------
@Composable
private fun CloudsCanvasEffect(
    isOvercast: Boolean,
    isDarkTheme: Boolean,
    windSpeedKmh: Double,
    modifier: Modifier = Modifier
) {
    val cloudCount = if (isOvercast) 7 else 4
    val clouds = remember(isOvercast) {
        val rand = Random(99)
        List(cloudCount) { index ->
            CloudParticle(
                xRatio = (index.toFloat() / cloudCount) + (rand.nextFloat() * 0.15f),
                yRatio = 0.06f + (index * 0.11f) % 0.45f,
                scale = rand.nextFloat() * 0.45f + (if (isOvercast) 1.1f else 0.75f),
                speed = (rand.nextFloat() * 0.0004f + 0.00025f) * (if (index % 2 == 0) 1.2f else 0.8f),
                alpha = if (isDarkTheme) (rand.nextFloat() * 0.14f + 0.10f) else (rand.nextFloat() * 0.22f + 0.18f)
            )
        }
    }

    var frameTick by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val speedMultiplier = (windSpeedKmh.toFloat() / 15f).coerceIn(0.5f, 2.5f)
                    for (cloud in clouds) {
                        cloud.xRatio += cloud.speed * speedMultiplier * (dt * 60f)
                        if (cloud.xRatio > 1.4f) {
                            cloud.xRatio = -0.4f
                        }
                    }
                    frameTick = (frameTick + 1f) % 1000f
                }
                lastTime = time
            }
        }
    }

    val cloudBaseColor = if (isDarkTheme) {
        if (isOvercast) Color(0xFF334155) else Color(0xFF475569)
    } else {
        if (isOvercast) Color(0xFFE2E8F0) else Color(0xFFFFFFFF)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (cloud in clouds) {
            val cx = cloud.xRatio * w
            val cy = cloud.yRatio * h
            val baseRadius = w * 0.16f * cloud.scale

            drawCloudPuff(
                centerX = cx,
                centerY = cy,
                radius = baseRadius,
                color = cloudBaseColor.copy(alpha = cloud.alpha)
            )
        }
    }
}

/**
 * Draws a natural, organic multi-lobed cloud formation
 */
private fun DrawScope.drawCloudPuff(
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: Color
) {
    val r = radius
    // Center main puff
    drawCircle(color = color, radius = r, center = Offset(centerX, centerY))
    // Left puff
    drawCircle(color = color, radius = r * 0.72f, center = Offset(centerX - r * 0.85f, centerY + r * 0.18f))
    // Right puff
    drawCircle(color = color, radius = r * 0.82f, center = Offset(centerX + r * 0.88f, centerY + r * 0.12f))
    // Upper puff
    drawCircle(color = color, radius = r * 0.65f, center = Offset(centerX + r * 0.25f, centerY - r * 0.42f))
    // Base smoothing pill
    drawRoundRect(
        color = color,
        topLeft = Offset(centerX - r * 1.3f, centerY + r * 0.05f),
        size = Size(r * 2.6f, r * 0.7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.35f, r * 0.35f)
    )
}

// ------------------------------------------------------------------------------------------------
// 4. SNOW ANIMATION EFFECT
// ------------------------------------------------------------------------------------------------
@Composable
private fun SnowCanvasEffect(
    windSpeedKmh: Double,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val flakeCount = 65
    val flakes = remember {
        val rand = Random(77)
        List(flakeCount) { i ->
            SnowParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 3.8f + 1.6f,
                fallSpeed = rand.nextFloat() * 0.0035f + 0.0020f,
                swayAmp = rand.nextFloat() * 22f + 10f,
                swayFreq = rand.nextFloat() * 2.5f + 1.2f,
                swayOffset = rand.nextFloat() * (2f * PI.toFloat()),
                alpha = rand.nextFloat() * 0.55f + 0.35f,
                isStar = i % 5 == 0
            )
        }
    }

    var globalPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val windFactor = (windSpeedKmh.toFloat() / 20f).coerceIn(-1.2f, 1.2f)
                    for (flake in flakes) {
                        flake.yRatio += flake.fallSpeed * (dt * 60f)
                        flake.xRatio += (flake.fallSpeed * 0.2f * windFactor) * (dt * 60f)
                        if (flake.yRatio > 1.05f) {
                            flake.yRatio = -0.05f
                            flake.xRatio = Random.nextFloat()
                        }
                        if (flake.xRatio > 1.05f) flake.xRatio = -0.05f
                        if (flake.xRatio < -0.05f) flake.xRatio = 1.05f
                    }
                    globalPhase += dt * 1.8f
                }
                lastTime = time
            }
        }
    }

    val snowColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFFFFFFFF)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (flake in flakes) {
            val sway = sin(globalPhase * flake.swayFreq + flake.swayOffset) * flake.swayAmp
            val x = flake.xRatio * w + sway
            val y = flake.yRatio * h

            if (flake.isStar && flake.radius > 3f) {
                // Draw 6-pointed star snowflake
                val r = flake.radius * 1.5f
                val color = snowColor.copy(alpha = flake.alpha)
                for (angle in 0 until 3) {
                    val rad = Math.toRadians((angle * 60.0)).toFloat()
                    val dx = cos(rad) * r
                    val dy = sin(rad) * r
                    drawLine(
                        color = color,
                        start = Offset(x - dx, y - dy),
                        end = Offset(x + dx, y + dy),
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round
                    )
                }
            } else {
                drawCircle(
                    color = snowColor.copy(alpha = flake.alpha),
                    radius = flake.radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 5. CLEAR NIGHT SKY (Stars + Periodic Shooting Star)
// ------------------------------------------------------------------------------------------------
@Composable
private fun NightSkyCanvasEffect(
    modifier: Modifier = Modifier
) {
    val starCount = 55
    val stars = remember {
        val rand = Random(33)
        List(starCount) {
            StarParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.75f, // Upper 75% of sky
                radius = rand.nextFloat() * 2.2f + 0.8f,
                baseAlpha = rand.nextFloat() * 0.45f + 0.35f,
                twinkleSpeed = rand.nextFloat() * 2.5f + 1.0f,
                phase = rand.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nightTwinkle")
    val timeSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeSec"
    )

    // Shooting star animation state
    var shootingStarProgress by remember { mutableFloatStateOf(0f) }
    var isShootingStarActive by remember { mutableStateOf(false) }
    var shootStartX by remember { mutableFloatStateOf(0.2f) }
    var shootStartY by remember { mutableFloatStateOf(0.1f) }

    LaunchedEffect(Unit) {
        while (isActive) {
            kotlinx.coroutines.delay(Random.nextLong(6000, 14000))
            shootStartX = Random.nextFloat() * 0.5f + 0.1f
            shootStartY = Random.nextFloat() * 0.25f + 0.05f
            isShootingStarActive = true

            val steps = 30
            for (step in 0..steps) {
                shootingStarProgress = step.toFloat() / steps
                kotlinx.coroutines.delay(20)
            }
            isShootingStarActive = false
            shootingStarProgress = 0f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Subtle upper nebula glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x286366F1), Color(0x006366F1)),
                center = Offset(w * 0.75f, h * 0.2f),
                radius = w * 0.6f
            ),
            center = Offset(w * 0.75f, h * 0.2f),
            radius = w * 0.6f
        )

        // Draw twinkling stars
        for (star in stars) {
            val twinkle = (sin(timeSec * star.twinkleSpeed + star.phase) + 1f) * 0.5f
            val alpha = (star.baseAlpha * (0.4f + 0.6f * twinkle)).coerceIn(0.1f, 0.95f)

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.radius * (0.85f + 0.3f * twinkle),
                center = Offset(star.xRatio * w, star.yRatio * h)
            )
        }

        // Draw shooting star
        if (isShootingStarActive && shootingStarProgress in 0f..1f) {
            val length = w * 0.22f
            val totalTravelX = w * 0.45f
            val totalTravelY = h * 0.22f

            val headX = shootStartX * w + totalTravelX * shootingStarProgress
            val headY = shootStartY * h + totalTravelY * shootingStarProgress
            val tailX = headX - length * 0.85f
            val tailY = headY - length * 0.42f

            val fadeAlpha = if (shootingStarProgress < 0.2f) {
                shootingStarProgress / 0.2f
            } else if (shootingStarProgress > 0.7f) {
                (1f - shootingStarProgress) / 0.3f
            } else {
                1f
            }

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xCCBAE6FD), Color.White),
                    start = Offset(tailX, tailY),
                    end = Offset(headX, headY)
                ),
                start = Offset(tailX, tailY),
                end = Offset(headX, headY),
                strokeWidth = 2.2f * fadeAlpha,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * fadeAlpha),
                radius = 2.8f,
                center = Offset(headX, headY)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 6. CLEAR DAY SUNBURST & FLOATING SUN PARTICLES
// ------------------------------------------------------------------------------------------------
@Composable
private fun ClearDaySunCanvasEffect(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val moteCount = 28
    val motes = remember {
        val rand = Random(12)
        List(moteCount) {
            SunMoteParticle(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 3.5f + 1.2f,
                speed = rand.nextFloat() * 0.0012f + 0.0006f,
                alpha = rand.nextFloat() * 0.35f + 0.20f,
                phase = rand.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sunRays")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rayRotation"
    )

    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    var globalPhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    for (mote in motes) {
                        mote.yRatio -= mote.speed * (dt * 60f)
                        if (mote.yRatio < -0.05f) {
                            mote.yRatio = 1.05f
                            mote.xRatio = Random.nextFloat()
                        }
                    }
                    globalPhase += dt * 1.5f
                }
                lastTime = time
            }
        }
    }

    val sunCenterRatio = Offset(0.18f, 0.12f)
    val rayColor = if (isDarkTheme) Color(0xFFFDE047).copy(alpha = 0.04f) else Color(0xFFFEF08A).copy(alpha = 0.09f)
    val glowColor = if (isDarkTheme) Color(0xFFFBBF24).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.18f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val sunCenter = Offset(w * sunCenterRatio.x, h * sunCenterRatio.y)
        val sunRadius = w * 0.28f * sunPulse

        // Radial Sun Halo Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = sunCenter,
                radius = sunRadius * 1.8f
            ),
            center = sunCenter,
            radius = sunRadius * 1.8f
        )

        // Rotating Sunbeams
        rotate(degrees = rayRotation, pivot = sunCenter) {
            val beamCount = 12
            val beamLength = w * 1.2f
            val angleStep = 360f / beamCount
            for (i in 0 until beamCount) {
                val angle = i * angleStep
                val rad1 = Math.toRadians((angle - 4.5).toDouble()).toFloat()
                val rad2 = Math.toRadians((angle + 4.5).toDouble()).toFloat()

                val path = Path().apply {
                    moveTo(sunCenter.x, sunCenter.y)
                    lineTo(sunCenter.x + cos(rad1) * beamLength, sunCenter.y + sin(rad1) * beamLength)
                    lineTo(sunCenter.x + cos(rad2) * beamLength, sunCenter.y + sin(rad2) * beamLength)
                    close()
                }
                drawPath(path = path, color = rayColor, style = Fill)
            }
        }

        // Drifting Sun Dust motes / shimmering particles
        val moteColor = if (isDarkTheme) Color(0xFFFDE047) else Color(0xFFFEF3C7)
        for (mote in motes) {
            val sway = sin(globalPhase + mote.phase) * 12f
            val x = mote.xRatio * w + sway
            val y = mote.yRatio * h
            drawCircle(
                color = moteColor.copy(alpha = mote.alpha),
                radius = mote.radius,
                center = Offset(x, y)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 7. MIST & FOG HORIZONTAL WAVE EFFECT
// ------------------------------------------------------------------------------------------------
@Composable
private fun MistFogCanvasEffect(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mistWave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val fogColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFFE2E8F0)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 4 Layers of undulating mist bands across screen
        for (band in 0 until 4) {
            val bandY = h * (0.25f + band * 0.18f)
            val bandHeight = h * 0.22f
            val bandAlpha = (if (isDarkTheme) 0.08f else 0.14f) * (1f - (band * 0.12f))
            val speedFactor = if (band % 2 == 0) 1f else -0.7f
            val phase = waveOffset * speedFactor + band * 1.2f

            val path = Path().apply {
                moveTo(0f, bandY)
                val segments = 20
                for (s in 0..segments) {
                    val x = w * (s.toFloat() / segments)
                    val y = bandY + sin(phase + s * 0.5f) * 18f
                    lineTo(x, y)
                }
                lineTo(w, bandY + bandHeight)
                for (s in segments downTo 0) {
                    val x = w * (s.toFloat() / segments)
                    val y = bandY + bandHeight + cos(phase + s * 0.4f) * 15f
                    lineTo(x, y)
                }
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        fogColor.copy(alpha = 0f),
                        fogColor.copy(alpha = bandAlpha),
                        fogColor.copy(alpha = 0f)
                    ),
                    startY = bandY,
                    endY = bandY + bandHeight
                ),
                style = Fill
            )
        }
    }
}
