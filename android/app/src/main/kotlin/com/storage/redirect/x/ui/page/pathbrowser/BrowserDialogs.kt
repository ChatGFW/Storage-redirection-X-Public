package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.BuildConfig
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.icon.AppIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.window.WindowDialog

// 已选清单：点条目跳到该目录，× 移出选择
@Composable
internal fun PathBrowserDialogs(state: PathBrowserState) {
    if (!state.isSelectionDialogVisible) return
    WindowDialog(
        show = true,
        title = stringResource(R.string.browser_selected_count, state.selectedPaths.size),
        onDismissRequest = { state.isSelectionDialogVisible = false },
    ) {
        Column(Modifier.semantics { testTagsAsResourceId = BuildConfig.IS_DEBUG_BUILD }.testTag("browser_dialog")) {
            BrowserSelectedPaths(state)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.selectedPaths.isNotEmpty()) {
                    BrowserButton(
                        text = stringResource(R.string.browser_clear_selection),
                        onClick = state::clearSelection,
                        modifier = Modifier.weight(1f).testTag("browser_clear_selection"),
                    )
                }
                BrowserButton(
                    text = stringResource(R.string.common_close),
                    onClick = { state.isSelectionDialogVisible = false }, primary = true,
                    modifier = Modifier.weight(1f).testTag("browser_dialog_close"),
                )
            }
        }
    }
}

@Composable
private fun BrowserSelectedPaths(state: PathBrowserState) {
    if (state.selectedPaths.isEmpty()) {
        BrowserText(
            stringResource(R.string.browser_no_selection), color = browserColors().summary,
            modifier = Modifier.padding(bottom = 20.dp),
        )
        return
    }
    LazyColumn(
        Modifier.fillMaxWidth().heightIn(max = 300.dp).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(state.selectedPaths.toList(), key = { it }) { path ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.weight(1f).clickable {
                        state.isSelectionDialogVisible = false
                        state.navigateTo(path)
                    }.padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BrowserText(path.substringAfterLast('/'), fontWeight = FontWeight.Medium, maxLines = 1)
                    BrowserText(path, fontSize = 12.sp, color = browserColors().summary, maxLines = 2)
                }
                IconButton(onClick = { state.togglePath(path) }, modifier = Modifier.testTag("browser_remove_$path")) {
                    Icon(
                        AppIcons.Close, stringResource(R.string.browser_remove_directory, path),
                        tint = browserColors().summary,
                    )
                }
            }
        }
    }
}
