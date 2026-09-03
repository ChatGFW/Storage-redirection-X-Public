package com.storage.redirect.x.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.ui.util.LocalTopBarBackdrop
import com.storage.redirect.x.ui.util.surfaceBlur
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

val SRX_TOP_BAR_TRAILING_ICON_END_PADDING = 8.dp

// 顶栏模糊开启时经 backdrop 采样内容，关闭时行为与 miuix TopAppBar 一致
@Composable
fun SrxTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    horizontalPadding: Dp = 12.dp,
) {
    val backdrop = LocalTopBarBackdrop.current
    TopAppBar(
        title = title,
        modifier = modifier.surfaceBlur(backdrop),
        color = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        titlePadding = horizontalPadding,
        navigationIconPadding = horizontalPadding,
        actionIconPadding = horizontalPadding,
    )
}

@Composable
fun SrxSmallTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    horizontalPadding: Dp = 12.dp,
) {
    val backdrop = LocalTopBarBackdrop.current
    SmallTopAppBar(
        title = title,
        modifier = modifier.surfaceBlur(backdrop),
        color = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        titlePadding = horizontalPadding,
        navigationIconPadding = horizontalPadding,
        actionIconPadding = horizontalPadding,
    )
}
