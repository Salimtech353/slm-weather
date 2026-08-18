package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color? = null,
    borderBrush: Brush? = null,
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val defaultBgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x33FFFFFF), // 20% white
                Color(0x12FFFFFF), // 7% white
                Color(0x1FFFFFFF)  // 12% white
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF2FFFFFF), // 95% white
                Color(0xDBFFFFFF), // 86% white
                Color(0xE6FFFFFF)  // 90% white
            )
        )
    }

    val defaultBorderBrush = borderBrush ?: if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x59FFFFFF), // 35% white highlight
                Color(0x1AFFFFFF), // 10% white
                Color(0x33FFFFFF)  // 20% white
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x80FFFFFF),
                Color(0x33000000),
                Color(0x4DFFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color(0x66000000) else Color(0x1F000000),
                spotColor = if (isDark) Color(0x4D38BDF8) else Color(0x1F3B82F6)
            )
            .clip(shape)
            .then(
                if (backgroundColor != null) {
                    Modifier.background(backgroundColor, shape)
                } else {
                    Modifier.background(defaultBgBrush, shape)
                }
            )
            .border(
                border = BorderStroke(1.dp, defaultBorderBrush),
                shape = shape
            ),
        content = content
    )
}
