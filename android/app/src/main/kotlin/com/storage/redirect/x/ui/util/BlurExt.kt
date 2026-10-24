package com.storage.redirect.x.ui.util

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 顶栏模糊总开关，由 App 层根据外观设置 Provide
val LocalTopBarBlur = staticCompositionLocalOf { true }

// 当前页面的顶栏 backdrop，由 SrxScaffold 在模糊启用时 Provide
val LocalTopBarBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

// 毛玻璃表面：模糊 backdrop 后叠加 surface 蒙层，顶栏与底栏共用，参数照搬 only_player
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

// 顶栏下方固定控件（如搜索栏）的底，与顶栏一同采样内容；模糊关闭时为不透明 surface
@Composable
fun Modifier.topBarSurface(): Modifier {
    val backdrop = LocalTopBarBackdrop.current ?: return background(MiuixTheme.colorScheme.surface)
    return surfaceBlur(backdrop)
}
