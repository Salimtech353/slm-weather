package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecast
import com.example.data.model.TempUnit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun TemperatureGraphCard(
    hourlyList: List<HourlyForecast>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    // Use up to 8 representative time slices (every 3 hours for smooth curve across 24h)
    val displayPoints = if (hourlyList.size > 8) {
        hourlyList.filterIndexed { index, _ -> index % (hourlyList.size / 7).coerceAtLeast(1) == 0 }.take(8)
    } else {
        hourlyList
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("temperature_graph_card"),
        shape = RoundedCornerShape(24.dp),
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
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "Temperature Trend",
                    tint = AccentAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Temperature Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "24-Hour Curve",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bezier Graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingX = 40f
                val paddingYTop = 30f
                val paddingYBottom = 40f
                val graphHeight = height - paddingYTop - paddingYBottom
                val availableWidth = width - (paddingX * 2)

                val temps = displayPoints.map { tempUnit.convert(it.tempC).toFloat() }
                var minT = temps.minOrNull() ?: 0f
                var maxT = temps.maxOrNull() ?: 30f
                if (maxT == minT) {
                    maxT += 2f
                    minT -= 2f
                }
                val tRange = max(maxT - minT, 1f)

                val points = displayPoints.mapIndexed { idx, item ->
                    val x = paddingX + (idx.toFloat() / (displayPoints.size - 1).coerceAtLeast(1)) * availableWidth
                    val normY = (tempUnit.convert(item.tempC).toFloat() - minT) / tRange
                    val y = paddingYTop + (1f - normY) * graphHeight
                    Offset(x, y)
                }

                // Draw Bezier Smooth Curve Path
                val strokePath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlX1 = (p0.x + p1.x) / 2f
                            val controlY1 = p0.y
                            val controlX2 = (p0.x + p1.x) / 2f
                            val controlY2 = p1.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                        }
                    }
                }

                // Area Fill under curve
                val fillPath = Path().apply {
                    addPath(strokePath)
                    if (points.isNotEmpty()) {
                        lineTo(points.last().x, height - paddingYBottom + 10f)
                        lineTo(points.first().x, height - paddingYBottom + 10f)
                        close()
                    }
                }

                // Draw Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AccentAmber.copy(alpha = 0.35f),
                            AccentCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = paddingYTop,
                        endY = height - paddingYBottom
                    )
                )

                // Draw Curve Line
                drawPath(
                    path = strokePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AccentCyan,
                            AccentAmber,
                            Color(0xFFF97316)
                        )
                    ),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Draw Node circles & native text labels
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }

                val subTextPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(180, 203, 213, 225)
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                points.forEachIndexed { index, pt ->
                    val item = displayPoints[index]
                    val isCurrent = item.isCurrentHour || index == 0

                    // Outer glow for current
                    if (isCurrent) {
                        drawCircle(
                            color = AccentCyan.copy(alpha = 0.35f),
                            radius = 12f,
                            center = pt
                        )
                    }

                    // Node dot
                    drawCircle(
                        color = if (isCurrent) AccentCyan else AccentAmber,
                        radius = if (isCurrent) 6f else 4.5f,
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = pt
                    )

                    // Draw Temperature Label above point
                    val tempStr = "${tempUnit.convert(item.tempC).roundToInt()}°"
                    drawContext.canvas.nativeCanvas.drawText(
                        tempStr,
                        pt.x,
                        pt.y - 14f,
                        textPaint
                    )

                    // Draw Time Label below graph
                    drawContext.canvas.nativeCanvas.drawText(
                        item.timeLabel,
                        pt.x,
                        height - 8f,
                        subTextPaint
                    )
                }
            }
        }
    }
}
