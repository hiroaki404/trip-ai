package org.example.agent

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.structure.StructureFixingParser
import org.example.prompt.*
import org.example.tools.AskUserInUI
import org.example.tools.CalendarTool
import org.example.tools.DirectionsTool
import org.example.tools.WebSearchTools

fun createTripPlanningStrategy(
    askTool: AskUserInUI,
    webSearchTools: WebSearchTools,
    directionsTool: DirectionsTool,
    calendarTool: CalendarTool,
) = strategy<String, TripPlan>("trip-planning") {
    var planMemory: String = ""

    val nodeBeforeClarifyUserRequest by node<String, String> { userInput ->
        llm.writeSession {
            updatePrompt {
                system(systemClarifyRequestPrompt)
            }
        }
        userInput
    }

    val clarifyUserRequest by subgraphWithTask<String, String>(
        tools = listOf(askTool),
    ) { userInput ->
        clarifyRequestPrompt(userInput)
    }

    val nodeBeforePlanTrip by node<String, String> { requestInfo ->
        llm.writeSession {
            updatePrompt {
                system(systemPlanTripPrompt)
            }
        }
        requestInfo
    }

    val planTrip by subgraphWithTask<String, String>(
        tools = webSearchTools.asTools() + directionsTool,
    ) { requestInfo ->
        planTripPrompt(requestInfo)
    }

    val savePlan by node<String, String> { plan ->
        plan.also { planMemory = it }
    }

    val createCalendar by subgraphWithTask<String, String>(
        tools = listOf(calendarTool),
    ) { plan ->
        createCalendarPrompt(plan)
    }

    val restorePlan by node<String, String> { _ ->
        planMemory
    }

    val nodeStructuredOutput by nodeLLMRequestStructured<TripPlan>(
        "tripPlanStructured",
        examples = listOf(tripPlanExample),
        fixingParser = StructureFixingParser(
            fixingModel = GoogleModels.Gemini2_0Flash,
            retries = 2
        )
    )

    nodeStart then nodeBeforeClarifyUserRequest then clarifyUserRequest then nodeBeforePlanTrip then planTrip then
            savePlan then createCalendar then restorePlan then nodeStructuredOutput
    edge(
        nodeStructuredOutput forwardTo nodeFinish
                transformed { it.getOrThrow().structure }
    )
}
