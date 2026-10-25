package com.storage.redirect.x.data.model

data class AppUpdateInfo(
    val latestVersion: String,
    val releaseUrl: String,
    val releaseNotes: String,
)

enum class UpdateChannel { STABLE, TEST }
