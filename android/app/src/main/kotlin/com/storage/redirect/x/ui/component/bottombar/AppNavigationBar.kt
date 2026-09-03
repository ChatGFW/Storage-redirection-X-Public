package com.storage.redirect.x.ui.component.bottombar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.NavigationBar as MaterialNavigationBar
import androidx.compose.material3.NavigationBarItem as MaterialNavigationBarItem
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.ui.theme.LocalUiMode
import com.storage.redirect.x.ui.theme.UiMode
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText

private val NAV_BAR_CONTENT_HEIGHT = 72.dp

// 固定底栏按 uiMode 分发：Material 用 Material3 NavigationBar，Miuix 用 miuix NavigationBar + 自绘条目
// Miuix 侧在模糊开启时经 textureBlur 采样内容，菜单展开时由 enabled 拦截点击
@Composable
fun AppNavigationBar(
    labels: List<String>,
    icons: List<ImageVector>,
    selectedIndex: Int,
    enabled: Boolean,
    backdrop: Backdrop?,
    onSelect: (Int) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Material -> MaterialNavigationBar {
            icons.forEachIndexed { index, icon ->
                MaterialNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { if (enabled) onSelect(index) },
                    icon = { MaterialIcon(icon, contentDescription = labels[index]) },
                    label = { MaterialText(labels[index]) },
                )
            }
        }

        UiMode.Miuix -> {
            val isBlurEnabled = backdrop != null
            MiuixNavigationBar(
                color = if (isBlurEnabled) Color.Transparent else MiuixTheme.colorScheme.surface,
                modifier = Modifier.surfaceBlur(backdrop),
            ) {
                icons.forEachIndexed { index, icon ->
                    MiuixBottomBarItem(
                        label = labels[index],
                        icon = icon,
                        isSelected = selectedIndex == index,
                        enabled = enabled,
                        onClick = { onSelect(index) },
                    )
                }
            }
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

// 毛玻璃表面：模糊 backdrop 后叠加 surface 蒙层，参数与顶栏保持一致
@Composable
fun Modifier.surfaceBlur(
    backdrop: Backdrop?,
    shape: Shape = RectangleShape,
): Modifier {
    if (backdrop == null) return this
    return textureBlur(
        backdrop = backdrop,
        shape = shape,
        blurRadius = 25f,
        colors = BlurDefaults.blurColors(
            blendColors = listOf(
                BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.8f)),
            ),
        ),
    )
}
