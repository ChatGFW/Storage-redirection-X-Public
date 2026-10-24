package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storage.redirect.x.ui.component.PillButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal data class BrowserColors(
    val surface: Color,
    val text: Color,
    val summary: Color,
    val primary: Color,
    val selected: Color,
    val error: Color,
    val errorSelected: Color,
    val folder: Color,
)

@Composable
internal fun browserColors(): BrowserColors = MiuixTheme.colorScheme.let {
    BrowserColors(
        it.surfaceContainer, it.onSurface, it.onSurfaceVariantSummary, it.primary,
        it.tertiaryContainer, it.error, it.errorContainer, Color(0xFFF0BD56),
    )
}

// 排除模式下强调色与选中底色整体换成错误色
@Composable
internal fun PathBrowserState.accentColor(): Color =
    if (isExcludeMode) browserColors().error else browserColors().primary

@Composable
internal fun PathBrowserState.selectedRowColor(): Color =
    if (isExcludeMode) browserColors().errorSelected else browserColors().selected

@Composable
internal fun BrowserText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = browserColors().text,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text, modifier = modifier, color = color, fontSize = fontSize,
        fontWeight = fontWeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun BrowserButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    PillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        colors = if (primary) ButtonDefaults.textButtonColorsPrimary() else ButtonDefaults.textButtonColors(),
    )
}

@Composable
internal fun BrowserCheckbox(
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp), Alignment.Center) {
        Checkbox(
            state = if (isSelected) ToggleableState.On else ToggleableState.Off,
            onClick = onClick,
            modifier = modifier.semantics { contentDescription = description },
            colors = CheckboxDefaults.checkboxColors(checkedBackgroundColor = color),
        )
    }
}
