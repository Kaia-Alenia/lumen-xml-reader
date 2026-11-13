package com.lumen.xmlreader.domain.model

data class XmlDocumentInfo(
    val uriString: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val rootNode: XmlNode,
    val documentType: XmlDocumentType,
    val totalNodes: Int,
    val encoding: String = "UTF-8",
    val rawXml: String = ""
)
