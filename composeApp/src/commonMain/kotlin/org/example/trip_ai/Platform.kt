package org.example.trip_ai

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform