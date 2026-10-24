package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.component.SrxTopAppBar
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar

@Composable
internal fun BrowserScaffold(
    title: String,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val back: @Composable () -> Unit = {
        IconButton(onClick = onBack, modifier = Modifier.testTag("browser_back")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.browser_back), tint = browserColors().text)
        }
    }
    val settings: @Composable () -> Unit = {
        IconButton(onClick = onSettings, modifier = Modifier.testTag("browser_settings")) {
            Icon(Icons.Rounded.MoreHoriz, stringResource(R.string.browser_settings), tint = browserColors().text)
        }
    }
    Scaffold(
        modifier = modifier.imePadding(),
        topBar = { SrxTopAppBar(title = title, navigationIcon = back, actions = { settings() }) },
        bottomBar = bottomBar,
        content = content,
    )
}

@Composable
internal fun BrowserBreadcrumbs(state: PathBrowserState) {
    val scrollState = rememberScrollState()
    val parts = state.currentPath.split('/').filter { it.isNotEmpty() }
    LaunchedEffect(state.currentPath, scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f).horizontalScroll(scrollState), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { state.navigateTo("") }, modifier = Modifier.testTag("browser_root")) {
                Icon(Icons.Rounded.Home, stringResource(R.string.browser_storage), tint = browserColors().summary)
            }
            BreadcrumbButton(
                text = stringResource(if (state.packageName == null) R.string.browser_storage else R.string.browser_private_storage),
                onClick = { state.navigateTo("") },
            )
            parts.forEachIndexed { index, name ->
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
                    modifier = Modifier.size(16.dp), tint = browserColors().summary,
                )
                BreadcrumbButton(name) { state.navigateTo(parts.take(index + 1).joinToString("/")) }
            }
        }
        IconButton(
            onClick = { state.dialog = BrowserDialog.Location },
            modifier = Modifier.testTag("browser_full_path"),
        ) {
            Icon(Icons.Rounded.Info, stringResource(R.string.browser_full_path), tint = browserColors().summary)
        }
    }
}

@Composable
private fun BreadcrumbButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BrowserText(text, modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp), maxLines = 1)
    }
}

@Composable
internal fun BrowserSearch(query: String, onQueryChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    val placeholder = stringResource(R.string.browser_search)
    val leading: @Composable () -> Unit = {
        Icon(Icons.Rounded.Search, null, Modifier.padding(horizontal = 12.dp), tint = browserColors().summary)
    }
    val trailing: @Composable () -> Unit = {
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Rounded.Close, stringResource(R.string.browser_clear_search), tint = browserColors().summary)
            }
        }
    }
    SearchBar(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        insideMargin = DpSize(16.dp, 0.dp),
        inputField = {
            InputField(
                query = query, onQueryChange = onQueryChange, onSearch = { focus.clearFocus() },
                expanded = false, onExpandedChange = {}, label = placeholder,
                leadingIcon = leading, trailingIcon = trailing, modifier = Modifier.testTag("browser_search"),
            )
        },
        onExpandedChange = {},
    ) {}
}
