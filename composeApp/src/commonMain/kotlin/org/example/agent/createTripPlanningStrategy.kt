package org.example.agent

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.structure.StructureFixingParser
import org.example.prompt.*
import org.example.tools.AskUserInUI
import org.example.tools.WebSearchTools

fun createTripPlanningStrategy(
    askTool: AskUserInUI,
    webSearchTools: WebSearchTools,
    map: ToolRegistry, // must be MapMcp, FIXME
) = strategy<String, TripPlan>("trip-planning") {

    val nodeBeforeClarifyUserRequest by node<String, String> { userInput ->
        llm.writeSession {
            updatePrompt {
                system(systemClarifyRequestPrompt)
            }
        }
        userInput
    }

    val nodeClarifyUserRequest by subgraphWithTask<String, String>(
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

    val nodePlanTrip by subgraphWithTask<String, String>(
        tools = webSearchTools.asTools() + map.tools,
    ) { requestInfo ->
        planTripPrompt(requestInfo)
    }

    val nodeStructuredOutput by nodeLLMRequestStructured<TripPlan>(
        "tripPlanStructured",
        examples = listOf(tripPlanExample),
        fixingParser = StructureFixingParser(
            fixingModel = OpenAIModels.Chat.GPT4o,
            retries = 2
        )
    )

    nodeStart then nodeBeforeClarifyUserRequest then nodeClarifyUserRequest then nodeBeforePlanTrip then nodePlanTrip then nodeStructuredOutput
    edge(
        nodeStructuredOutput forwardTo nodeFinish
                transformed { it.getOrThrow().structure }
    )
}
