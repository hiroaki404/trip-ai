package org.example.agent

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.structure.StructureFixingParser
import org.example.prompt.clarifyRequestPrompt
import org.example.prompt.createCalendarPrompt
import org.example.prompt.planTripPrompt
import org.example.prompt.tripPlanExample
import org.example.tools.*

fun createTripPlanningStrategy(
    askTool: AskUserInUI,
    feedbackTool: FeedbackUserInUI,
    webSearchTools: WebSearchTools,
    directionsTool: DirectionsTool,
    calendarTool: CalendarTool,
) = strategy<String, TripPlan>("trip-planning") {
    var planMemory: TripPlan? = null

    val clarifyUserRequest by subgraphWithTask<String, String>(
        tools = listOf(askTool),
    ) { userInput ->
        clarifyRequestPrompt(userInput)
    }

    val planTrip by subgraphWithTask<String, String>(
        tools = webSearchTools.asTools() + directionsTool,
    ) { requestInfo ->
        planTripPrompt(requestInfo)
    }

    val nodeStructuredOutput by nodeLLMRequestStructured<TripPlan>(
        "tripPlanStructured",
        examples = listOf(tripPlanExample),
        fixingParser = StructureFixingParser(
            fixingModel = GoogleModels.Gemini2_0Flash,
            retries = 3
        )
    )

    val savePlan by node<TripPlan, TripPlan> { plan ->
        plan.also { planMemory = it }
    }

    val evaluation by subgraphWithTask<TripPlan, TripPlan>(
        tools = listOf(calendarTool, feedbackTool),
    ) { plan ->
        createCalendarPrompt(plan)
    }

    val restorePlan by node<TripPlan, TripPlan> { _ ->
        planMemory!! // FIXME: error handling when null
    }

    nodeStart then clarifyUserRequest then planTrip then nodeStructuredOutput
    edge(
        nodeStructuredOutput forwardTo savePlan
                transformed { it.getOrThrow().structure }
    )
    savePlan then evaluation then restorePlan then nodeFinish
}
