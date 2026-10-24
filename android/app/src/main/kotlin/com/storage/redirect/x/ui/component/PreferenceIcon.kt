package com.storage.redirect.x.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 偏好项左侧图标：与标题间隔 12dp，禁用时与 miuix 禁用文字同色
@Composable
fun PreferenceIcon(icon: ImageVector, isEnabled: Boolean = true) {
    val colorScheme = MiuixTheme.colorScheme
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.padding(end = 12.dp),
        tint = if (isEnabled) colorScheme.onBackground else colorScheme.disabledOnSecondaryVariant,
    )
}
