package com.storage.redirect.x.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 顶栏模糊总开关，由 App 层根据外观设置 Provide
val LocalTopBarBlur = staticCompositionLocalOf { false }

// 当前页面的顶栏 backdrop，由内容容器在模糊启用时 Provide
val LocalTopBarBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

// 内容层创建顶栏可采样的 backdrop：模糊关闭或渲染不支持时返回 null
@Composable
fun rememberTopBarBackdrop(enableBlur: Boolean): LayerBackdrop? {
    if (!enableBlur || !isRuntimeShaderSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

// 毛玻璃表面：模糊 backdrop 后叠加 surface 蒙层，参数与底栏保持一致
@Composable
fun Modifier.surfaceBlur(
    backdrop: Backdrop?,
): Modifier {
    if (backdrop == null) return this
    return textureBlur(
        backdrop = backdrop,
        shape = RectangleShape,
        blurRadius = 25f * LocalDensity.current.density,
        colors = BlurColors(
            blendColors = listOf(
                BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.87f)),
            ),
        ),
    )
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.surfaceBlur(backdrop),
    ) {
        content()
    }
}
