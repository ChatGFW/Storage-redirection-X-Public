package com.storage.redirect.x.ui.page.pathbrowser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.Checkbox as MaterialCheckbox
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
import com.storage.redirect.x.ui.theme.LocalUiMode
import com.storage.redirect.x.ui.theme.UiMode
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal data class BrowserColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val summary: Color,
    val primary: Color,
    val selected: Color,
    val divider: Color,
    val error: Color,
    val folder: Color,
)

@Composable
internal fun browserColors(): BrowserColors = when (LocalUiMode.current) {
    UiMode.Miuix -> MiuixTheme.colorScheme.let {
        BrowserColors(
            it.surface, it.surfaceContainer, it.onSurface, it.onSurfaceVariantSummary,
            it.primary, it.tertiaryContainer, it.dividerLine, it.error, Color(0xFFF0BD56),
        )
    }
    UiMode.Material -> MaterialTheme.colorScheme.let {
        BrowserColors(
            it.surface, it.surfaceContainerLow, it.onSurface, it.onSurfaceVariant,
            it.primary, it.primaryContainer, it.outlineVariant, it.error, it.tertiary,
        )
    }
}

@Composable
internal fun BrowserText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = browserColors().text,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> Text(
            text, modifier = modifier, color = color, fontSize = fontSize,
            fontWeight = fontWeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
        )
        UiMode.Material -> MaterialText(
            text, modifier = modifier, color = color, fontSize = fontSize,
            fontWeight = fontWeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun BrowserIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> IconButton(onClick = onClick, modifier = modifier) { content() }
        UiMode.Material -> MaterialIconButton(onClick = onClick, modifier = modifier) { content() }
    }
}

@Composable
internal fun BrowserButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    when {
        LocalUiMode.current == UiMode.Miuix -> TextButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = if (primary) ButtonDefaults.textButtonColorsPrimary() else ButtonDefaults.textButtonColors(),
        )
        primary -> Button(onClick = onClick, modifier = modifier, enabled = enabled) {
            MaterialText(text)
        }
        else -> FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            MaterialText(text)
        }
    }
}

@Composable
internal fun BrowserCheckbox(
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkboxModifier = modifier.semantics { contentDescription = description }
    when (LocalUiMode.current) {
        UiMode.Miuix -> Box(Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp), Alignment.Center) {
            Checkbox(
                state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                onClick = onClick,
                modifier = checkboxModifier,
            )
        }
        UiMode.Material -> MaterialCheckbox(
            checked = isSelected, onCheckedChange = { onClick() }, modifier = checkboxModifier,
        )
    }
}
