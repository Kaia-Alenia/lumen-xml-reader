package com.lumen.xmlreader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lumen.xmlreader.domain.repository.FileRepository
import com.lumen.xmlreader.domain.repository.XmlParserRepository

class ViewModelFactory(
    private val fileRepository: FileRepository,
    private val xmlParserRepository: XmlParserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ReaderViewModel::class.java) -> {
                ReaderViewModel(xmlParserRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(fileRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}
