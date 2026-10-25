package com.storage.redirect.x.data.repository

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.storage.redirect.x.BuildConfig
import com.storage.redirect.x.data.model.AppUpdateInfo
import com.storage.redirect.x.data.model.UpdateChannel
import com.storage.redirect.x.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateResult {
    data class Available(val info: AppUpdateInfo) : UpdateResult
    data object UpToDate : UpdateResult
    data object Failed : UpdateResult
}

class UpdateRepository {
    companion object {
        // GitHub Releases API 地址
        private const val API_URL =
            "https://api.github.com/repos/Kindness-Kismet/Storage-redirection-X-Public/releases"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 10_000
        private const val REQUEST_ACCEPT = "application/vnd.github+json"
        private const val REQUEST_USER_AGENT = "StorageRedirectX-Android/${BuildConfig.VERSION_NAME}"

        private fun requestReleaseJson(url: String): String {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", REQUEST_ACCEPT)
                setRequestProperty("User-Agent", REQUEST_USER_AGENT)
            }

            return try {
                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    throw IllegalStateException("GitHub API error: code=$responseCode")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun checkForUpdate(channel: UpdateChannel = UpdateChannel.STABLE): UpdateResult =
        withContext(Dispatchers.IO) {
            try {
                val installed = requireNotNull(parseVersion(BuildConfig.VERSION_NAME))
                // 正式版单独查询，避免被大量测试版挤出列表首页。
                val stable = requireNotNull(
                    parseRelease(JsonParser.parseString(requestReleaseJson("$API_URL/latest")))
                )
                val candidate = when (channel) {
                    UpdateChannel.STABLE -> stable
                    UpdateChannel.TEST -> {
                        val releases = JsonParser.parseString(
                            requestReleaseJson("$API_URL?per_page=100")
                        ).asJsonArray
                        (releases.mapNotNull(::parseRelease) + stable).maxBy { it.version }
                    }
                }
                if (candidate.version > installed) UpdateResult.Available(candidate.info)
                else UpdateResult.UpToDate
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Logger.error("Check for updates failed", error)
                UpdateResult.Failed
            }
        }
}

private data class RemoteRelease(val version: ParsedVersion, val info: AppUpdateInfo)

private fun parseRelease(value: JsonElement): RemoteRelease? {
    val json = value.asJsonObject
    if (json.get("draft").asBoolean) return null
    val tag = json.get("tag_name").asString.removePrefix("v")
    val version = parseVersion(tag) ?: return null
    return RemoteRelease(
        version,
        AppUpdateInfo(
            latestVersion = tag,
            releaseUrl = json.get("html_url").asString,
            releaseNotes = json.get("body")?.takeUnless { it.isJsonNull }?.asString.orEmpty(),
        ),
    )
}

private data class ParsedVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val betaNumber: Int?,
) : Comparable<ParsedVersion> {
    override fun compareTo(other: ParsedVersion): Int {
        val core = compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })
        if (core != 0) return core
        // 同版本正式版高于测试版，测试序号按数值比较。
        return when {
            betaNumber == null && other.betaNumber == null -> 0
            betaNumber == null -> 1
            other.betaNumber == null -> -1
            else -> betaNumber.compareTo(other.betaNumber)
        }
    }
}

private fun parseVersion(raw: String): ParsedVersion? {
    val match = VERSION_PATTERN.matchEntire(raw.removePrefix("v")) ?: return null
    return ParsedVersion(
        major = match.groupValues[1].toInt(),
        minor = match.groupValues[2].toInt(),
        patch = match.groupValues[3].toInt(),
        betaNumber = match.groupValues[4].takeIf { it.isNotEmpty() }?.toInt(),
    )
}

private val VERSION_PATTERN = Regex("""^(\d+)\.(\d+)\.(\d+)(?:-beta(\d+))?$""")
