package com.storage.redirect.x.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.storage.redirect.x.ui.theme.rememberInteractionFeedback
import top.yukonga.miuix.kmp.basic.IconButton

// 图标按钮的触摸、悬停和焦点共用圆形边界。
@Composable
fun SrxIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDownState: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIndication provides rememberInteractionFeedback(CircleShape)) {
        IconButton(
            onClick = onClick,
            modifier = modifier.clip(CircleShape),
            enabled = enabled,
            holdDownState = holdDownState,
            content = content,
        )
    }
}
