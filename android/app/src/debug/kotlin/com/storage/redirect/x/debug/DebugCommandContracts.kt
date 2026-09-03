package com.storage.redirect.x.debug

import android.os.Bundle

internal const val METHOD_PAGE_OPEN = "page.open"
internal const val METHOD_APPS_LIST = "apps.list"
internal const val METHOD_APPS_SHARED_UID = "apps.shareduid"
internal const val METHOD_BROWSER_LIST = "browser.list"
internal const val METHOD_MONITOR_RECENT = "monitor.recent"
internal const val METHOD_CONFIG_GET = "config.get"
internal const val METHOD_TEMPLATES_LIST = "templates.list"

internal const val EXTRA_FILTER = "filter"
internal const val EXTRA_LIMIT = "limit"
internal const val EXTRA_USER_ID = "user_id"

private const val KEY_OK = "ok"
private const val KEY_MESSAGE = "message"
private const val KEY_COMMAND = "command"
private const val KEY_TARGET = "target"
private const val KEY_VALUE = "value"

// Binder 传输上限约 1MB，列表类指令统一截断到该行数
internal const val MAX_LIST_ROWS = 500

internal fun debugResult(
    isOk: Boolean,
    message: String,
    command: String? = null,
    target: String? = null,
    value: String? = null,
): Bundle = Bundle().apply {
    putBoolean(KEY_OK, isOk)
    putString(KEY_MESSAGE, message)
    putString(KEY_COMMAND, command)
    putString(KEY_TARGET, target)
    putString(KEY_VALUE, value)
}

// 命中异常统一转失败结果，避免把堆栈抛给调用方
internal inline fun runDebugCommand(
    command: String,
    target: String?,
    block: () -> Bundle,
): Bundle = try {
    block()
} catch (e: Exception) {
    debugResult(isOk = false, message = e.message ?: "Failed: $command", command = command, target = target)
}

// content call 的 extra 类型由调用方指定，读取失败时回退默认值
internal fun Bundle?.optionalInt(key: String, default: Int): Int {
    if (this == null || !containsKey(key)) return default
    return getInt(key, default)
}

internal fun Bundle?.optionalString(key: String): String? {
    if (this == null) return null
    return getString(key)?.takeIf { it.isNotBlank() }
}
