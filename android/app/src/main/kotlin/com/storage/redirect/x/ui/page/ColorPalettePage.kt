package com.storage.redirect.x.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.ui.component.bottombar.BottomBarAppearanceState
import com.storage.redirect.x.ui.theme.AppThemeSettings

@Composable
fun ColorPalettePage(
    themeSettings: AppThemeSettings,
    onThemeSettingsChange: (AppThemeSettings) -> Unit,
    bottomBarAppearance: BottomBarAppearanceState,
    onBottomBarAppearanceChange: (BottomBarAppearanceState) -> Unit,
    onBack: () -> Unit,
    bottomInnerPadding: Dp = 0.dp,
) {
    ColorPalettePageMiuix(
        themeSettings = themeSettings,
        onThemeSettingsChange = onThemeSettingsChange,
        bottomBarAppearance = bottomBarAppearance,
        onBottomBarAppearanceChange = onBottomBarAppearanceChange,
        onBack = onBack,
        bottomInnerPadding = bottomInnerPadding,
    )
}
