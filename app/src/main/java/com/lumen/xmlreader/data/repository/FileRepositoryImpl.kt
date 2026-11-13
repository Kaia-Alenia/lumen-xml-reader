package com.lumen.xmlreader.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.lumen.xmlreader.data.local.RecentFilesDataStore
import com.lumen.xmlreader.domain.model.RecentFile
import com.lumen.xmlreader.domain.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream
import java.util.Locale

class FileRepositoryImpl(
    private val context: Context,
    private val dataStore: RecentFilesDataStore
) : FileRepository {

    override fun getRecentFiles(): Flow<List<RecentFile>> {
        return dataStore.recentFilesFlow
    }

    override suspend fun saveRecentFile(file: RecentFile) {
        dataStore.saveRecentFile(file)
    }

    override suspend fun clearHistory() {
        dataStore.clearHistory()
    }

    override fun getFileMetadata(uri: Uri): Pair<String, String> {
        var name = uri.lastPathSegment ?: "documento.xml"
        var sizeInBytes: Long = -1

        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: name
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex != -1) {
                            sizeInBytes = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        } else if (uri.scheme == "file") {
            try {
                val file = uri.path?.let { File(it) }
                if (file != null && file.exists()) {
                    name = file.name
                    sizeInBytes = file.length()
                }
            } catch (_: Exception) {
            }
        }

        val formattedSize = formatFileSize(sizeInBytes)
        return Pair(name, formattedSize)
    }

    override fun openInputStream(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Desconocido"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.2f MB", mb)
        } else {
            String.format(Locale.US, "%.1f KB", kb)
        }
    }
}
