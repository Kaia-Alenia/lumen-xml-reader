package com.lumen.xmlreader.ui.navigation

sealed class Screen(val route: String) {
    object Reader : Screen("reader_screen")
    object History : Screen("history_screen")
}
