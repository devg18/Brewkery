package com.dev.brewkery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Espresso = Color(0xFF3B2417)
val Mocha = Color(0xFF6B4226)
val Caramel = Color(0xFFC8873F)
val StarYellow = Color(0xFFF5A623)

/** Main brand gradient used by buttons and bars. */
val BrewGradient = listOf(Color(0xFFB87333), Color(0xFF6B4226))

private val LightColors = lightColorScheme(
    primary = Mocha,
    onPrimary = Color.White,
    secondary = Caramel,
    onSecondary = Color.White,
    tertiary = Color(0xFF4F7A5A),
    tertiaryContainer = Color(0xFFDDEBDD),
    onTertiaryContainer = Color(0xFF1B3322),
    background = Color(0xFFFBF6EF),
    onBackground = Color(0xFF2B1B12),
    surface = Color.White,
    onSurface = Color(0xFF2B1B12),
    surfaceVariant = Color(0xFFF1E7DA),
    onSurfaceVariant = Color(0xFF7A6455),
    outline = Color(0xFFE2D3C0),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE0A96D),
    onPrimary = Color(0xFF2B1B12),
    secondary = Caramel,
    onSecondary = Color.White,
    tertiary = Color(0xFF8FC29C),
    tertiaryContainer = Color(0xFF23372A),
    onTertiaryContainer = Color(0xFFD3EBD9),
    background = Color(0xFF14100D),
    onBackground = Color(0xFFF4E9DC),
    surface = Color(0xFF1F1813),
    onSurface = Color(0xFFF4E9DC),
    surfaceVariant = Color(0xFF2C231C),
    onSurfaceVariant = Color(0xFFB9A592),
    outline = Color(0xFF3E3228),
    error = Color(0xFFF2B8B5)
)

private val BrewTypography = Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
)

@Composable
fun BrewkeryTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BrewTypography,
        content = content
    )
}
