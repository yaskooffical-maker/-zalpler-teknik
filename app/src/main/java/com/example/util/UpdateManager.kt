package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object UpdateManager {

    private const val PREFS_NAME = "ozalpler_update_prefs"
    private const val KEY_UPDATE_JSON_URL = "update_json_url"

    // Default JSON update endpoint pointing to user's GitHub repository
    const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/yaskooffical-maker/-zalpler-teknik/main/version.json"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    fun getUpdateJsonUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_UPDATE_JSON_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
    }

    fun setUpdateJsonUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_UPDATE_JSON_URL, url.trim()).apply()
    }

    fun resetUpdateJsonUrl(context: Context) {
        setUpdateJsonUrl(context, DEFAULT_UPDATE_URL)
    }

    /**
     * Checks if a newer version exists on the server.
     * Returns AppUpdateInfo if an update is available, or null if the app is already up to date.
     */
    suspend fun checkForUpdate(
        context: Context,
        customUrl: String? = null
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = (customUrl ?: getUpdateJsonUrl(context)).trim()
            if (url.isBlank() || !url.startsWith("http")) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz URL: Lütfen geçerli bir http/https linki belirtin."))
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Ozalpler-App/${BuildConfig.VERSION_NAME}")
                .addHeader("Cache-Control", "no-cache")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Sunucu hatası: HTTP ${response.code}"))
            }

            val jsonBody = response.body?.string() ?: ""
            if (jsonBody.isBlank()) {
                return@withContext Result.failure(Exception("Sunucudan boş yanıt döndü."))
            }

            val updateInfo = AppUpdateInfo.fromJson(jsonBody)
                ?: return@withContext Result.failure(Exception("JSON formatı çözümlenemedi. Lütfen JSON yapısını kontrol edin."))

            val currentVersionCode = BuildConfig.VERSION_CODE
            val currentVersionName = BuildConfig.VERSION_NAME

            val isNewer = isVersionHigher(
                remoteCode = updateInfo.versionCode,
                remoteName = updateInfo.versionName,
                localCode = currentVersionCode,
                localName = currentVersionName
            )

            if (isNewer) {
                Result.success(updateInfo)
            } else {
                Result.success(null) // Already up to date
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compares remote version with local version.
     */
    fun isVersionHigher(
        remoteCode: Int,
        remoteName: String,
        localCode: Int,
        localName: String
    ): Boolean {
        // First check versionCode if specified and different
        if (remoteCode > 0 && localCode > 0) {
            if (remoteCode > localCode) return true
            if (remoteCode < localCode) return false
        }

        // Compare semantic version names (e.g., "1.2.0" vs "1.1.0")
        return compareVersionNames(remoteName, localName) > 0
    }

    private fun compareVersionNames(v1: String, v2: String): Int {
        val parts1 = v1.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(parts1.size, parts2.size)
        for (i in 0 until length) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 > num2) return 1
            if (num1 < num2) return -1
        }
        return 0
    }

    /**
     * Streams APK download with progress callback.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apkUrl)
                .addHeader("User-Agent", "Ozalpler-App/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("APK indirilemedi: HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("APK gövdesi boş."))
            val contentLength = body.contentLength()

            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val apkFile = File(downloadDir, "ozalpler_latest.apk")
            if (apkFile.exists()) apkFile.delete()

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead: Long = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val progress = (totalRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        } else {
                            onProgress(-1f) // Indeterminate
                        }
                    }
                    output.flush()
                }
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks whether the app has permission to install unknown apps.
     */
    fun canInstallApk(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Launches settings intent to allow installing unknown apps.
     */
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Opens system PackageInstaller to install the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
