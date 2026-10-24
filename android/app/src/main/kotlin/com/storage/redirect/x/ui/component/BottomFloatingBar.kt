package com.storage.redirect.x.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors

// 底部悬浮胶囊：离屏幕底至少 16dp，有导航条时离导航条 12dp，两侧至少留 16dp 避开屏幕圆角
private val FLOATING_BAR_MIN_BOTTOM_OFFSET = 16.dp
private val FLOATING_BAR_NAV_GAP = 12.dp
private val FLOATING_BAR_SIDE_PADDING = 16.dp
private val FLOATING_BAR_INSIDE_PADDING = 6.dp
private val FLOATING_BAR_ITEM_GAP = 4.dp

// 栏内控件统一 44dp 高的胶囊
val FLOATING_BAR_ITEM_HEIGHT = 44.dp
private val FLOATING_BAR_BUTTON_HORIZONTAL_PADDING = 22.dp

// 在 Box 底部居中显示；onSpaceChange 回报胶囊连同离底距离的总高度，供列表底部留白
@Composable
fun BoxScope.BottomFloatingBar(
    isVisible: Boolean,
    onSpaceChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val density = LocalDensity.current
    val bottomOffset = max(
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + FLOATING_BAR_NAV_GAP,
        FLOATING_BAR_MIN_BOTTOM_OFFSET,
    )
    AnimatedVisibility(
        visible = isVisible,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = FLOATING_BAR_SIDE_PADDING, end = FLOATING_BAR_SIDE_PADDING, bottom = bottomOffset),
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        FloatingToolbar(
            modifier = modifier.onSizeChanged { onSpaceChange(with(density) { it.height.toDp() } + bottomOffset) },
            outSidePadding = PaddingValues(0.dp),
        ) {
            Row(
                modifier = Modifier.padding(FLOATING_BAR_INSIDE_PADDING),
                horizontalArrangement = Arrangement.spacedBy(FLOATING_BAR_ITEM_GAP),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: TextButtonColors = ButtonDefaults.textButtonColors(),
) {
    TextButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        cornerRadius = FLOATING_BAR_ITEM_HEIGHT / 2,
        minHeight = FLOATING_BAR_ITEM_HEIGHT,
        colors = colors,
        insideMargin = PaddingValues(horizontal = FLOATING_BAR_BUTTON_HORIZONTAL_PADDING),
    )
}
