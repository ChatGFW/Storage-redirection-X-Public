package com.storage.redirect.x.debug

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.storage.redirect.x.data.repository.ConfigRepository
import com.storage.redirect.x.data.repository.TemplateRepository
import kotlinx.coroutines.runBlocking

// 读取单个应用的重定向配置，JSON 返回
internal fun Context.runConfigGetCommand(packageName: String?, extras: Bundle?): Bundle = runDebugCommand(METHOD_CONFIG_GET, packageName) {
    if (packageName.isNullOrBlank()) {
        return@runDebugCommand debugResult(isOk = false, message = "Missing package name", command = METHOD_CONFIG_GET)
    }
    val userId = extras.optionalInt(EXTRA_USER_ID, 0)
    val config = runBlocking { ConfigRepository().loadPackages(userId, listOf(packageName)) }
    val appConfig = config.getAppConfig(packageName)
        ?: return@runDebugCommand debugResult(
            isOk = false,
            message = "No config for $packageName (user $userId)",
            command = METHOD_CONFIG_GET,
            target = packageName,
        )

    debugResult(
        isOk = true,
        message = "enabled=${appConfig.isEnabled} allowed=${appConfig.allowedRealPaths.size} mappings=${appConfig.pathMappings.size}",
        command = METHOD_CONFIG_GET,
        target = packageName,
        value = Gson().toJson(appConfig),
    )
}

// 列出全部重定向模板，JSON 数组返回
internal fun Context.runTemplatesListCommand(): Bundle = runDebugCommand(METHOD_TEMPLATES_LIST, null) {
    val templates = TemplateRepository().loadTemplates()
    debugResult(
        isOk = true,
        message = "rows=${templates.size}",
        command = METHOD_TEMPLATES_LIST,
        value = Gson().toJson(templates),
    )
}
