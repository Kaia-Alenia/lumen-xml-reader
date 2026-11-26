package com.lumen.xmlreader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lumen.xmlreader.ui.screens.HistoryScreen
import com.lumen.xmlreader.ui.screens.ReaderScreen
import com.lumen.xmlreader.ui.viewmodel.HistoryViewModel
import com.lumen.xmlreader.ui.viewmodel.ReaderViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    readerViewModel: ReaderViewModel,
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Reader.route,
        modifier = modifier
    ) {
        composable(route = Screen.Reader.route) {
            ReaderScreen(
                viewModel = readerViewModel,
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(route = Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFileSelected = { uri ->
                    readerViewModel.loadXmlUri(uri)
                    navController.popBackStack()
                }
            )
        }
    }
}
