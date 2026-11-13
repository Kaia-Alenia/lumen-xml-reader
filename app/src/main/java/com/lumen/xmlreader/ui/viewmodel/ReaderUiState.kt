package com.lumen.xmlreader.ui.viewmodel

import com.lumen.xmlreader.domain.model.XmlDocumentInfo
import com.lumen.xmlreader.domain.model.XmlNode

sealed interface ReaderUiState {
    object Idle : ReaderUiState
    object Loading : ReaderUiState
    data class Success(
        val documentInfo: XmlDocumentInfo,
        val searchQuery: String = "",
        val expandedNodeIds: Set<String> = emptySet(),
        val selectedDetailNode: XmlNode? = null
    ) : ReaderUiState
    data class Error(
        val message: String,
        val line: Int,
        val column: Int,
        val snippet: String? = null
    ) : ReaderUiState
}
