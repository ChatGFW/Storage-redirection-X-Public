package com.storage.redirect.x.ui.page

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.icon.AppIcons
import com.storage.redirect.x.ui.component.PAGE_HORIZONTAL_PADDING
import com.storage.redirect.x.ui.component.SrxBackButton
import com.storage.redirect.x.ui.component.SrxTopAppBar
import com.storage.redirect.x.ui.component.bottombar.BottomBarAppearanceState
import com.storage.redirect.x.ui.component.miuix.ScaleDialog
import com.storage.redirect.x.ui.theme.AppThemeSettings
import com.storage.redirect.x.ui.theme.ColorMode
import com.storage.redirect.x.ui.theme.keyColorOptions
import com.storage.redirect.x.ui.util.LocalTopBarBackdrop
import com.storage.redirect.x.ui.util.rememberTopBarBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun ColorPalettePageMiuix(
    themeSettings: AppThemeSettings,
    onThemeSettingsChange: (AppThemeSettings) -> Unit,
    bottomBarAppearance: BottomBarAppearanceState,
    onBottomBarAppearanceChange: (BottomBarAppearanceState) -> Unit,
    onBack: () -> Unit,
    bottomInnerPadding: Dp = 0.dp,
) {
    BackHandler(onBack = onBack)

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val backdrop = rememberTopBarBackdrop(themeSettings.enableBlur)
    val colorMode = themeSettings.colorMode
    val isDark = colorMode.isDark || colorMode.isSystem && isSystemInDarkTheme()

    Scaffold(
        topBar = {
            CompositionLocalProvider(LocalTopBarBackdrop provides backdrop) {
                SrxTopAppBar(
                    title = stringResource(R.string.theme_title),
                    navigationIcon = { SrxBackButton(onClick = onBack) },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        val showScaleDialog = rememberSaveable { mutableStateOf(false) }

        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = PAGE_HORIZONTAL_PADDING),
                contentPadding = innerPadding,
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    ThemePreviewCardMiuix(
                        keyColor = themeSettings.keyColor,
                        isDark = isDark,
                        miuixMonet = themeSettings.miuixMonet,
                        enableFloatingBottomBar = bottomBarAppearance.isFloatingBottomBarEnabled,
                        enableFloatingBottomBarBlur = bottomBarAppearance.isFloatingBottomBarBlurEnabled,
                        paletteStyle = themeSettings.paletteStyle,
                        colorSpec = themeSettings.colorSpec,
                    )
                    Spacer(modifier = Modifier.height(72.dp))

                    val themeItems = listOf(
                        stringResource(R.string.theme_system),
                        stringResource(R.string.theme_light),
                        stringResource(R.string.theme_dark),
                    )
                    TabRow(
                        tabs = themeItems,
                        selectedTabIndex = colorMode.toBaseThemeIndex(),
                        onTabSelected = { index ->
                            onThemeSettingsChange(themeSettings.copy(colorMode = baseColorModeForIndex(index)))
                        },
                        height = 48.dp,
                    )

                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        SwitchPreference(
                            title = stringResource(R.string.settings_monet),
                            startAction = {
                                Icon(
                                    AppIcons.ColorFilter,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = null,
                                    tint = colorScheme.onBackground,
                                )
                            },
                            checked = themeSettings.miuixMonet,
                            onCheckedChange = { onThemeSettingsChange(themeSettings.copy(miuixMonet = it)) },
                        )

                        AnimatedVisibility(visible = themeSettings.miuixMonet) {
                            Column {
                                val colorItems = themeColorNames()
                                val colorValues = listOf(0) + keyColorOptions
                                OverlayDropdownPreference(
                                    title = stringResource(R.string.settings_key_color),
                                    items = colorItems,
                                    startAction = {
                                        Icon(
                                            AppIcons.ColorPicker,
                                            modifier = Modifier.padding(end = 6.dp),
                                            contentDescription = null,
                                            tint = colorScheme.onBackground,
                                        )
                                    },
                                    selectedIndex = colorValues.indexOf(themeSettings.keyColor).takeIf { it >= 0 } ?: 0,
                                    onSelectedIndexChange = { index ->
                                        onThemeSettingsChange(themeSettings.copy(keyColor = colorValues[index]))
                                    },
                                )

                                AnimatedVisibility(visible = themeSettings.keyColor != 0) {
                                    Column {
                                        val styles = PaletteStyle.entries
                                        OverlayDropdownPreference(
                                            title = stringResource(R.string.settings_color_style),
                                            items = styles.map { it.name },
                                            startAction = {
                                                Icon(
                                                    AppIcons.PaletteStyle,
                                                    modifier = Modifier.padding(end = 6.dp),
                                                    contentDescription = null,
                                                    tint = colorScheme.onBackground,
                                                )
                                            },
                                            selectedIndex = styles.indexOf(themeSettings.paletteStyle).coerceAtLeast(0),
                                            onSelectedIndexChange = { index ->
                                                onThemeSettingsChange(themeSettings.copy(paletteStyle = styles[index]))
                                            },
                                        )

                                        val specs = ColorSpec.SpecVersion.entries
                                        OverlayDropdownPreference(
                                            title = stringResource(R.string.settings_color_spec),
                                            items = specs.map { it.name },
                                            startAction = {
                                                Icon(
                                                    AppIcons.Brush,
                                                    modifier = Modifier.padding(end = 6.dp),
                                                    contentDescription = null,
                                                    tint = colorScheme.onBackground,
                                                )
                                            },
                                            selectedIndex = specs.indexOf(themeSettings.colorSpec).coerceAtLeast(0),
                                            onSelectedIndexChange = { index ->
                                                onThemeSettingsChange(themeSettings.copy(colorSpec = specs[index]))
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            SwitchPreference(
                                title = stringResource(R.string.settings_enable_blur),
                                summary = stringResource(R.string.settings_enable_blur_summary),
                                startAction = {
                                    Icon(
                                        AppIcons.Shadow,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = null,
                                        tint = colorScheme.onBackground,
                                    )
                                },
                                checked = themeSettings.enableBlur,
                                onCheckedChange = { onThemeSettingsChange(themeSettings.copy(enableBlur = it)) },
                            )
                        }
                        SwitchPreference(
                            title = stringResource(R.string.settings_floating_bottom_bar),
                            summary = stringResource(R.string.settings_floating_bottom_bar_summary),
                            startAction = {
                                Icon(
                                    AppIcons.LayoutBottom,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = null,
                                    tint = colorScheme.onBackground,
                                )
                            },
                            checked = bottomBarAppearance.isFloatingBottomBarEnabled,
                            onCheckedChange = { enabled ->
                                onBottomBarAppearanceChange(bottomBarAppearance.copy(isFloatingBottomBarEnabled = enabled))
                            },
                        )
                        AnimatedVisibility(visible = bottomBarAppearance.isFloatingBottomBarEnabled) {
                            SwitchPreference(
                                title = stringResource(R.string.settings_floating_bottom_bar_blur),
                                summary = stringResource(R.string.settings_floating_bottom_bar_blur_summary),
                                startAction = {
                                    Icon(
                                        AppIcons.Drop,
                                        modifier = Modifier.padding(end = 6.dp),
                                        contentDescription = null,
                                        tint = colorScheme.onBackground,
                                    )
                                },
                                checked = bottomBarAppearance.isFloatingBottomBarBlurEnabled,
                                onCheckedChange = { enabled ->
                                    onBottomBarAppearanceChange(bottomBarAppearance.copy(isFloatingBottomBarBlurEnabled = enabled))
                                },
                                enabled = bottomBarAppearance.isBlurSwitchEnabled,
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        var sliderValue by remember(themeSettings.pageScale) { mutableFloatStateOf(themeSettings.pageScale) }
                        ArrowPreference(
                            title = stringResource(R.string.settings_page_scale),
                            summary = stringResource(R.string.settings_page_scale_summary),
                            startAction = {
                                Icon(
                                    AppIcons.AspectRatio,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = null,
                                    tint = colorScheme.onBackground,
                                )
                            },
                            endActions = {
                                Text(
                                    text = "${(sliderValue * 100).toInt()}%",
                                    color = colorScheme.onSurfaceVariantActions,
                                )
                            },
                            onClick = { showScaleDialog.value = !showScaleDialog.value },
                            bottomAction = {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { sliderValue = it },
                                    onValueChangeFinished = {
                                        onThemeSettingsChange(themeSettings.copy(pageScale = sliderValue))
                                    },
                                    valueRange = 0.8f..1.1f,
                                )
                            },
                        )
                        ScaleDialog(
                            show = showScaleDialog.value,
                            onDismissRequest = { showScaleDialog.value = false },
                            volumeState = { themeSettings.pageScale },
                            onVolumeChange = { onThemeSettingsChange(themeSettings.copy(pageScale = it)) },
                        )
                    }
                }
                item {
                    Spacer(
                        Modifier.height(
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() +
                                12.dp + bottomInnerPadding,
                        )
                    )
                }
            }
        }
    }
}

private fun ColorMode.toBaseThemeIndex(): Int = when (this) {
    ColorMode.LIGHT, ColorMode.MONET_LIGHT -> 1
    ColorMode.DARK, ColorMode.MONET_DARK -> 2
    else -> 0
}

private fun baseColorModeForIndex(index: Int): ColorMode = when (index) {
    1 -> ColorMode.LIGHT
    2 -> ColorMode.DARK
    else -> ColorMode.SYSTEM
}

@Composable
internal fun themeColorNames(): List<String> = listOf(
    stringResource(R.string.settings_key_color_default),
    stringResource(R.string.color_red),
    stringResource(R.string.color_pink),
    stringResource(R.string.color_purple),
    stringResource(R.string.color_deep_purple),
    stringResource(R.string.color_indigo),
    stringResource(R.string.color_blue),
    stringResource(R.string.color_cyan),
    stringResource(R.string.color_teal),
    stringResource(R.string.color_green),
    stringResource(R.string.color_yellow),
    stringResource(R.string.color_amber),
    stringResource(R.string.color_orange),
    stringResource(R.string.color_brown),
    stringResource(R.string.color_blue_grey),
    stringResource(R.string.color_sakura),
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ThemePreviewCardMiuix(
    keyColor: Int,
    isDark: Boolean,
    miuixMonet: Boolean,
    enableFloatingBottomBar: Boolean = false,
    enableFloatingBottomBarBlur: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.Default,
) {
    val configuration = LocalConfiguration.current
    val screenRatio = configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.toFloat()

    val seedColor = if (keyColor == 0) colorScheme.primary else Color(keyColor)
    val effectiveStyle = if (keyColor == 0) PaletteStyle.TonalSpot else paletteStyle
    val effectiveSpec = if (keyColor == 0) ColorSpec.SpecVersion.Default else colorSpec
    val dynamicCs = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        style = effectiveStyle,
        specVersion = effectiveSpec,
    )

    val bgColor = if (miuixMonet) dynamicCs.background else colorScheme.surface
    val textColor = if (miuixMonet) dynamicCs.onSurface else colorScheme.onBackground
    val accentCardColor = when {
        miuixMonet -> dynamicCs.secondaryContainer
        isDark -> Color(0xFF1A3825)
        else -> Color(0xFFDFFAE4)
    }
    val cardColor = if (miuixMonet) dynamicCs.surfaceContainerHighest else colorScheme.surfaceVariant
    val navBarColor = if (miuixMonet) dynamicCs.surfaceContainer else colorScheme.surface
    val iconColor = if (miuixMonet) dynamicCs.primary else colorScheme.primary
    val navSelectedColor = colorScheme.onSurfaceContainer
    val navUnselectedColor = colorScheme.onSurfaceContainer.copy(alpha = 0.5f)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(screenRatio)
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(1.dp, colorScheme.outline, RoundedCornerShape(20.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 12.sp,
                        color = textColor,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentCardColor),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cardColor),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cardColor),
                    )
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(cardColor),
                        )
                    }
                }
            }

            if (enableFloatingBottomBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (enableFloatingBottomBarBlur) navBarColor.copy(alpha = 0.5f)
                                else navBarColor,
                            )
                            .border(0.5.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (index == 0) iconColor else textColor),
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(textColor.copy(alpha = 0.1f)),
                    )
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .fillMaxWidth()
                            .background(navBarColor)
                            .padding(top = 2.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (index == 0) navSelectedColor else navUnselectedColor),
                            )
                        }
                    }
                }
            }
        }
    }
}
