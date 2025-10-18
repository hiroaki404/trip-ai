package org.example.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * Platform-specific Google Calendar service interface
 */
interface GoogleCalendarService {
    suspend fun createEvent(eventName: String, dateTime: LocalDateTime): String
}

expect fun getGoogleCalendarService(): GoogleCalendarService

object CalendarTool : SimpleTool<CalendarTool.Args>() {
    @Serializable
    data class Args(
        @property:LLMDescription("The date and time when the event should be scheduled")
        val date: LocalDateTime,
        @property:LLMDescription("The name or title of the calendar event")
        val eventName: String,
    )

    override val name: String = "calendar_tool"
    override val description: String = "Create a new calendar event"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    private val calendarService = getGoogleCalendarService()

    override suspend fun doExecute(args: Args): String {
        return try {
            calendarService.createEvent(args.eventName, args.date)
        } catch (e: Exception) {
            "Failed to create calendar event: ${e.message}"
        }
    }
}
