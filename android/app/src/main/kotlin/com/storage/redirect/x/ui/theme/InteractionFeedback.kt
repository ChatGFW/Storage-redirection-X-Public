package com.storage.redirect.x.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.interfaces.HoldDownInteraction
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 普通反馈使用中性色，按下替代悬停；外部裁切负责圆形和胶囊边界。
@Immutable
internal data class InteractionFeedback(
    val isDark: Boolean,
    val focusColor: Color,
    val shape: Shape = RoundedCornerShape(12.dp),
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        FeedbackNode(interactionSource, isDark, focusColor, shape)
}

@Composable
internal fun rememberInteractionFeedback(shape: Shape): IndicationNodeFactory {
    val isDark = isInDarkTheme(LocalColorMode.current)
    val focusColor = MiuixTheme.colorScheme.primary
    return remember(isDark, focusColor, shape) { InteractionFeedback(isDark, focusColor, shape) }
}

private class FeedbackNode(
    private val interactionSource: InteractionSource,
    private val isDark: Boolean,
    private val focusColor: Color,
    private val shape: Shape,
) : Modifier.Node(), DrawModifierNode {
    private var isPressed = false
    private var isHovered = false
    private var isFocused = false
    private var isHeld = false
    private val alpha = Animatable(0f)
    private var animation: Job? = null

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
                    is HoverInteraction.Enter -> isHovered = true
                    is HoverInteraction.Exit -> isHovered = false
                    is FocusInteraction.Focus -> isFocused = true
                    is FocusInteraction.Unfocus -> isFocused = false
                    is HoldDownInteraction.HoldDown -> isHeld = true
                    is HoldDownInteraction.Release -> isHeld = false
                    else -> return@collect
                }
                val target = when {
                    isPressed || isHeld -> if (isDark) 0.13f else 0.10f
                    isHovered -> if (isDark) 0.07f else 0.055f
                    else -> 0f
                }
                animation?.cancel()
                animation = coroutineScope.launch { alpha.animateTo(target, tween(120)) }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (alpha.value > 0f) {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                color = if (isDark) Color.White else Color.Black,
                alpha = alpha.value,
            )
        }
        if (isFocused) {
            val stroke = 2.dp.toPx()
            val outline = shape.createOutline(Size(size.width - stroke, size.height - stroke), layoutDirection, this)
            translate(stroke / 2, stroke / 2) {
                drawOutline(outline = outline, color = focusColor, style = Stroke(stroke))
            }
        }
    }
}
