package com.storage.redirect.x.debug

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Process

// Debug 包专用的指令入口，仅授权 shell / root / 自身进程调用
class DebugCommandProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?,
    ): Bundle {
        if (!isAuthorizedCaller()) {
            return debugResult(isOk = false, message = "Unauthorized caller", command = method, target = arg)
        }
        val context = context
            ?: return debugResult(isOk = false, message = "Context is not ready", command = method, target = arg)

        return when (method) {
            METHOD_PAGE_OPEN -> context.openDebugPage(arg)
            METHOD_APPS_LIST -> context.runAppsListCommand(arg, extras)
            METHOD_APPS_SHARED_UID -> context.runAppsSharedUidCommand(arg)
            METHOD_BROWSER_LIST -> context.runBrowserListCommand(arg, extras)
            METHOD_MONITOR_RECENT -> context.runMonitorRecentCommand(extras)
            METHOD_CONFIG_GET -> context.runConfigGetCommand(arg, extras)
            METHOD_TEMPLATES_LIST -> context.runTemplatesListCommand()
            else -> debugResult(isOk = false, message = "Unknown method: $method", command = method, target = arg)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    private fun isAuthorizedCaller(): Boolean {
        val callingUid = Binder.getCallingUid()
        return callingUid == Process.SHELL_UID ||
            callingUid == Process.ROOT_UID ||
            callingUid == Process.myUid()
    }
}
