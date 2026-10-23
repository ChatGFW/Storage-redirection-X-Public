package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.theme.LocalUiMode
import com.storage.redirect.x.ui.theme.UiMode
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun BrowserSelectionDock(state: PathBrowserState, onConfirm: () -> Unit) {
    val colors = browserColors()
    Column(Modifier.fillMaxWidth().background(colors.background)) {
        HorizontalDivider(thickness = 0.5.dp, color = colors.divider)
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (state.allowExclude) BrowserRuleMode(state)
            if (state.multiSelect) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        Modifier
                            .clickable(role = Role.Button) { state.dialog = BrowserDialog.Selection }
                            .padding(vertical = 10.dp)
                            .testTag("browser_selected_paths"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BrowserText(
                            stringResource(R.string.browser_selected_count, state.selectedPaths.size),
                            color = colors.primary, fontWeight = FontWeight.Medium,
                        )
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(18.dp), tint = colors.summary)
                    }
                    BrowserText(
                        stringResource(if (state.isExcludeMode) R.string.browser_exclude_hint else R.string.browser_cross_directory),
                        color = colors.summary, fontSize = 12.sp,
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Rounded.Folder, null, tint = colors.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BrowserText(stringResource(R.string.browser_current_location), color = colors.summary, fontSize = 12.sp)
                        BrowserText(state.currentPath.ifEmpty { stringResource(R.string.browser_enter_directory) }, maxLines = 1)
                    }
                }
            }
            BrowserButton(
                text = if (state.multiSelect) {
                    stringResource(
                        if (state.isExcludeMode) R.string.browser_exclude_selected else R.string.browser_add_selected,
                        state.selectedPaths.size,
                    )
                } else stringResource(R.string.rule_config_browser_use_current),
                onClick = onConfirm, enabled = state.canConfirm, primary = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("browser_confirm"),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserRuleMode(state: PathBrowserState) {
    val labels = listOf(stringResource(R.string.browser_allow_access), stringResource(R.string.browser_exclude_directory))
    if (LocalUiMode.current == UiMode.Material) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("browser_rule_mode")) {
            labels.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = state.isExcludeMode == (index == 1),
                    onClick = { state.isExcludeMode = index == 1 },
                    shape = SegmentedButtonDefaults.itemShape(index, labels.size),
                ) { Text(label) }
            }
        }
        return
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val itemWidth = (maxWidth - 15.dp) / 2
        TabRowWithContour(
            tabs = labels,
            selectedTabIndex = if (state.isExcludeMode) 1 else 0,
            onTabSelected = { state.isExcludeMode = it == 1 },
            minWidth = itemWidth, maxWidth = itemWidth,
            modifier = Modifier.testTag("browser_rule_mode"),
            colors = TabRowDefaults.tabRowColors(
                backgroundColor = MiuixTheme.colorScheme.secondaryVariant,
                selectedContentColor = if (state.isExcludeMode) browserColors().error else browserColors().text,
            ),
        )
    }
}
