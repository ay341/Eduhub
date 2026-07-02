package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

data class ThemeContextState(
    val isDark: Boolean = false,
    val toggleTheme: () -> Unit = {}
)

val LocalThemeContext = staticCompositionLocalOf { ThemeContextState() }

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    secondary = SecondaryPurpleContainer,
    onSecondary = OnSecondaryContainerText,
    background = SoftBackground,
    onBackground = OnBackgroundText,
    surface = CardBackground,
    onSurface = OnBackgroundText,
    outline = OutlineBorder,
    outlineVariant = LightBorder
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    secondary = Color(0xFF4A4458),
    onSecondary = Color(0xFFE8DEF8),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B20),
    onSurface = Color(0xFFE6E1E5),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep signature compatible
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
