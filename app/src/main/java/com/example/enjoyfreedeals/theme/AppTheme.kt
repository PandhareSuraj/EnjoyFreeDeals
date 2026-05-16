package com.example.enjoyfreedeals.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.enjoyfreedeals.ui.theme.Typography

val DealRed = Color(0xFFE91B23)
val DealGreen = Color(0xFF006B2E)
val DealYellow = Color(0xFFFFD600)
val DealBackground = Color(0xFFF8F9FA)
val DealCard = Color(0xFFFFFFFF)
val DealText = Color(0xFF1E1E1E)

private val LightColors = lightColorScheme(
    primary = DealRed,
    secondary = DealGreen,
    tertiary = DealYellow,
    background = DealBackground,
    surface = DealCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DealText,
    onBackground = DealText,
    onSurface = DealText
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5A60),
    secondary = Color(0xFF40D37B),
    tertiary = DealYellow,
    background = Color(0xFF101214),
    surface = Color(0xFF181B1F),
    onPrimary = Color.White,
    onSecondary = Color(0xFF06170D),
    onTertiary = DealText,
    onBackground = Color(0xFFECEFF3),
    onSurface = Color(0xFFECEFF3)
)

@Composable
fun EnjoyDealsTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
