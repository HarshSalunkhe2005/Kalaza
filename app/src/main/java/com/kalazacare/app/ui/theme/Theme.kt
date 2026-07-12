package com.kalazacare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KalazaColorScheme = lightColorScheme(
    // Primary – Red
    primary          = KalazaRed,
    onPrimary        = White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = KalazaDarkMaroon,

    // Secondary – Maroon accent
    secondary        = KalazaDarkMaroon,
    onSecondary      = White,
    secondaryContainer = Color(0xFFFFEDEA),
    onSecondaryContainer = KalazaDarkMaroon,

    // Tertiary – Navy (charts)
    tertiary         = KalazaNavy,
    onTertiary       = White,
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = KalazaNavy,

    // Error
    error            = StatusError,
    onError          = White,
    errorContainer   = Color(0xFFFFDAD6),
    onErrorContainer = KalazaDarkMaroon,

    // Background & Surface – All white
    background       = White,
    onBackground     = OnSurface,
    surface          = White,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    // Outline
    outline          = Outline,
    outlineVariant   = Color(0xFFEEEEEE),

    // Inverse
    inverseSurface   = OnSurface,
    inverseOnSurface = White,
    inversePrimary   = Color(0xFFFFB3AC),
)

@Composable
fun KalazaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KalazaColorScheme,
        typography  = KalazaTypography,
        shapes      = KalazaShapes,
        content     = content,
    )
}
