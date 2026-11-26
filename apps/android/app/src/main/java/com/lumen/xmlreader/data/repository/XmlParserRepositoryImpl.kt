package com.lumen.xmlreader.data.repository

import android.net.Uri
import com.lumen.xmlreader.domain.model.RecentFile
import com.lumen.xmlreader.domain.model.XmlParseResult
import com.lumen.xmlreader.domain.parser.UniversalXmlParser
import com.lumen.xmlreader.domain.repository.FileRepository
import com.lumen.xmlreader.domain.repository.XmlParserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class XmlParserRepositoryImpl(
    private val fileRepository: FileRepository
) : XmlParserRepository {

    override suspend fun parseFromUri(uri: Uri): XmlParseResult = withContext(Dispatchers.IO) {
        val (fileName, fileSizeFormatted) = fileRepository.getFileMetadata(uri)
        val inputStream = fileRepository.openInputStream(uri)

        if (inputStream == null) {
            return@withContext XmlParseResult.Error(
                message = "No se pudo acceder al archivo. Comprueba los permisos o si el archivo fue movido.",
                line = 0,
                column = 0
            )
        }

        val bytes = try {
            inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            return@withContext XmlParseResult.Error(
                message = e.localizedMessage ?: "Error al leer los datos del archivo.",
                line = 0,
                column = 0
            )
        }

        val rawXml = String(bytes, Charsets.UTF_8)
        val parseInputStream = ByteArrayInputStream(bytes)

        val result = UniversalXmlParser.parse(
            inputStream = parseInputStream,
            fileName = fileName,
            fileSizeFormatted = fileSizeFormatted,
            uriString = uri.toString(),
            rawXml = rawXml
        )

        if (result is XmlParseResult.Success) {
            val recent = RecentFile(
                uriString = uri.toString(),
                fileName = fileName,
                fileSizeFormatted = fileSizeFormatted,
                lastOpenedTimestamp = System.currentTimeMillis(),
                documentType = result.documentInfo.documentType
            )
            fileRepository.saveRecentFile(recent)
        }

        result
    }
}
