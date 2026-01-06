package escom.ipn.m.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import escom.ipn.m.data.preferences.AppTheme

// ===== Esquemas de color GUINDA IPN =====
private val GuindaLightColorScheme = lightColorScheme(
    primary = GuindaPrimary,
    onPrimary = GuindaOnPrimary,
    primaryContainer = GuindaPrimaryLight,
    onPrimaryContainer = GuindaOnPrimary,
    secondary = GuindaSecondary,
    onSecondary = GuindaOnSecondary,
    background = GuindaBackground,
    onBackground = GuindaOnBackground,
    surface = GuindaSurface,
    onSurface = GuindaOnSurface,
    surfaceVariant = GuindaBackground,
    onSurfaceVariant = GuindaOnSurface
)

private val GuindaDarkColorScheme = darkColorScheme(
    primary = GuindaPrimaryDarkMode,
    onPrimary = GuindaOnPrimaryDarkMode,
    primaryContainer = GuindaPrimaryDarkDarkMode,
    onPrimaryContainer = GuindaPrimaryDarkMode,
    secondary = GuindaSecondaryDarkMode,
    onSecondary = GuindaOnPrimaryDarkMode,
    background = GuindaBackgroundDarkMode,
    onBackground = GuindaOnBackgroundDarkMode,
    surface = GuindaSurfaceDarkMode,
    onSurface = GuindaOnSurfaceDarkMode,
    surfaceVariant = GuindaSurfaceDarkMode,
    onSurfaceVariant = GuindaOnSurfaceDarkMode
)

// ===== Esquemas de color AZUL ESCOM =====
private val EscomLightColorScheme = lightColorScheme(
    primary = EscomPrimary,
    onPrimary = EscomOnPrimary,
    primaryContainer = EscomPrimaryLight,
    onPrimaryContainer = EscomOnPrimary,
    secondary = EscomSecondary,
    onSecondary = EscomOnSecondary,
    background = EscomBackground,
    onBackground = EscomOnBackground,
    surface = EscomSurface,
    onSurface = EscomOnSurface,
    surfaceVariant = EscomBackground,
    onSurfaceVariant = EscomOnSurface
)

private val EscomDarkColorScheme = darkColorScheme(
    primary = EscomPrimaryDarkMode,
    onPrimary = EscomOnPrimaryDarkMode,
    primaryContainer = EscomPrimaryDarkDarkMode,
    onPrimaryContainer = EscomPrimaryDarkMode,
    secondary = EscomSecondaryDarkMode,
    onSecondary = EscomOnPrimaryDarkMode,
    background = EscomBackgroundDarkMode,
    onBackground = EscomOnBackgroundDarkMode,
    surface = EscomSurfaceDarkMode,
    onSurface = EscomOnSurfaceDarkMode,
    surfaceVariant = EscomSurfaceDarkMode,
    onSurfaceVariant = EscomOnSurfaceDarkMode
)

private val AppTypography = androidx.compose.material3.Typography()

/**
 * Tema principal de la aplicación GPS Tracker
 *
 * @param appTheme Tema seleccionado (GUINDA_IPN o AZUL_ESCOM)
 * @param darkTheme Si está en modo oscuro
 * @param content Contenido de la aplicación
 */
@Composable
fun GPSTrackerTheme(
    appTheme:  AppTheme = AppTheme. GUINDA_IPN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.GUINDA_IPN -> {
            if (darkTheme) GuindaDarkColorScheme else GuindaLightColorScheme
        }
        AppTheme. AZUL_ESCOM -> {
            if (darkTheme) EscomDarkColorScheme else EscomLightColorScheme
        }
    }

    val view = LocalView. current
    if (! view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = ! darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}