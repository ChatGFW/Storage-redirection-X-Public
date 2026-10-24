package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.component.SrxBackButton
import com.storage.redirect.x.ui.component.TOP_BAR_POPUP_POSITION_PROVIDER
import com.storage.redirect.x.ui.icon.AppIcons
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 单行小标题栏，导航区与操作区边距沿用 miuix 顶栏默认值
private val BROWSER_TOP_BAR_HEIGHT = 56.dp
private val BROWSER_TOP_BAR_EDGE_PADDING = 16.dp
private val BROWSER_TITLE_START_PADDING = 4.dp

// 顶栏两种形态：目录标题（点按展开路径面板）与搜索栏
@Composable
internal fun BrowserTopBar(state: PathBrowserState, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
            .height(BROWSER_TOP_BAR_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isSearchActive) {
            BrowserSearchBar(state)
        } else {
            BrowserTitleBar(state, onBack)
        }
    }
}

@Composable
private fun RowScope.BrowserTitleBar(state: PathBrowserState, onBack: () -> Unit) {
    val colors = browserColors()
    val caretRotation by animateFloatAsState(if (state.isPathPanelExpanded) 180f else 0f, label = "caret")
    Box(Modifier.padding(start = BROWSER_TOP_BAR_EDGE_PADDING)) {
        SrxBackButton(onClick = onBack, modifier = Modifier.testTag("browser_back"))
    }
    Row(
        Modifier
            .weight(1f)
            .padding(start = BROWSER_TITLE_START_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) { state.isPathPanelExpanded = !state.isPathPanelExpanded }
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .testTag("browser_title"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrowserText(
                text = state.currentName.ifEmpty { rootLabel(state) },
                modifier = Modifier.weight(1f, fill = false),
                fontSize = MiuixTheme.textStyles.title3.fontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Icon(
                AppIcons.ChevronDown,
                contentDescription = stringResource(R.string.browser_current_location),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp)
                    .rotate(caretRotation),
                tint = colors.summary,
            )
        }
    }
    Row(Modifier.padding(end = BROWSER_TOP_BAR_EDGE_PADDING), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { state.isSearchActive = true }, modifier = Modifier.testTag("browser_search_open")) {
            Icon(AppIcons.Search, stringResource(R.string.browser_search), tint = colors.text)
        }
        BrowserMoreMenu(state)
    }
}

// ⋯ 菜单：排序、隐藏目录与回到根目录
@Composable
private fun BrowserMoreMenu(state: PathBrowserState) {
    var isExpanded by remember { mutableStateOf(false) }
    val options = buildList {
        add(stringResource(R.string.browser_sort_ascending) to !state.isDescending)
        add(stringResource(R.string.browser_sort_descending) to state.isDescending)
        add(stringResource(R.string.browser_show_hidden) to state.showHidden)
        if (state.currentPath.isNotEmpty()) {
            add(stringResource(R.string.browser_go_to_root, rootLabel(state)) to false)
        }
    }
    Box {
        IconButton(onClick = { isExpanded = true }, modifier = Modifier.testTag("browser_settings")) {
            Icon(AppIcons.More, stringResource(R.string.browser_settings), tint = browserColors().text)
        }
        OverlayListPopup(
            show = isExpanded,
            popupPositionProvider = TOP_BAR_POPUP_POSITION_PROVIDER,
            alignment = PopupPositionProvider.Align.TopEnd,
            onDismissRequest = { isExpanded = false },
        ) {
            ListPopupColumn {
                options.forEachIndexed { index, (text, isSelected) ->
                    DropdownImpl(
                        text = text,
                        optionSize = options.size,
                        isSelected = isSelected,
                        index = index,
                        dropdownColors = DropdownDefaults.dropdownColors(),
                        onSelectedIndexChange = { selected ->
                            isExpanded = false
                            when (selected) {
                                0 -> state.isDescending = false
                                1 -> state.isDescending = true
                                2 -> state.showHidden = !state.showHidden
                                else -> state.navigateTo("")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BrowserSearchBar(state: PathBrowserState) {
    val focus = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val leading: @Composable () -> Unit = {
        Icon(AppIcons.Search, null, Modifier.padding(horizontal = 12.dp), tint = browserColors().summary)
    }
    val trailing: @Composable () -> Unit = {
        if (state.query.isNotEmpty()) {
            IconButton(onClick = { state.query = "" }) {
                Icon(AppIcons.Close, stringResource(R.string.browser_clear_search), tint = browserColors().summary)
            }
        }
    }
    Box(Modifier.padding(start = BROWSER_TOP_BAR_EDGE_PADDING)) {
        SrxBackButton(onClick = state::closeSearch, modifier = Modifier.testTag("browser_search_close"))
    }
    SearchBar(
        modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp, end = BROWSER_TOP_BAR_EDGE_PADDING),
        insideMargin = DpSize(0.dp, 0.dp),
        inputField = {
            InputField(
                query = state.query, onQueryChange = { state.query = it }, onSearch = { focus.clearFocus() },
                expanded = false, onExpandedChange = {}, label = stringResource(R.string.browser_search),
                leadingIcon = leading, trailingIcon = trailing,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .testTag("browser_search"),
            )
        },
        onExpandedChange = {},
    ) {}
}

@Composable
internal fun rootLabel(state: PathBrowserState): String = stringResource(
    if (state.packageName == null) R.string.browser_storage else R.string.browser_private_storage,
)
