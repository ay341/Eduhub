package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Turn off dynamic colors to preserve customized brand styling
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
