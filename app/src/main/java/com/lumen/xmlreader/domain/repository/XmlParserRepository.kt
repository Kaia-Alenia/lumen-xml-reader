package com.lumen.xmlreader.domain.repository

import android.net.Uri
import com.lumen.xmlreader.domain.model.XmlParseResult

interface XmlParserRepository {
    suspend fun parseFromUri(uri: Uri): XmlParseResult
}
