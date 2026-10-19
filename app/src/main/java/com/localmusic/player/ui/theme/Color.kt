package com.localmusic.player.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val WarmLight = lightColorScheme(
    primary = Color(0xFFB4472B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3B0A00),
    secondary = Color(0xFF77574C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF2C160F),
    tertiary = Color(0xFF6C5D2F),
    background = Color(0xFFFDF8F6),
    onBackground = Color(0xFF201A18),
    surface = Color(0xFFFDF8F6),
    onSurface = Color(0xFF201A18),
    surfaceVariant = Color(0xFFF3DED7),
    onSurfaceVariant = Color(0xFF53433F),
    outline = Color(0xFF85736E),
    outlineVariant = Color(0xFFD8C2BC),
)

private val WarmDark = darkColorScheme(
    primary = Color(0xFFFFB59B),
    onPrimary = Color(0xFF5E1500),
    primaryContainer = Color(0xFF8A2A0B),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFE7BEB0),
    onSecondary = Color(0xFF442A21),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFD8C68E),
    background = Color(0xFF181210),
    onBackground = Color(0xFFEDE0DC),
    surface = Color(0xFF181210),
    onSurface = Color(0xFFEDE0DC),
    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = Color(0xFFD8C2BC),
    outline = Color(0xFFA08C87),
    outlineVariant = Color(0xFF53433F),
)

internal val LightColors = WarmLight
internal val DarkColors = WarmDark
