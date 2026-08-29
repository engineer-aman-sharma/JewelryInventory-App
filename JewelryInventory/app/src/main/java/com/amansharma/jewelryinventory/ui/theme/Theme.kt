package com.amansharma.jewelryinventory.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Gold40,
    onPrimary = Color.White,
    primaryContainer = Champagne,
    onPrimaryContainer = Ink,
    secondary = Burgundy40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3D6D2),
    onSecondaryContainer = Color(0xFF3D1414),
    tertiary = SoftGold,
    background = Ivory,
    onBackground = Ink,
    surface = WarmSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0E7D8),
    onSurfaceVariant = Color(0xFF52463A),
    outline = Color(0xFF857567),
    error = Color(0xFFB3261E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Gold80,
    onPrimary = Night,
    primaryContainer = Color(0xFF5C4710),
    onPrimaryContainer = Champagne,
    secondary = Burgundy80,
    onSecondary = Night,
    secondaryContainer = Color(0xFF4A2424),
    onSecondaryContainer = Color(0xFFF6D9D5),
    tertiary = SoftGold,
    background = Night,
    onBackground = Color(0xFFF6EFE4),
    surface = NightSurface,
    onSurface = Color(0xFFF6EFE4),
    surfaceVariant = Color(0xFF3A332B),
    onSurfaceVariant = Color(0xFFD4C6B6),
    outline = Color(0xFF9C8E7E),
    error = Color(0xFFF2B8B5)
)

@Composable
fun JewelryInventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
