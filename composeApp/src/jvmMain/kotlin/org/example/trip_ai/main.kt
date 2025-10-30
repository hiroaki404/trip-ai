package org.example.trip_ai

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = jvmApplication
val jvmApplication = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "trip_ai",
//        state = WindowState(width = 500.dp, height = 600.dp)
        state = WindowState(width = 700.dp, height = 600.dp)
    ) {
        App()
    }
}

//val jvmTool = runBlocking {
//    val userInput = "2泊3日の札幌旅行の計画を立ててください"
//    println("---\n")
//    println("User: $userInput\n")
//    println("---")
//
//    val agent = createTripAgent()
//    val response = agent.run(userInput)
//    println("---")
//    println("Agent: $response\n")
//    println("---")
//}
