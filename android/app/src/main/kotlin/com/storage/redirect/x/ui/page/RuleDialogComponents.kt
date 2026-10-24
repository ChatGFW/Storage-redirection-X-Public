package com.storage.redirect.x.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.data.model.RedirectTemplate
import com.storage.redirect.x.ui.icon.AppIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 规则配置页、模板编辑页与应用页共用的对话框组件
private val TEMPLATE_PICKER_MAX_HEIGHT = 360.dp

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

// 模板单选列表：照 only_player 的 OptionsDialog，点选即进入下一步，只保留取消
@Composable
internal fun TemplatePickerContent(
    templates: List<RedirectTemplate>,
    testTag: String,
    onPick: (RedirectTemplate) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag(testTag),
    ) {
        if (templates.isEmpty()) {
            Text(
                text = stringResource(R.string.apps_template_empty),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TEMPLATE_PICKER_MAX_HEIGHT)
                    .selectableGroup(),
            ) {
                items(templates, key = { it.id }) { template ->
                    RadioButtonPreference(
                        title = template.name,
                        selected = false,
                        onClick = { onPick(template) },
                        modifier = Modifier.testTag("${testTag}_item_${template.id}"),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            text = stringResource(R.string.common_cancel),
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTag}_cancel"),
        )
    }
}
