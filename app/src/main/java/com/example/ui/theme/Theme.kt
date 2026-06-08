package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = SageGreen,
    secondary = PeachAmber,
    tertiary = GoldenYellow,
    background = Color(0xFF1B221E),
    surface = Color(0xFF242C27),
    onPrimary = DeepMocha,
    onSecondary = DeepMocha,
    onTertiary = DeepMocha,
    onBackground = WarmBeige,
    onSurface = WarmBeige
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForestGreen,
    secondary = SageGreen,
    tertiary = PeachAmber,
    background = WarmBeige,
    surface = CreamIvory,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DeepMocha,
    onBackground = DeepMocha,
    onSurface = DeepMocha,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamicColor by default to preserve the hand-picked cozy garden coloring
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
