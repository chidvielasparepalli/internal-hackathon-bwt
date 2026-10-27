package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HarvestLinkLightColorScheme = lightColorScheme(
  primary = ForestGreenPrimary,
  onPrimary = Color.White,
  primaryContainer = ForestGreenContainer,
  onPrimaryContainer = OnForestGreenContainer,
  secondary = TerracottaAccent,
  onSecondary = Color.White,
  secondaryContainer = TerracottaContainer,
  onSecondaryContainer = OnTerracottaContainer,
  tertiary = HarvestOchre,
  onTertiary = Color.White,
  tertiaryContainer = OchreContainer,
  background = WarmIvoryBackground,
  onBackground = CharcoalText,
  surface = WarmIvorySurface,
  onSurface = CharcoalText,
  surfaceVariant = WarmIvorySurfaceVariant,
  onSurfaceVariant = CharcoalMuted,
  outline = BorderSoft,
  outlineVariant = Color(0xFFEEEAE2),
  error = StatusCancelled,
  onError = Color.White,
  errorContainer = StatusCancelledBg,
  onErrorContainer = Color(0xFF410002)
)

private val HarvestLinkDarkColorScheme = darkColorScheme(
  primary = Color(0xFF90D5A9),
  onPrimary = Color(0xFF00391E),
  primaryContainer = ForestGreenLight,
  onPrimaryContainer = Color(0xFFAEF2C7),
  secondary = Color(0xFFFFB59B),
  onSecondary = Color(0xFF5A1D05),
  secondaryContainer = Color(0xFF7A2E14),
  onSecondaryContainer = Color(0xFFFFDBCF),
  background = Color(0xFF141916),
  onBackground = Color(0xFFE2E7E2),
  surface = Color(0xFF1C221E),
  onSurface = Color(0xFFE2E7E2),
  surfaceVariant = Color(0xFF28302A),
  onSurfaceVariant = Color(0xFFC3C9C2),
  outline = Color(0xFF4A544C)
)

@Composable
fun HarvestLinkTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) HarvestLinkDarkColorScheme else HarvestLinkLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
