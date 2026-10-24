package com.storage.redirect.x.ui.page

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.ui.icon.AppIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 规则配置页、模板编辑页与应用页共用的对话框组件
private val TEMPLATE_OPTION_CORNER = 14.dp
private const val TEMPLATE_OPTION_PICKED_ALPHA = 0.1f

// 路径输入框；onBrowse 非空时尾部显示浏览按钮
@Composable
internal fun RulePathField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onBrowse: (() -> Unit)? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        trailingIcon = onBrowse?.let { browse ->
            {
                IconButton(onClick = browse, modifier = Modifier.padding(end = 4.dp)) {
                    Icon(
                        imageVector = AppIcons.FolderOpen,
                        contentDescription = stringResource(R.string.rule_config_browse),
                        tint = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        },
    )
}

// 映射方向：上方请求路径指向下方最终路径
@Composable
internal fun MappingDirectionArrow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.ArrowDown,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// 模板单选条目：选中为浅主色底、主色文字与对勾
@Composable
internal fun TemplatePickerOption(
    name: String,
    isPicked: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val colorScheme = MiuixTheme.colorScheme
    val background by animateColorAsState(
        if (isPicked) colorScheme.primary.copy(alpha = TEMPLATE_OPTION_PICKED_ALPHA) else colorScheme.secondaryContainer,
        label = "templateOptionBackground",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TEMPLATE_OPTION_CORNER))
            .background(background)
            .selectable(selected = isPicked, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.body1,
            color = if (isPicked) colorScheme.primary else colorScheme.onSurface,
            fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isPicked) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
