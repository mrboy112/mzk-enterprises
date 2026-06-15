package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

var isDarkThemeGlobal by mutableStateOf(true)

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF0066CC), // Deep Blue
    secondary = Color(0xFF00BFFF), // Sky Blue
    tertiary = Color(0xFFFFFFFF), // Accent Color: White
    background = Color(0xFF0A0F1D), // Deep dark sleek background
    surface = Color(0xFF131B2E), // Modern rich slate card/surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF0D2544), // Rich dark blue container
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF1A263E), // Dark container for list items
    onSecondaryContainer = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0066CC), // Deep Blue
    secondary = Color(0xFF0052A3), // Sky/Dark Blue for legibility
    tertiary = Color(0xFF000000), // Black
    background = Color(0xFFF4F6FA), // High contrast clean light gray
    surface = Color(0xFFFFFFFF), // Pure white cards/sheets
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    primaryContainer = Color(0xFFE6F0FF), // Very soft sky tint
    onPrimaryContainer = Color(0xFF004499),
    secondaryContainer = Color(0xFFE2E8F0), // Soft control container
    onSecondaryContainer = Color.Black
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isDarkThemeGlobal,
  dynamicColor: Boolean = false, // Set to false to force our custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (isDarkThemeGlobal) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
