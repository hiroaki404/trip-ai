package org.example.tools

import kotlinx.datetime.LocalDateTime

actual fun getGoogleCalendarService(): GoogleCalendarService = AndroidGoogleCalendarService()

class AndroidGoogleCalendarService : GoogleCalendarService {
    override suspend fun createEvent(
        eventName: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): String {
        return "Google Calendar integration is not yet implemented for Android platform"
    }
}
