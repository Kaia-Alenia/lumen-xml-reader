package com.lumen.xmlreader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lumen.xmlreader.domain.model.RecentFile
import com.lumen.xmlreader.domain.model.XmlDocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xml_clean_reader_prefs")

class RecentFilesDataStore(private val context: Context) {

    private val recentFilesKey = stringPreferencesKey("recent_files_history_v1")

    val recentFilesFlow: Flow<List<RecentFile>> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[recentFilesKey] ?: "[]"
        deserializeRecentFiles(rawJson)
    }

    suspend fun saveRecentFile(file: RecentFile) {
        context.dataStore.edit { preferences ->
            val rawJson = preferences[recentFilesKey] ?: "[]"
            val currentList = deserializeRecentFiles(rawJson).toMutableList()

            currentList.removeAll { it.uriString == file.uriString }
            currentList.add(0, file)

            val trimmedList = if (currentList.size > 30) currentList.take(30) else currentList
            preferences[recentFilesKey] = serializeRecentFiles(trimmedList)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(recentFilesKey)
        }
    }

    private fun serializeRecentFiles(list: List<RecentFile>): String {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("uriString", item.uriString)
                put("fileName", item.fileName)
                put("fileSizeFormatted", item.fileSizeFormatted)
                put("lastOpenedTimestamp", item.lastOpenedTimestamp)
                put("documentType", item.documentType.name)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun deserializeRecentFiles(rawJson: String): List<RecentFile> {
        val list = mutableListOf<RecentFile>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeName = obj.optString("documentType", XmlDocumentType.GENERIC_XML.name)
                val documentType = try {
                    XmlDocumentType.valueOf(typeName)
                } catch (_: Exception) {
                    XmlDocumentType.GENERIC_XML
                }
                list.add(
                    RecentFile(
                        uriString = obj.getString("uriString"),
                        fileName = obj.getString("fileName"),
                        fileSizeFormatted = obj.getString("fileSizeFormatted"),
                        lastOpenedTimestamp = obj.getLong("lastOpenedTimestamp"),
                        documentType = documentType
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }
}
