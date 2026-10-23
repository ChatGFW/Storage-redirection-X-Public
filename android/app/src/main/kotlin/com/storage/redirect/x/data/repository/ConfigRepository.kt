package com.storage.redirect.x.data.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.storage.redirect.x.data.model.AppRedirectConfig
import com.storage.redirect.x.data.model.RedirectConfig
import com.storage.redirect.x.data.service.RootService
import com.storage.redirect.x.util.Logger
import com.storage.redirect.x.util.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val FILE_DUMP_MARKER = "@@SRX@@"

// 配置仓库：读取/写入全局配置和应用配置
class ConfigRepository {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    // 加载完整配置
    suspend fun load(userId: Int): RedirectConfig {
        Logger.debug("Load config: userId=$userId")
        val globalConfig = readGlobalConfig()
        val appConfigs = readAllAppConfigs(userId)
        val redirectApps = appConfigs.filter { it.isEnabled }.map { it.packageName }

        Logger.debug("Load config done: redirected=${redirectApps.size}, total=${appConfigs.size}")
        return RedirectConfig(
            userId = userId,
            isFileMonitorEnabled = globalConfig.isFileMonitorEnabled,
            isFuseFixerEnabled = globalConfig.isFuseFixerEnabled,
            redirectApps = redirectApps,
            appConfigs = appConfigs,
        )
    }

    suspend fun loadPackages(userId: Int, packageNames: Collection<String>): RedirectConfig {
        val targetPackages = packageNames.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSortedSet()
        Logger.debug("Load partial config: userId=$userId, appCount=${targetPackages.size}")
        val globalConfig = readGlobalConfig()
        val appConfigs = targetPackages.mapNotNull { readAppConfig(it, userId) }
        val redirectApps = appConfigs.filter { it.isEnabled }.map { it.packageName }
        return RedirectConfig(
            userId = userId,
            isFileMonitorEnabled = globalConfig.isFileMonitorEnabled,
            isFuseFixerEnabled = globalConfig.isFuseFixerEnabled,
            redirectApps = redirectApps,
            appConfigs = appConfigs,
        )
    }

    // 保存完整配置
    suspend fun save(config: RedirectConfig): Boolean = withContext(Dispatchers.IO) {
        Logger.debug("Save config: userId=${config.userId}, appCount=${config.appConfigs.size}")
        if (!writeGlobalConfig(config.isFileMonitorEnabled, config.isFuseFixerEnabled)) {
            Logger.error("Save global config failed")
            return@withContext false
        }
        if (!ensureAppsConfigDir()) {
            Logger.error("Prepare app config directory failed")
            return@withContext false
        }

        for (appConfig in config.appConfigs) {
            if (!writeAppConfigFile(appConfig, config.userId)) {
                Logger.error("Save app config failed: ${appConfig.packageName}")
                return@withContext false
            }
        }
        Logger.debug("Config saved")
        true
    }

    // 仅保存受影响的应用配置，避免小改动触发整份配置重写
    suspend fun saveAppConfigs(config: RedirectConfig, packageNames: Collection<String>): Boolean =
        withContext(Dispatchers.IO) {
            val targetPackages = packageNames.asSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSortedSet()
            Logger.debug("Save partial app configs: userId=${config.userId}, appCount=${targetPackages.size}")
            if (targetPackages.isEmpty()) {
                return@withContext true
            }
            if (!ensureAppsConfigDir()) {
                Logger.error("Prepare app config directory failed")
                return@withContext false
            }

            for (packageName in targetPackages) {
                val appConfig = config.getAppConfig(packageName)
                if (appConfig == null) {
                    Logger.debug("App config missing, skip saving: $packageName")
                    continue
                }
                if (!writeAppConfigFile(appConfig, config.userId)) {
                    Logger.error("Save app config failed: $packageName")
                    return@withContext false
                }
            }
            Logger.debug("Partial app configs saved")
            true
        }

