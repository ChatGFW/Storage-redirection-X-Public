package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
internal fun BrowserDirectoryList(state: PathBrowserState, modifier: Modifier = Modifier) {
    val entries = state.visibleDirectories
    val listState = rememberLazyListState()
    LaunchedEffect(state.currentPath, state.query, state.showHidden, state.isDescending) {
        listState.scrollToItem(0)
    }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrowserText(
                stringResource(R.string.browser_directory_count, entries.size),
                color = browserColors().summary, fontSize = 12.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrowserText(
                    stringResource(if (state.isDescending) R.string.browser_sort_descending else R.string.browser_sort_ascending),
                    color = browserColors().summary, fontSize = 12.sp,
                )
                IconButton(onClick = { state.isDescending = !state.isDescending }) {
                    Icon(Icons.AutoMirrored.Rounded.Sort, stringResource(R.string.browser_sort), tint = browserColors().summary)
                }
            }
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.hasLoadError || entries.isEmpty() -> BrowserEmptyState(state)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("browser_directories"),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                itemsIndexed(entries, key = { _, path -> path }) { index, path ->
                    BrowserDirectoryRow(state, path, index, entries.size)
                }
            }
        }
    }
}

@Composable
private fun BrowserEmptyState(state: PathBrowserState) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.FolderOpen, null, Modifier.size(40.dp), tint = browserColors().summary)
        BrowserText(
            stringResource(
                when {
                    state.hasLoadError -> R.string.rule_config_browser_load_failed
                    state.query.isNotBlank() -> R.string.browser_no_results
                    else -> R.string.rule_config_browser_empty
                }
            ),
            color = if (state.hasLoadError) browserColors().error else browserColors().summary,
        )
        when {
            state.hasLoadError -> BrowserButton(stringResource(R.string.common_retry), { state.reloadKey++ })
            state.query.isNotEmpty() -> BrowserButton(stringResource(R.string.browser_clear_search), { state.query = "" })
            state.multiSelect && state.canSelect(state.currentPath) -> BrowserButton(
                stringResource(
                    if (state.currentPath in state.selectedPaths) R.string.browser_deselect_current
                    else R.string.rule_config_browser_use_current
                ),
                { state.togglePath(state.currentPath) },
            )
        }
    }
}

@Composable
private fun DirectoryTitle(path: String) {
    BrowserText(path.substringAfterLast('/'), fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
}

@Composable
private fun DirectorySummary(isEnabled: Boolean) {
    BrowserText(
        stringResource(if (isEnabled) R.string.browser_folder else R.string.browser_restricted),
        fontSize = 12.sp, color = browserColors().summary, maxLines = 1,
    )
}

@Composable
private fun DirectoryTrailing(state: PathBrowserState, path: String) {
    if (!state.canSelect(path)) {
        Icon(Icons.Rounded.Lock, stringResource(R.string.browser_restricted), Modifier.size(20.dp), tint = browserColors().summary)
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(18.dp), tint = browserColors().summary)
        if (state.multiSelect) {
            BrowserCheckbox(
                isSelected = path in state.selectedPaths,
                description = stringResource(R.string.browser_select_directory, path.substringAfterLast('/')),
                onClick = { state.togglePath(path) },
                modifier = Modifier.testTag("browser_select_$path"),
            )
        }
    }
}

@Composable
private fun BrowserDirectoryRow(state: PathBrowserState, path: String, index: Int, count: Int) {
    val colors = browserColors()
    val isSelected = path in state.selectedPaths && state.multiSelect
    val isEnabled = state.canSelect(path)
    val onClick = { state.navigateTo(path) }
    val modifier = Modifier.fillMaxWidth().testTag("browser_directory_$path")
    val top = if (index == 0) 16.dp else 0.dp
    val bottom = if (index == count - 1) 16.dp else 0.dp
    Card(
        modifier = modifier.clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)),
        cornerRadius = 0.dp,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = if (isSelected) colors.selected else colors.surface),
        onClick = if (isEnabled) onClick else null,
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp).alpha(if (isEnabled) 1f else 0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Folder, null, Modifier.size(34.dp), tint = colors.folder)
            Column(Modifier.weight(1f).padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DirectoryTitle(path)
                DirectorySummary(isEnabled)
            }
            DirectoryTrailing(state, path)
        }
        if (index < count - 1) {
            Box(Modifier.fillMaxWidth().padding(start = 62.dp, end = 16.dp).height(0.5.dp).background(colors.divider))
        }
    }
}
