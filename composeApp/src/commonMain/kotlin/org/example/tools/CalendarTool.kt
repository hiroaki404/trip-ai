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
    suspend fun createEvent(eventName: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): String
}

expect fun getGoogleCalendarService(): GoogleCalendarService

object CalendarTool : SimpleTool<CalendarTool.Args>() {
    @Serializable
    data class Args(
        @property:LLMDescription("The name or title of the calendar event")
        val eventName: String,
        @property:LLMDescription("The start date and time of the event in ISO 8601 format (YYYY-MM-DDTHH:MM). Example: 2025-10-15T09:00. Use 'T' to separate date and time, NOT a space.")
        val startDate: LocalDateTime,
        @property:LLMDescription("The end date and time of the event in ISO 8601 format (YYYY-MM-DDTHH:MM). Example: 2025-10-15T18:00. Use 'T' to separate date and time, NOT a space.")
        val endDate: LocalDateTime,
    )

    override val name: String = "calendar_tool"
    override val description: String = "Create a new calendar event with start and end times"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    private val calendarService = getGoogleCalendarService()

    override suspend fun doExecute(args: Args): String {
        return try {
            calendarService.createEvent(args.eventName, args.startDate, args.endDate)
        } catch (e: Exception) {
            "Failed to create calendar event: ${e.message}"
        }
    }
}
