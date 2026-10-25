package com.storage.redirect.x.ui.page

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.storage.redirect.x.R
import com.storage.redirect.x.data.model.FileMonitorEntry
import com.storage.redirect.x.data.model.MonitorActionType
import com.storage.redirect.x.data.model.MonitorResultState
import com.storage.redirect.x.data.model.MonitorWriterType
import com.storage.redirect.x.ui.component.PAGE_CONTENT_TOP_PADDING
import com.storage.redirect.x.ui.component.PAGE_HORIZONTAL_PADDING
import com.storage.redirect.x.ui.component.PAGE_ITEM_GAP
import com.storage.redirect.x.ui.component.STATUS_SUCCESS_COLOR
import com.storage.redirect.x.ui.component.SrxScaffold
import com.storage.redirect.x.ui.component.SrxTopAppBar
import com.storage.redirect.x.ui.component.StatusTag
import com.storage.redirect.x.ui.icon.AppIcons
import com.storage.redirect.x.ui.util.topBarSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 条目卡片：应用 + 时间 / 来源动作 + 结果 / 路径，三行固定结构
private val FILE_MONITOR_CARD_HORIZONTAL_PADDING = 16.dp
private val FILE_MONITOR_CARD_VERTICAL_PADDING = 12.dp
private val FILE_MONITOR_APP_ICON_SIZE = 32.dp
private val FILE_MONITOR_ICON_GAP = 10.dp
private val FILE_MONITOR_SOURCE_TOP_SPACING = 8.dp
private val FILE_MONITOR_PATH_TOP_SPACING = 4.dp

