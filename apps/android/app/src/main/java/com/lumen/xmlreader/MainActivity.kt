package com.lumen.xmlreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.lumen.xmlreader.ui.navigation.NavGraph
import com.lumen.xmlreader.ui.theme.XMLCleanReaderTheme
import com.lumen.xmlreader.ui.viewmodel.HistoryViewModel
import com.lumen.xmlreader.ui.viewmodel.ReaderViewModel
import com.lumen.xmlreader.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val readerViewModel: ReaderViewModel by viewModels {
        val app = application as XmlCleanReaderApp
        ViewModelFactory(app.fileRepository, app.xmlParserRepository)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        val app = application as XmlCleanReaderApp
        ViewModelFactory(app.fileRepository, app.xmlParserRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        setContent {
            XMLCleanReaderTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    readerViewModel = readerViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if (action == Intent.ACTION_VIEW && data != null) {
            readerViewModel.loadXmlUri(data)
        }
    }
}
