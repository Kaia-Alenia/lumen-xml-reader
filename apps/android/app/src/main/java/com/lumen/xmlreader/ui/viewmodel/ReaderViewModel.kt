package com.lumen.xmlreader.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.xmlreader.domain.model.AppUpdateInfo
import com.lumen.xmlreader.domain.model.XmlDocumentType
import com.lumen.xmlreader.domain.model.XmlNode
import com.lumen.xmlreader.domain.model.XmlParseResult
import com.lumen.xmlreader.domain.parser.InvoiceDataExtractor
import com.lumen.xmlreader.domain.pdf.PdfInvoiceGenerator
import com.lumen.xmlreader.domain.repository.XmlParserRepository
import com.lumen.xmlreader.domain.updater.AppUpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val xmlParserRepository: XmlParserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Idle)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            val updateInfo = AppUpdateChecker.checkUpdate(context)
            if (updateInfo != null && updateInfo.isUpdateAvailable) {
                _availableUpdate.value = updateInfo
                _showUpdateDialog.value = true
            }
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun openUpdateDialog() {
        if (_availableUpdate.value != null) {
            _showUpdateDialog.value = true
        }
    }

    fun loadXmlUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            when (val result = xmlParserRepository.parseFromUri(uri)) {
                is XmlParseResult.Success -> {
                    val initialExpanded = hashSetOf<String>()
                    initialExpanded.add(result.documentInfo.rootNode.id)
                    result.documentInfo.rootNode.hijos.take(5).forEach {
                        initialExpanded.add(it.id)
                    }

                    val invoiceData = InvoiceDataExtractor.extract(result.documentInfo.rootNode)
                    val defaultTab = if (result.documentInfo.documentType == XmlDocumentType.CFDI_SAT || invoiceData.isCfdi) {
                        ReaderTab.FACTURA
                    } else {
                        ReaderTab.ARBOL
                    }

                    _uiState.value = ReaderUiState.Success(
                        documentInfo = result.documentInfo,
                        invoiceData = invoiceData,
                        selectedTab = defaultTab,
                        expandedNodeIds = initialExpanded
                    )
                }
                is XmlParseResult.Error -> {
                    _uiState.value = ReaderUiState.Error(
                        message = result.message,
                        line = result.line,
                        column = result.column,
                        snippet = result.rawSnippet
                    )
                }
            }
        }
    }

    fun selectTab(tab: ReaderTab) {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                current.copy(selectedTab = tab)
            } else current
        }
    }

    fun exportPdf(context: Context) {
        val state = _uiState.value
        if (state is ReaderUiState.Success) {
            PdfInvoiceGenerator.generateAndShare(context, state.invoiceData)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                current.copy(searchQuery = query)
            } else current
        }
    }

    fun toggleNodeExpansion(nodeId: String) {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                val updated = current.expandedNodeIds.toMutableSet()
                if (updated.contains(nodeId)) {
                    updated.remove(nodeId)
                } else {
                    updated.add(nodeId)
                }
                current.copy(expandedNodeIds = updated)
            } else current
        }
    }

    fun expandAll() {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                val allIds = hashSetOf<String>()
                collectAllNodeIds(current.documentInfo.rootNode, allIds)
                current.copy(expandedNodeIds = allIds)
            } else current
        }
    }

    fun collapseAll() {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                current.copy(expandedNodeIds = emptySet())
            } else current
        }
    }

    fun showNodeDetails(node: XmlNode) {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                current.copy(selectedDetailNode = node)
            } else current
        }
    }

    fun dismissNodeDetails() {
        _uiState.update { current ->
            if (current is ReaderUiState.Success) {
                current.copy(selectedDetailNode = null)
            } else current
        }
    }

    private fun collectAllNodeIds(node: XmlNode, destination: MutableSet<String>) {
        destination.add(node.id)
        for (child in node.hijos) {
            collectAllNodeIds(child, destination)
        }
    }
}
