package com.lumen.xmlreader.domain.updater

import android.content.Context
import com.lumen.xmlreader.domain.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object AppUpdateChecker {

    private const val GITHUB_API_URL = "https://api.github.com/repos/Kaia-Alenia/lumen-xml-reader/releases/latest"

    suspend fun checkUpdate(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentAppVersion(context)
            val url = URL(GITHUB_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "LUMEN-App")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseBody = reader.use { it.readText() }
            connection.disconnect()

            val json = JSONObject(responseBody)
            val rawTagName = json.optString("tag_name", "").trim()
            val releaseTitle = json.optString("name", "Actualización disponible").ifBlank { "LUMEN Actualización" }
            val releaseNotes = json.optString("body", "").trim()
            val releasePageUrl = json.optString("html_url", "https://github.com/Kaia-Alenia/lumen-xml-reader/releases")

            val cleanRemoteVersion = rawTagName.removePrefix("v").removePrefix("V").trim()
            val isNewer = isNewerVersion(currentVersion, cleanRemoteVersion)

            var downloadUrl = ""
            val assetsArray = json.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (downloadUrl.isBlank()) {
                downloadUrl = releasePageUrl
            }

            AppUpdateInfo(
                isUpdateAvailable = isNewer,
                latestVersion = cleanRemoteVersion.ifBlank { rawTagName },
                currentVersion = currentVersion,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                releasePageUrl = releasePageUrl
            )
        } catch (_: Exception) {
            null
        }
    }

    fun isNewerVersion(current: String, remote: String): Boolean {
        if (remote.isBlank()) return false
        val cleanCurrent = current.removePrefix("v").removePrefix("V").trim()
        val cleanRemote = remote.removePrefix("v").removePrefix("V").trim()

        if (cleanCurrent == cleanRemote) return false

        val currentParts = cleanCurrent.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
        val remoteParts = cleanRemote.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }

        if (currentParts.isEmpty() || remoteParts.isEmpty()) {
            return cleanRemote != cleanCurrent
        }

        val maxLength = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLength) {
            val currPart = currentParts.getOrElse(i) { 0 }
            val remPart = remoteParts.getOrElse(i) { 0 }
            if (remPart > currPart) return true
            if (remPart < currPart) return false
        }

        return false
    }

    private fun getCurrentAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }
}
