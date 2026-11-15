package com.vibe.feature.devices

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateCheckResult {
    data object Idle : UpdateCheckResult
    data object Checking : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseUrl: String,
        val apkDownloadUrl: String?,
        val releaseNotes: String
    ) : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

object AppUpdater {
    private const val PLAY_STORE_PACKAGE = "com.android.vending"
    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/M-Tre-Consulting/vibe-player/releases/latest"

    fun isInstalledFromPlayStore(context: Context): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            installer == PLAY_STORE_PACKAGE
        } catch (_: Exception) {
            false
        }
    }

    fun openPlayStore(context: Context) {
        val pkg = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    suspend fun checkGitHubUpdate(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "VibePlayer-Android")
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                return@withContext UpdateCheckResult.Error("HTTP $code")
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tagName = json.optString("tag_name").trim()
            val htmlUrl = json.optString("html_url").trim()
            val releaseNotes = json.optString("body").trim()

            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }

            if (tagName.isBlank()) {
                return@withContext UpdateCheckResult.UpToDate
            }

            if (isNewerVersion(latest = tagName, current = currentVersion)) {
                UpdateCheckResult.UpdateAvailable(
                    latestVersion = tagName,
                    releaseUrl = htmlUrl,
                    apkDownloadUrl = apkUrl,
                    releaseNotes = releaseNotes
                )
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun downloadAndInstallApk(context: Context, apkUrl: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val apkFile = File(updatesDir, "vibe-update.apk")

                val conn = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "VibePlayer-Android")
                }

                if (conn.responseCode !in 200..299) {
                    return@withContext Result.failure(Exception("HTTP ${conn.responseCode}"))
                }

                conn.inputStream.use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )

                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(installIntent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    internal fun isNewerVersion(latest: String, current: String): Boolean {
        val cleanLatest = latest.removePrefix("v").substringBefore("-")
        val cleanCurrent = current.removePrefix("v").substringBefore("-")
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
