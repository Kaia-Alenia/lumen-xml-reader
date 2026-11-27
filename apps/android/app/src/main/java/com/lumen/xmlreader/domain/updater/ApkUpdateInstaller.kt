package com.lumen.xmlreader.domain.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateInstallResult {
    data object InstallerLaunched : UpdateInstallResult()
    data object PermissionRequired : UpdateInstallResult()
    data class Error(val message: String) : UpdateInstallResult()
}

object ApkUpdateInstaller {

    private const val MAX_REDIRECTS = 5

    private fun isAllowedUrl(url: String): Boolean {
        return url.startsWith("https://github.com/Kaia-Alenia/lumen-xml-reader/") ||
            url.startsWith("https://objects.githubusercontent.com/") ||
            url.startsWith("https://release-assets.githubusercontent.com/") ||
            url.endsWith(".apk", ignoreCase = true)
    }

    suspend fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): UpdateInstallResult = withContext(Dispatchers.IO) {
        if (!isAllowedUrl(downloadUrl)) {
            return@withContext UpdateInstallResult.Error("URL de descarga no válida.")
        }

        val apkFile = File(context.cacheDir, "lumen-update.apk")
        var connection: HttpURLConnection? = null

        try {
            var currentUrl = downloadUrl
            var redirects = 0

            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = false
                    connectTimeout = 15_000
                    readTimeout = 60_000
                    setRequestProperty("User-Agent", "LUMEN-App")
                }

                val code = connection.responseCode
                if (code in listOf(
                        HttpURLConnection.HTTP_MOVED_PERM,
                        HttpURLConnection.HTTP_MOVED_TEMP,
                        HttpURLConnection.HTTP_SEE_OTHER,
                        307,
                        308
                    )
                ) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location.isNullOrBlank() || redirects >= MAX_REDIRECTS) {
                        return@withContext UpdateInstallResult.Error("Demasiadas redirecciones al descargar.")
                    }
                    currentUrl = location
                    redirects += 1
                    continue
                }

                if (code != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    return@withContext UpdateInstallResult.Error("Error al descargar (HTTP $code).")
                }

                break
            }

            val totalBytes = connection!!.contentLengthLong
            if (apkFile.exists()) {
                apkFile.delete()
            }

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0) {
                            onProgress((downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                        } else {
                            onProgress(-1f)
                        }
                    }
                    output.flush()
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            connection?.disconnect()
            return@withContext UpdateInstallResult.Error(
                e.localizedMessage ?: "No se pudo descargar la actualización."
            )
        }

        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !context.packageManager.canRequestPackageInstalls()
            ) {
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                UpdateInstallResult.PermissionRequired
            } else {
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(installIntent)
                UpdateInstallResult.InstallerLaunched
            }
        }
    }
}