    suspend fun setFuseFixerEnabled(isEnabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        val globalConfig = readGlobalConfig()
        writeGlobalConfig(globalConfig.isFileMonitorEnabled, isEnabled)
    }

    suspend fun readFuseFixerEnabled(): Boolean = readGlobalConfig().isFuseFixerEnabled

    // 保留其他用户；最后一个用户删除后，将文件移入备份目录。
    suspend fun deleteAppConfigs(userId: Int, packageNames: Collection<String>, backupDir: String): Boolean =
        withContext(Dispatchers.IO) {
            for (packageName in packageNames.toSortedSet()) {
                val path = Paths.appConfigFile(packageName)
                val content = RootService.readFile(path) ?: return@withContext false
                val json = try {
                    JsonParser.parseString(content).asJsonObject
                } catch (e: Exception) {
                    Logger.error("Parse app config failed: $packageName", e)
                    return@withContext false
                }
                val users = json.getAsJsonObject("users") ?: return@withContext false
                if (users.remove(userId.toString()) == null) continue
                if (users.size() == 0) {
                    if (!archiveAppConfigFiles(packageName, backupDir)) return@withContext false
                    continue
                }
                if (!writeAppConfigJson(packageName, json)) return@withContext false
            }
            true
        }

    private suspend fun archiveAppConfigFiles(packageName: String, backupDir: String): Boolean {
        val path = Paths.appConfigFile(packageName)
        if (!RootService.runCommand("mv '$path' '$backupDir/$packageName.removed.json'").isSuccess) return false
        val sharedPath = Paths.sharedAppConfigFile(packageName)
        return RootService.runCommand(
            "if [ -e '$sharedPath' ]; then mv '$sharedPath' '$backupDir/$packageName.shared.removed.json'; fi"
        ).isSuccess
    }

    // 读取全局配置，字段缺失时走保守默认
    private suspend fun readGlobalConfig(): GlobalConfig {
        val content = RootService.readFile(Paths.GLOBAL_CONFIG_FILE)
        if (content.isNullOrBlank()) return GlobalConfig()
        return try {
            val json = JsonParser.parseString(content).asJsonObject
            GlobalConfig(
                isFileMonitorEnabled = json.get("file_monitor_enabled")?.asBoolean ?: true,
                isFuseFixerEnabled = json.get("fuse_fixer_enabled")?.asBoolean ?: false,
            )
        } catch (_: Exception) {
            GlobalConfig()
        }
    }

    private suspend fun readAllAppConfigs(userId: Int): List<AppRedirectConfig> {
        // 一次 awk 遍历全部配置，空目录不触发读取错误
        val result = RootService.runCommand(
            "set -- ${Paths.APPS_CONFIG_DIR}/*.json; [ -f \"\$1\" ] || exit 0; " +
                "awk 'FNR==1{print \"$FILE_DUMP_MARKER\" FILENAME}{print}' \"\$@\""
        )
        if (!result.isSuccess || result.stdout.isBlank()) return emptyList()
        return parseAppConfigDump(result.stdout, userId)
    }

    // 解析 awk 输出：@@SRX@@<路径> 行作条目分隔，其余为 json 原文
    private fun parseAppConfigDump(stdout: String, userId: Int): List<AppRedirectConfig> {
        val appConfigs = ArrayList<AppRedirectConfig>()
        val content = StringBuilder()
        var packageName: String? = null
        fun flush() {
            val name = packageName ?: return
            parseAppConfigContent(name, content.toString(), userId)?.let { appConfigs.add(it) }
            content.clear()
        }
        for (line in stdout.lineSequence()) {
            if (line.startsWith(FILE_DUMP_MARKER)) {
                flush()
                val fileName = line.removePrefix(FILE_DUMP_MARKER).substringAfterLast('/')
                packageName = fileName.removeSuffix(".json")
            } else {
                content.appendLine(line)
            }
        }
        flush()
        return appConfigs.sortedBy { it.packageName }
    }

