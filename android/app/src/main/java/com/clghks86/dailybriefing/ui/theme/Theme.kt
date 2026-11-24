package com.clghks86.dailybriefing.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.clghks86.dailybriefing.domain.model.ChangeDirection
import com.clghks86.dailybriefing.domain.model.ThemeMode

data class MarketColors(
    val up: Color,
    val down: Color,
    val flat: Color,
) {
    fun of(change: Double?): Color = when {
        change == null -> flat
        change > 0 -> up
        change < 0 -> down
        else -> flat
    }

    fun of(direction: ChangeDirection): Color = when (direction) {
        ChangeDirection.UP -> up
        ChangeDirection.DOWN -> down
        ChangeDirection.FLAT -> flat
    }
}

val LocalMarketColors = staticCompositionLocalOf {
    MarketColors(up = MarketUp, down = MarketDown, flat = MarketFlat)
}

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1206),
    primaryContainer = GoldDeep,
    onPrimaryContainer = Color(0xFF1A1206),
    secondary = Mint,
    onSecondary = Color(0xFF04231A),
    background = Ink,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = MarketUp,
)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6E2B4),
    onPrimaryContainer = Color(0xFF2A1E05),
    secondary = Color(0xFF15836B),
    onSecondary = Color.White,
    background = Paper,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMutedLight,
    surfaceContainer = SurfaceLight,
    surfaceContainerHigh = SurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = MarketUp,
)

@Composable
fun DailyBriefingTheme(
    mode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val scheme = if (darkTheme) DarkColors else LightColors
    val marketColors = if (darkTheme) {
        MarketColors(up = MarketUp, down = MarketDown, flat = MarketFlat)
    } else {
        MarketColors(up = Color(0xFFD32F3A), down = Color(0xFF1F6FEB), flat = Color(0xFF6B7789))
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalMarketColors provides marketColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content,
        )
    }
}
