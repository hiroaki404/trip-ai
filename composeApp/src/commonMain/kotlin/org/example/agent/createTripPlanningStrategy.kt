package org.example.agent

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.structure.StructureFixingParser
import org.example.prompt.clarifyRequestPrompt
import org.example.prompt.createCalendarPrompt
import org.example.prompt.planTripPrompt
import org.example.prompt.tripPlanExample
import org.example.tools.*

fun createTripPlanningStrategy(
    askTool: AskUserInUI,
    feedbackTool: FeedbackUserInUI,
    webSearchTool: WebSearchTool,
    webScrapeTool: WebScrapeTool,
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
        tools = listOf(webSearchTool, webScrapeTool, directionsTool),
    ) { requestInfo ->
        planTripPrompt(requestInfo)
    }

    val nodeStructuredOutput by nodeLLMRequestStructured<TripPlan>(
        "tripPlanStructured",
        examples = listOf(tripPlanExample),
        fixingParser = StructureFixingParser(
            fixingModel = OpenRouterModels.Gemini2_5Flash,
            retries = 3
        )
    )

    val savePlan by node<TripPlan, TripPlan> { plan ->
        plan.also { planMemory = it }
    }

    val evaluation by subgraphWithTask<TripPlan, String?>(
        tools = listOf(calendarTool, feedbackTool),
    ) { plan ->
        createCalendarPrompt(plan)
    }

    val restorePlan by node<String?, TripPlan> { _ ->
        planMemory!! // FIXME: error handling when null
    }

    nodeStart then clarifyUserRequest then planTrip then nodeStructuredOutput
    edge(
        nodeStructuredOutput forwardTo savePlan
                transformed { it.getOrThrow().structure }
    )
    savePlan then evaluation
    edge(
        evaluation forwardTo restorePlan
                onCondition { it == null || it.contains("null") })
    edge(
        evaluation forwardTo planTrip
                onCondition { it != null }
                transformed { it!! }
    )
    restorePlan then nodeFinish
}