@Composable
fun FileMonitorPage(
    monitorViewModel: FileMonitorViewModel = viewModel(),
    bottomInnerPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val uiState by monitorViewModel.uiState.collectAsState()

    // 每次进入页面时自动刷新
    LaunchedEffect(Unit) {
        monitorViewModel.refresh()
    }

    // 页面离开时重置搜索状态
    DisposableEffect(Unit) {
        onDispose {
            monitorViewModel.resetFilters()
        }
    }

    // 仅缓存应用名和图标，避免重复查询 PackageManager
    val appNames = remember { mutableStateMapOf<String, String>() }
    val appIcons = remember { mutableStateMapOf<String, Drawable?>() }

    val entries = uiState.entries
    val searchQuery = uiState.searchQuery
    val isLoading = uiState.isLoading

    LaunchedEffect(entries) {
        val unresolvedPackages = entries
            .map { it.actorIdentity }
            .filter { it.isNotBlank() && it != "-" }
            .distinct()
            .filter { !appNames.containsKey(it) || !appIcons.containsKey(it) }

        if (unresolvedPackages.isEmpty()) {
            return@LaunchedEffect
        }

        val pm = context.packageManager
        val resolved = withContext(Dispatchers.IO) {
            unresolvedPackages.associateWith { packageName ->
                try {
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    val appName = appInfo.loadLabel(pm).toString()
                    appName to appInfo.loadIcon(pm)
                } catch (_: Exception) {
                    packageName to null
                }
            }
        }

        resolved.forEach { (packageName, appMeta) ->
            appNames[packageName] = appMeta.first
            appIcons[packageName] = appMeta.second
        }
    }

    val actionTypes = listOf(MonitorActionType.OPEN, MonitorActionType.CREATE, MonitorActionType.UPDATE,
        MonitorActionType.DELETE, MonitorActionType.RENAME)
    val filterLabels = listOf(stringResource(R.string.monitor_all)) + actionTypes.map { monitorActionLabel(it) }

    // 搜索与操作类型同时生效，未命中时仍可继续加载历史记录。
    val filteredEntries = remember(entries, searchQuery, uiState.opTypeIndex, appNames.toMap()) {
        entries.filter { entry ->
            (uiState.opTypeIndex == 0 || entry.actionType == actionTypes[uiState.opTypeIndex - 1]) &&
                (searchQuery.isBlank() || listOf(entry.actorIdentity, entry.packageName, entry.path,
                    appNames[entry.actorIdentity].orEmpty()).any { it.contains(searchQuery, ignoreCase = true) })
        }
    }

    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    // 有记录时搜索栏随顶栏固定，列表从两者下方滑过
    val isSearchVisible = entries.isNotEmpty() || searchQuery.isNotBlank()
    SrxScaffold(
        modifier = Modifier.testTag("page_monitor"),
        topBar = {
            Column {
                SrxTopAppBar(
                    title = stringResource(R.string.file_monitor_title),
                    actions = {
                        IconButton(onClick = { monitorViewModel.refresh() }, enabled = !isLoading) {
                            Icon(AppIcons.Refresh, contentDescription = stringResource(R.string.file_monitor_refresh))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                if (isSearchVisible) {
                    Column(Modifier.topBarSurface()) {
                        Spacer(Modifier.height(PAGE_CONTENT_TOP_PADDING))
                        SearchBar(
                            inputField = {
                                InputField(
                                    query = searchQuery,
                                    onQueryChange = { monitorViewModel.updateSearchQuery(it) },
                                    onSearch = {},
                                    expanded = false,
                                    onExpandedChange = {},
                                    label = stringResource(R.string.file_monitor_search_hint)
                                )
                            },
                            onExpandedChange = {},
                            modifier = Modifier.fillMaxWidth(),
                        ) {}
                        OverlayDropdownPreference(
                            title = stringResource(R.string.apps_filter),
                            items = filterLabels,
                            selectedIndex = uiState.opTypeIndex,
                            onSelectedIndexChange = monitorViewModel::updateOpTypeIndex,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val topPadding = innerPadding.calculateTopPadding()
        LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = PAGE_HORIZONTAL_PADDING,
                top = topPadding + PAGE_CONTENT_TOP_PADDING,
                end = PAGE_HORIZONTAL_PADDING,
                bottom = PAGE_CONTENT_TOP_PADDING + bottomInnerPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(PAGE_ITEM_GAP),
        ) {
            item { MonitorStatus(uiState) }
            if (isLoading && entries.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredEntries.isEmpty() && !uiState.hasLoadError) {
                item {
                    Text(stringResource(R.string.file_monitor_empty),
                        Modifier.padding(vertical = 32.dp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
            items(filteredEntries, key = { it.sequence }) { entry ->
                MonitorEntryCard(entry, appNames[entry.actorIdentity] ?: entry.actorIdentity, appIcons[entry.actorIdentity])
            }
            if (uiState.hasMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        if (uiState.isLoadingMore) CircularProgressIndicator(Modifier.size(24.dp))
                        else TextButton(
                            text = stringResource(R.string.file_monitor_load_more),
                            onClick = monitorViewModel::loadMore,
                            enabled = !isLoading,
                            modifier = Modifier.testTag("monitor_load_more"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitorStatus(state: FileMonitorUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!state.isMonitoringEnabled) Text(stringResource(R.string.file_monitor_disabled))
        if (state.lostRecords > 0 || state.writeErrors > 0) {
            Text(stringResource(R.string.file_monitor_gaps, state.lostRecords, state.writeErrors), color = MiuixTheme.colorScheme.error)
        }
        if (state.hasLoadError || state.isDaemonStale) {
            Text(stringResource(if (state.hasLoadError) R.string.file_monitor_load_failed else R.string.file_monitor_daemon_stale),
                color = MiuixTheme.colorScheme.error)
        }
    }
}

@Composable
private fun monitorActionLabel(type: MonitorActionType): String = stringResource(when (type) {
    MonitorActionType.OPEN -> R.string.file_monitor_action_open
    MonitorActionType.CREATE -> R.string.file_monitor_action_create
    MonitorActionType.DELETE -> R.string.file_monitor_action_delete
    MonitorActionType.RENAME -> R.string.file_monitor_action_rename
    MonitorActionType.UPDATE -> R.string.file_monitor_action_update
    MonitorActionType.READ -> R.string.file_monitor_action_read
    MonitorActionType.WRITE -> R.string.file_monitor_action_write
    MonitorActionType.TRUNCATE -> R.string.file_monitor_action_truncate
    MonitorActionType.MAP -> R.string.file_monitor_action_map
    MonitorActionType.SYNC -> R.string.file_monitor_action_sync
})

// 文件监视条目卡片
@Composable
private fun MonitorEntryCard(
    entry: FileMonitorEntry,
    appName: String,
    appIcon: Drawable?
) {
    val colorScheme = MiuixTheme.colorScheme
    val actionLabel = monitorActionLabel(entry.actionType)
    val sourceLabel = if (entry.isDelegatedBySystemWriter) {
        when (entry.delegatedWriterType) {
            MonitorWriterType.MEDIA -> stringResource(R.string.file_monitor_source_media_action, actionLabel)
            MonitorWriterType.DOWNLOAD -> stringResource(R.string.file_monitor_source_download_action, actionLabel)
            MonitorWriterType.MTP -> stringResource(R.string.file_monitor_source_mtp_action, actionLabel)
            MonitorWriterType.SYSTEM -> stringResource(R.string.file_monitor_source_system_writer_action, actionLabel)
            MonitorWriterType.NONE -> stringResource(R.string.file_monitor_source_filesystem_action, actionLabel)
        }
    } else {
        stringResource(R.string.file_monitor_source_filesystem_action, actionLabel)
    }
    val (resultLabel, resultColor) = when (entry.resultState) {
        MonitorResultState.SUCCESS -> stringResource(R.string.file_monitor_result_success) to STATUS_SUCCESS_COLOR
        MonitorResultState.FAILED -> stringResource(R.string.file_monitor_result_failed) to colorScheme.error
    }
    // 路径默认两行，点按卡片展开完整时间与路径。
    var isExpanded by rememberSaveable(entry.timestamp, entry.path) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(
            horizontal = FILE_MONITOR_CARD_HORIZONTAL_PADDING,
            vertical = FILE_MONITOR_CARD_VERTICAL_PADDING,
        ),
        onClick = { isExpanded = !isExpanded },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonitorAppIcon(drawable = appIcon, modifier = Modifier.size(FILE_MONITOR_APP_ICON_SIZE))
            Spacer(Modifier.width(FILE_MONITOR_ICON_GAP))
            Text(
                text = appName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatTimestamp(entry.timestamp),
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(start = FILE_MONITOR_ICON_GAP),
            )
        }
        Row(
            modifier = Modifier.padding(top = FILE_MONITOR_SOURCE_TOP_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (entry.count > 1) stringResource(R.string.file_monitor_repeated, sourceLabel, entry.count) else sourceLabel,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            StatusTag(label = resultLabel, color = resultColor, modifier = Modifier.padding(start = FILE_MONITOR_ICON_GAP))
        }
        if (isExpanded) {
            Text(entry.timestamp, modifier = Modifier.padding(top = FILE_MONITOR_PATH_TOP_SPACING),
                style = MiuixTheme.textStyles.footnote1, color = colorScheme.onSurfaceVariantSummary)
        }
        Text(
            text = entry.displayPath,
            modifier = Modifier.padding(top = FILE_MONITOR_PATH_TOP_SPACING),
            style = MiuixTheme.textStyles.body2,
            color = colorScheme.onSurfaceVariantSummary,
            fontFamily = FontFamily.Monospace,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MonitorAppIcon(drawable: Drawable?, modifier: Modifier = Modifier) {
    if (drawable != null) {
        val bitmap = remember(drawable) {
            drawable.toBitmap(48, 48).asImageBitmap()
        }
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
    }
}

// 提取 HH:mm 时间部分
private fun formatTimestamp(timestamp: String): String {
    val parts = timestamp.split(" ")
    if (parts.size < 2) return timestamp
    val time = parts[1]
    return if (time.length >= 5) time.substring(0, 5) else time
}
