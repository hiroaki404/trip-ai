package org.example.trip_ai

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "trip_ai",
    ) {
        App()
    }
}