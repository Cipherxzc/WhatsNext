package com.cipherxzc.whatsnext.ui.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/* ---- Brand blues & corals ---- */
val Blue40  = Color(0xFF2F5BEA)
val Blue50  = Color(0xFF3F6DFE)
val Blue60  = Color(0xFF4F7FFF)
val Blue80  = Color(0xFF9CB5FF)
val Coral40 = Color(0xFFFF715B)
val Coral80 = Color(0xFFFFA89E)
val Teal40  = Color(0xFF35CFC9)
val Teal80  = Color(0xFF78ECE6)

/* ---- Modern neutrals ---- */
val Graphite05 = Color(0xFFF5F7FA)   // 雾白
val Graphite95 = Color(0xFF101215)   // 夜黑

val WhatsNextLightScheme = lightColorScheme(
    primary = Blue60,         onPrimary = Color.White,
    secondary = Coral40,      onSecondary = Color.White,
    tertiary = Teal40,        onTertiary = Color.White,
    background = Graphite05,  onBackground = Color(0xFF1C1E21),
    surface = Color.White,    onSurface = Color(0xFF1C1E21),
    surfaceVariant = Color(0xFFF0F6F6),
    onSurfaceVariant = Color(0xFF45484F),
    outline = Color(0xFFC1C4CC),
    error = Color(0xFFE8505B), onError = Color.White,
)

val WhatsNextDarkScheme = darkColorScheme(
    primary = Blue80,         onPrimary = Graphite95,
    secondary = Coral80,      onSecondary = Graphite95,
    tertiary = Teal80,        onTertiary = Graphite95,
    background = Graphite95,  onBackground = Color(0xFFE4E6EB),
    surface = Color(0xFF1B1E23),  onSurface = Color(0xFFE4E6EB),
    surfaceVariant = Color(0xFF2A2E34),
    onSurfaceVariant = Color(0xFFC4C7CE),
    outline = Color(0xFF43464B),
    error = Color(0xFFFFB4AB), onError = Graphite95,
)