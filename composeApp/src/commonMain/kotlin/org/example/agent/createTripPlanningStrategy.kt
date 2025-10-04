package org.example.agent

import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.tool.AskUser
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import org.example.prompt.clarifyRequestPrompt
import org.example.prompt.planTripPrompt
import org.example.prompt.systemClarifyRequestPrompt
import org.example.prompt.systemPlanTripPrompt

fun createTripPlanningStrategy() = strategy<String, String>("trip-planning") {

    val nodeBeforeClarifyUserRequest by node<String, String> { userInput ->
        llm.writeSession {
            updatePrompt {
                system(systemClarifyRequestPrompt)
            }
        }
        userInput
    }

    val nodeClarifyUserRequest by subgraphWithTask<String, String>(
        tools = listOf(AskUser),
        llmModel = OpenAIModels.Reasoning.O4Mini
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
        tools = emptyList(),
        llmModel = OpenAIModels.Reasoning.O4Mini
    ) { requestInfo ->
        planTripPrompt(requestInfo)
    }

    nodeStart then nodeBeforeClarifyUserRequest then nodeClarifyUserRequest then nodeBeforePlanTrip then nodePlanTrip then nodeFinish
}
