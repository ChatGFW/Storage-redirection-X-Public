package com.storage.redirect.x.ui.component.bottombar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.ui.util.surfaceBlur
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText

// 不含系统导航栏；页面按此为叠在内容上的固定底栏留白
internal val NAV_BAR_CONTENT_HEIGHT = 72.dp

// 固定底栏：miuix NavigationBar + 自绘条目
// 模糊开启时经 textureBlur 采样内容，菜单展开时由 enabled 拦截点击
@Composable
fun AppNavigationBar(
    labels: List<String>,
    icons: List<ImageVector>,
    selectedIcons: List<ImageVector>,
    selectedIndex: Int,
    enabled: Boolean,
    backdrop: Backdrop?,
    onSelect: (Int) -> Unit,
) {
    MiuixNavigationBar(
        color = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface,
        modifier = Modifier.surfaceBlur(backdrop),
    ) {
        icons.forEachIndexed { index, icon ->
            val isSelected = selectedIndex == index
            MiuixBottomBarItem(
                label = labels[index],
                icon = if (isSelected) selectedIcons[index] else icon,
                isSelected = isSelected,
                enabled = enabled,
                onClick = { onSelect(index) },
            )
        }
    }
}

// Miuix 底栏条目：图标 + 文本竖排，选中态加粗（与 miuix 原生 NavigationBar 视觉一致）
@Composable
private fun RowScope.MiuixBottomBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (isSelected) {
        MiuixTheme.colorScheme.onSurfaceContainer
    } else {
        MiuixTheme.colorScheme.onSurfaceContainer.copy(alpha = 0.45f)
    }
    Column(
        modifier = Modifier
            .height(NAV_BAR_CONTENT_HEIGHT)
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MiuixIcon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp),
        )
        MiuixText(
            text = label,
            color = tint,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
