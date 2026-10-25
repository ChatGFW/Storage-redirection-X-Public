package com.storage.redirect.x.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.storage.redirect.x.R
import com.storage.redirect.x.data.model.AppUpdateInfo
import com.storage.redirect.x.data.repository.UpdateResult
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun UpdateDialogs(result: UpdateResult?, isVisible: Boolean, onDismiss: () -> Unit) {
    if (!isVisible) return
    when (result) {
        is UpdateResult.Available -> AppUpdateDialog(result.info, onDismiss)
        UpdateResult.UpToDate, UpdateResult.Failed -> WindowDialog(
            show = true,
            title = stringResource(R.string.settings_update),
            onDismissRequest = onDismiss,
        ) {
            val message = if (result == UpdateResult.UpToDate) R.string.settings_update_latest
                else R.string.settings_update_failed
            Text(stringResource(message))
            Spacer(Modifier.height(12.dp))
            TextButton(
                text = stringResource(R.string.common_ok),
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColorsPrimary(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        null -> Unit
    }
}

@Composable
private fun AppUpdateDialog(info: AppUpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val language = LocalResources.current.configuration.locales[0].language
    val notes = remember(info.releaseNotes, language) { localizedReleaseNotes(info.releaseNotes, language) }
    WindowDialog(
        show = true,
        title = stringResource(R.string.settings_update_dialog_title, info.latestVersion),
        onDismissRequest = onDismiss,
    ) {
        if (notes.isNotBlank()) {
            Text(
                notes,
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()).testTag("update_release_notes"),
            )
            Spacer(Modifier.height(12.dp))
        }
        Row {
            TextButton(
                text = stringResource(R.string.settings_update_not_now),
                onClick = onDismiss,
                modifier = Modifier.weight(1f).testTag("update_dialog_cancel"),
            )
            Spacer(Modifier.width(12.dp))
            TextButton(
                text = stringResource(R.string.settings_update_download),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                modifier = Modifier.weight(1f).testTag("update_dialog_download"),
                onClick = {
                    try {
                        uriHandler.openUri(info.releaseUrl)
                        onDismiss()
                    } catch (_: IllegalArgumentException) {
                        Toast.makeText(context, R.string.settings_update_open_failed, Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}

internal fun localizedReleaseNotes(body: String, language: String): String {
    // 正式发布为英文、分隔线、中文；测试版单语说明保留原文。
    val sections = body.split(Regex("""(?m)^---[ \t]*\r?$"""), limit = 2)
    val notes = if (language == "zh") sections.last() else sections.first()
    return notes.trim().lineSequence().joinToString("\n") { line ->
        val text = line.replace(Regex("""\[([^]]+)]\([^)]+\)"""), "$1").replace("`", "")
        when {
            text.startsWith("- ") -> "• ${text.removePrefix("- ")}"
            text.startsWith("## ") -> text.removePrefix("## ")
            else -> text
        }
    }
}
