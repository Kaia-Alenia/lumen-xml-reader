package com.lumen.xmlreader.domain.repository

import android.net.Uri
import com.lumen.xmlreader.domain.model.RecentFile
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface FileRepository {
    fun getRecentFiles(): Flow<List<RecentFile>>
    suspend fun saveRecentFile(file: RecentFile)
    suspend fun clearHistory()
    fun getFileMetadata(uri: Uri): Pair<String, String>
    fun openInputStream(uri: Uri): InputStream?
}
