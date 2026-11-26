package com.lumen.xmlreader

import android.app.Application
import com.lumen.xmlreader.data.local.RecentFilesDataStore
import com.lumen.xmlreader.data.repository.FileRepositoryImpl
import com.lumen.xmlreader.data.repository.XmlParserRepositoryImpl
import com.lumen.xmlreader.domain.repository.FileRepository
import com.lumen.xmlreader.domain.repository.XmlParserRepository

class XmlCleanReaderApp : Application() {

    lateinit var fileRepository: FileRepository
        private set

    lateinit var xmlParserRepository: XmlParserRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val dataStore = RecentFilesDataStore(this)
        val fileRepo = FileRepositoryImpl(this, dataStore)
        fileRepository = fileRepo
        xmlParserRepository = XmlParserRepositoryImpl(fileRepo)
    }
}
