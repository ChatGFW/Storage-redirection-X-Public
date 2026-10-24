package com.storage.redirect.x.ui.navigation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// 转场参数照搬 only_player 的 PageTransitions：500ms 弹簧曲线，下层页面位移 1/4 宽度
internal const val PAGE_TRANSITION_DURATION_MS = 500
internal const val PAGE_PARALLAX_FRACTION = 0.25f

@Immutable
private class NavTransitionEasing(
    response: Float,
    damping: Float,
) : Easing {
    private val r: Float
    private val w: Float
    private val c2: Float

    init {
        val omega = 2.0 * PI / response
        val k = omega * omega
        val c = damping * 4.0 * PI / response
        w = (sqrt(4.0 * k - c * c) / 2.0).toFloat()
        r = (-c / 2.0).toFloat()
        c2 = r / w
    }

    override fun transform(fraction: Float): Float {
        val t = fraction.toDouble()
        val decay = exp(r * t)
        return (decay * (-cos(w * t) + c2 * sin(w * t)) + 1.0).toFloat()
    }
}

internal val PageTransitionEasing: Easing = NavTransitionEasing(response = 0.8f, damping = 0.95f)

// 驱动的是线性时间进度，曲线在渲染时套用，手势中途松手可沿原曲线接着播完
internal fun pageTimeSpec(remainingFraction: Float = 1f): TweenSpec<Float> = tween(
    durationMillis = (PAGE_TRANSITION_DURATION_MS * remainingFraction).roundToInt(),
    easing = LinearEasing,
)
