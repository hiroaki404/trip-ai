package org.example.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
@LLMDescription("A complete trip plan containing a summary and daily steps")
data class TripPlan(
    @property:LLMDescription("Overall summary of the trip plan")
    val summary: String,
    @property:LLMDescription("List of daily steps with scheduled activities")
    val step: List<Step>
) {
    @Serializable
    @LLMDescription("A single day step in the trip plan with scheduled activities")
    data class Step(
        @property:LLMDescription("Date and time for this step")
        val date: String,
        @property:LLMDescription("List of activities scheduled for this day")
        val activities: List<Activity>
    ) {
        @Serializable
        @LLMDescription("A specific activity within a step")
        data class Activity(
            @property:LLMDescription("Duration or time range for this activity")
            val duration: String,
            @property:LLMDescription("Detailed description of the activity")
            val description: String,
            @property:LLMDescription("Location where the activity takes place")
            val location: String
        )
    }
}
