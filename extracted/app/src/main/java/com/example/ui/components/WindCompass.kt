package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentRose

@Composable
fun WindCompass(
    degrees: Int,
    modifier: Modifier = Modifier
) {
    val animatedRotation by animateFloatAsState(
        targetValue = degrees.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "compassRotation"
    )

    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 4f

            // Outer dial circle
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Cardinal marks (N, E, S, W)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(160, 255, 255, 255)
                textSize = 18f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }

            drawContext.canvas.nativeCanvas.drawText("N", center.x, center.y - radius + 14f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("S", center.x, center.y + radius - 4f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("E", center.x + radius - 8f, center.y + 6f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("W", center.x - radius + 8f, center.y + 6f, textPaint)

            // Animated Needle
            rotate(degrees = animatedRotation, pivot = center) {
                val needlePathNorth = Path().apply {
                    moveTo(center.x, center.y - radius + 12f)
                    lineTo(center.x - 4f, center.y)
                    lineTo(center.x + 4f, center.y)
                    close()
                }
                drawPath(needlePathNorth, color = AccentRose)

                val needlePathSouth = Path().apply {
                    moveTo(center.x, center.y + radius - 12f)
                    lineTo(center.x - 4f, center.y)
                    lineTo(center.x + 4f, center.y)
                    close()
                }
                drawPath(needlePathSouth, color = Color(0xFF94A3B8))

                // Center pin
                drawCircle(color = Color.White, radius = 3f, center = center)
            }
        }
    }
}
