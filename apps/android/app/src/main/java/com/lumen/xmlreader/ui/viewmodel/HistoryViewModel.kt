package com.lumen.xmlreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.xmlreader.domain.model.RecentFile
import com.lumen.xmlreader.domain.repository.FileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val fileRepository: FileRepository
) : ViewModel() {

    val recentFiles: StateFlow<List<RecentFile>> = fileRepository.getRecentFiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            fileRepository.clearHistory()
        }
    }
}
