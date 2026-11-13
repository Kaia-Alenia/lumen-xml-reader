package com.lumen.xmlreader.ui.viewmodel

import com.lumen.xmlreader.domain.model.InvoiceData
import com.lumen.xmlreader.domain.model.XmlDocumentInfo
import com.lumen.xmlreader.domain.model.XmlNode

enum class ReaderTab(val title: String) {
    FACTURA("Factura"),
    ARBOL("Árbol"),
    CRUDO("Crudo")
}

sealed interface ReaderUiState {
    data object Idle : ReaderUiState
    data object Loading : ReaderUiState
    data class Error(
        val message: String,
        val line: Int,
        val column: Int,
        val snippet: String? = null
    ) : ReaderUiState
    data class Success(
        val documentInfo: XmlDocumentInfo,
        val invoiceData: InvoiceData,
        val selectedTab: ReaderTab = ReaderTab.FACTURA,
        val expandedNodeIds: Set<String> = emptySet(),
        val searchQuery: String = "",
        val selectedDetailNode: XmlNode? = null
    ) : ReaderUiState
}
