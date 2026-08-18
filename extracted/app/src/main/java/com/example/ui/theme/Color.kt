package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// SLM Weather Design System Colors
val DeepNavy = Color(0xFF0B111E)
val DarkNavyCard = Color(0xFF131D31)
val SlateNavy = Color(0xFF1E293B)
val BackgroundDark = DeepNavy
val BackgroundLight = Color(0xFFF8FAFC)
val GlassSurfaceDark = Color(0x33FFFFFF)
val GlassBorderDark = Color(0x26FFFFFF)
val GlassSurfaceLight = Color(0xB3FFFFFF)
val GlassBorderLight = Color(0x4DFFFFFF)

// Accent Colors
val AccentCyan = Color(0xFF38BDF8)
val AccentBlue = Color(0xFF3B82F6)
val AccentPurple = Color(0xFF8B5CF6)
val AccentAmber = Color(0xFFF59E0B)
val AccentGold = Color(0xFFFBBF24)
val AccentEmerald = Color(0xFF10B981)
val AccentRose = Color(0xFFF43F5E)

// Weather Condition Sky Gradients
val ClearDaySky = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5), Color(0xFF90CAF9))
val ClearNightSky = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF2E1065))
val CloudySky = listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFF64748B))
val RainySky = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF2563EB))
val ThunderstormSky = listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B), Color(0xFF4338CA))
val SnowySky = listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF94A3B8))
val FoggySky = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8))

// Air Quality Scale Colors
val AqiGood = Color(0xFF10B981)        // 1 - Good (0-50)
val AqiModerate = Color(0xFFFBBF24)    // 2 - Moderate (51-100)
val AqiSensitive = Color(0xFFF97316)   // 3 - Sensitive (101-150)
val AqiUnhealthy = Color(0xFFEF4444)   // 4 - Unhealthy (151-200)
val AqiVeryUnhealthy = Color(0xFF8B5CF6) // 5 - Very Unhealthy (201-300)
val AqiHazardous = Color(0xFF7F1D1D)   // Hazardous (301+)

// Glassmorphism Brushes
val GlassmorphicDarkBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x33FFFFFF),
        Color(0x14FFFFFF)
    )
)

val GlassmorphicLightBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xD9FFFFFF),
        Color(0xB3FFFFFF)
    )
)
