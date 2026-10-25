package com.storage.redirect.x.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
fun AppTheme(
    settings: AppThemeSettings,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalColorMode provides settings.colorMode) {
        MiuixAppTheme(settings, content)
    }
}

@Composable
fun AppTheme(
    colorMode: Int = ColorMode.SYSTEM.value,
    keyColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val settings = AppThemeController.readSettings().let { current ->
        current.copy(
            colorMode = ColorMode.fromValue(colorMode),
            keyColor = keyColor?.toArgb() ?: current.keyColor,
        )
    }
    AppTheme(settings = settings, content = content)
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(themeMode: Int): Boolean {
    return isInDarkTheme(ColorMode.fromValue(themeMode))
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(colorMode: ColorMode): Boolean {
    return when {
        colorMode == ColorMode.LIGHT || colorMode == ColorMode.MONET_LIGHT -> false
        colorMode.isDark -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
private fun MiuixAppTheme(
    settings: AppThemeSettings,
    content: @Composable () -> Unit,
) {
    val darkTheme = isInDarkTheme(settings.colorMode)
    val paletteStyle = try {
        ThemePaletteStyle.valueOf(settings.paletteStyle.name)
    } catch (_: IllegalArgumentException) {
        ThemePaletteStyle.TonalSpot
    }
    val colorSpec = if (settings.colorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }
    val miuixColorMode = if (settings.miuixMonet) {
        settings.colorMode.toMonetMode()
    } else {
        settings.colorMode.toNonMonetMode()
    }
    val controller = ThemeController(
        colorSchemeMode = miuixColorMode.toMiuixColorSchemeMode(),
        keyColor = settings.keyColor.takeIf { it != 0 }?.let(::Color),
        isDark = darkTheme,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    MiuixTheme(
        controller = controller,
    ) {
        val focusColor = MiuixTheme.colorScheme.primary
        val feedback = remember(darkTheme, focusColor) { InteractionFeedback(darkTheme, focusColor) }
        CompositionLocalProvider(LocalIndication provides feedback, content = content)
    }
}

private fun ColorMode.toMiuixColorSchemeMode(): ColorSchemeMode {
    return when (this) {
        ColorMode.SYSTEM -> ColorSchemeMode.System
        ColorMode.LIGHT -> ColorSchemeMode.Light
        ColorMode.DARK -> ColorSchemeMode.Dark
        ColorMode.MONET_SYSTEM -> ColorSchemeMode.MonetSystem
        ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
        ColorMode.MONET_DARK -> ColorSchemeMode.MonetDark
    }
}
