package org.example.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable
import org.example.trip_ai.Coordinate

@Serializable
@LLMDescription("A complete trip plan containing a summary and daily steps")
data class TripPlan(
    @property:LLMDescription("Overall summary of the trip plan")
    val summary: String,
    @property:LLMDescription("List of daily steps with scheduled entries")
    val step: List<Step>
) {
    @Serializable
    @LLMDescription("A single day step in the trip plan with scheduled entries")
    data class Step(
        @property:LLMDescription("Date and time for this step")
        val date: String,
        @property:LLMDescription("List of schedule entries (activities and transportation) for this day")
        val scheduleEntries: List<ScheduleEntry>,
    ) {
        @Serializable
        sealed interface ScheduleEntry {
            val duration: String
            val description: String

            @Serializable
            @LLMDescription("A specific activity within a step")
            data class Activity(
                @property:LLMDescription("Duration or time range for this activity")
                override val duration: String,
                @property:LLMDescription("Detailed description of the activity")
                override val description: String,
                @property:LLMDescription("Location where the activity takes place")
                val location: String,
                @property:LLMDescription("Longitude of the location")
                val longitude: Double,
                @property:LLMDescription("Latitude of the location")
                val latitude: Double,
            ) : ScheduleEntry

            @Serializable
            @LLMDescription("Transportation between locations")
            data class Transportation(
                @property:LLMDescription("Type of transportation (e.g., train, bus, taxi, walking)")
                val type: String,
                @property:LLMDescription("Departure location")
                val from: String,
                @property:LLMDescription("Destination location")
                val to: String,
                @property:LLMDescription("Tripe route path")
                val lineId: String,
                @property:LLMDescription("Duration or time range for this transportation")
                override val duration: String,
                @property:LLMDescription("Detailed description of the transportation")
                override val description: String
            ) : ScheduleEntry
        }
    }
}

@Serializable
data class Line(
    val id: String,
    val points: List<Point>
) {
    @Serializable
    data class Point(
        val longitude: Double,
        val latitude: Double
    )

    fun toCoordinate() = points.map { Coordinate(it.longitude, it.latitude) }

}
