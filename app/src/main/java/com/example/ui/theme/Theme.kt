package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HTUDarkColorScheme = darkColorScheme(
    primary = Color(0xFF76D1FF),
    onPrimary = Color(0xFF003364),
    primaryContainer = Color(0xFF00488B),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFFC3C7D0),
    onSecondary = Color(0xFF2D3139),
    tertiary = HTURedTorch,
    background = HTUDarkBg,
    onBackground = HTUTextLight,
    surface = HTUDarkSurface,
    onSurface = HTUTextLight,
    surfaceVariant = Color(0xFF24262E),
    onSurfaceVariant = Color(0xFFC3C7D0),
    outline = Color(0xFF8D9199)
)

private val HTULightColorScheme = lightColorScheme(
    primary = HTUNavy,
    onPrimary = HTUSurface,
    primaryContainer = Color(0xFFDDE2F1),
    onPrimaryContainer = Color(0xFF001D35),
    secondary = Color(0xFF43474E),
    onSecondary = HTUSurface,
    tertiary = HTURedTorch,
    background = HTULightBg,
    onBackground = HTUTextDark,
    surface = HTUSurface,
    onSurface = HTUTextDark,
    surfaceVariant = Color(0xFFF3F3FA),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFFC3C7D0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HTUDarkColorScheme else HTULightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