    // 读取单个应用配置
    private suspend fun readAppConfig(packageName: String, userId: Int): AppRedirectConfig? {
        val path = Paths.appConfigFile(packageName)
        val content = RootService.readFile(path) ?: return null
        return parseAppConfigContent(packageName, content, userId)
    }

    private fun parseAppConfigContent(packageName: String, content: String, userId: Int): AppRedirectConfig? {
        if (content.isBlank()) return null
        return try {
            val json = JsonParser.parseString(content).asJsonObject
            val users = json.getAsJsonObject("users") ?: return null
            val userObj = users.getAsJsonObject(userId.toString()) ?: return null
            AppRedirectConfig.fromUserJson(packageName, userObj)
        } catch (e: Exception) {
            Logger.error("Parse app config failed: $packageName", e)
            null
        }
    }

    // 写入全局配置
    private suspend fun writeGlobalConfig(isFileMonitorEnabled: Boolean, isFuseFixerEnabled: Boolean): Boolean {
        if (!ensureConfigDir()) {
            return false
        }
        val globalJson = JsonObject().apply {
            addProperty("file_monitor_enabled", isFileMonitorEnabled)
            addProperty("fuse_fixer_enabled", isFuseFixerEnabled)
        }
        val content = gson.toJson(globalJson) + "\n"
        val isSaved = RootService.writeFile(Paths.GLOBAL_CONFIG_FILE, content)
        if (isSaved) {
            mirrorSharedFile(Paths.SHARED_CONFIG_DIR, Paths.SHARED_GLOBAL_CONFIG_FILE, content)
        }
        return isSaved
    }

    // 确保配置目录存在
    private suspend fun ensureConfigDir(): Boolean {
        val result = RootService.runCommand("mkdir -p ${Paths.CONFIG_DIR}")
        return result.isSuccess
    }

    // 确保应用配置目录存在
    private suspend fun ensureAppsConfigDir(): Boolean {
        val result = RootService.runCommand("mkdir -p ${Paths.APPS_CONFIG_DIR}")
        return result.isSuccess
    }

    // 写入单个应用配置，保留其他用户的配置
    private suspend fun writeAppConfigFile(config: AppRedirectConfig, userId: Int): Boolean {
        val path = Paths.appConfigFile(config.packageName)

        val mergedJson = try {
            val existing = RootService.readFile(path)
            if (!existing.isNullOrBlank()) {
                JsonParser.parseString(existing).asJsonObject
            } else {
                JsonObject()
            }
        } catch (_: Exception) {
            JsonObject()
        }

        mergedJson.remove("package")
        mergedJson.remove("enabled")
        mergedJson.remove("allowed_real_paths")

        val usersObj = mergedJson.getAsJsonObject("users") ?: JsonObject()
        usersObj.add(userId.toString(), config.toUserJson())
        mergedJson.add("users", usersObj)

        return writeAppConfigJson(config.packageName, mergedJson)
    }

    private suspend fun writeAppConfigJson(packageName: String, json: JsonObject): Boolean {
        val path = Paths.appConfigFile(packageName)
        val content = gson.toJson(json) + "\n"
        val isSaved = RootService.writeFile(path, content)
        if (isSaved) {
            mirrorSharedFile(
                Paths.SHARED_APPS_CONFIG_DIR,
                Paths.sharedAppConfigFile(packageName),
                content,
            )
        }
        return isSaved
    }

    private suspend fun mirrorSharedFile(parentDir: String, path: String, content: String): Boolean {
        RootService.runCommand("mkdir -p '$parentDir' && chmod 755 '${Paths.SHARED_CONFIG_DIR}' '$parentDir'")
        val isSaved = RootService.writeFile(path, content)
        if (isSaved) {
            RootService.runCommand("chmod 644 '$path' && chcon u:object_r:shell_data_file:s0 '$path' 2>/dev/null")
        } else {
            Logger.warn("Mirror shared config failed: $path")
        }
        return isSaved
    }

    private data class GlobalConfig(
        val isFileMonitorEnabled: Boolean = true,
        val isFuseFixerEnabled: Boolean = false,
    )
}
