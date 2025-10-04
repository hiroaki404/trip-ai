package org.example.trip_ai

import kotlinx.coroutines.runBlocking
import org.example.agent.createTripAgent

fun main() = runBlocking {
    val userInput = "2泊3日の札幌旅行の計画を立ててください"
    println("---\n")
    println("User: $userInput\n")
    println("---")

    val agent = createTripAgent()
    val response = agent.run(userInput)
    println("---")
    println("Agent: $response\n")
    println("---")
}

//    application {
//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "trip_ai",
//    ) {
//        App()
//    }
//}
