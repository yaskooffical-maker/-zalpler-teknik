package com.example.data.model

import org.json.JSONObject

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String = "",
    val fileSizeMb: Double = 0.0,
    val forceUpdate: Boolean = false,
    val minSupportedVersionCode: Int = 0
) {
    companion object {
        fun fromJson(jsonStr: String): AppUpdateInfo? {
            return try {
                val json = JSONObject(jsonStr)
                AppUpdateInfo(
                    versionCode = json.optInt("versionCode", json.optInt("version_code", 0)),
                    versionName = json.optString("versionName", json.optString("version_name", "1.0")),
                    apkUrl = json.optString("apkUrl", json.optString("apk_url", json.optString("downloadUrl", ""))),
                    releaseNotes = json.optString("releaseNotes", json.optString("release_notes", json.optString("changelog", ""))),
                    fileSizeMb = json.optDouble("fileSizeMb", json.optDouble("file_size_mb", 0.0)),
                    forceUpdate = json.optBoolean("forceUpdate", json.optBoolean("force_update", false)),
                    minSupportedVersionCode = json.optInt("minSupportedVersionCode", 0)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
