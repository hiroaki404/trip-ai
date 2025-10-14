package org.example.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.message.Message
import org.example.tools.AskUserInUI
import org.example.tools.DirectionsTool
import org.example.tools.WebSearchTools
import org.example.tools.createMapMCP
import org.example.trip_ai.ChatMessage

suspend fun createTripAgent(askUser: AskUserInUI, onMessageUpdate: (ChatMessage) -> Unit): AIAgent<String, TripPlan> {
    // not work in Android
    val apiKey = System.getenv("OPENAI_API_KEY")
    val geminiApiKey = System.getenv("GEMINI_API_KEY")
    val googleApiKey = System.getenv("CUSTOM_SEARCH_API_KEY")
    val searchEngineId = System.getenv("SEARCH_ENGINE_ID")
    val mapboxAccessToken = System.getenv("MAPBOX_ACCESS_TOKEN")
    val npxCommandPath = System.getenv("NPX_COMMAND_PATH")

    val executor = simpleOpenAIExecutor(apiKey)
    val geminiExecutor = simpleGoogleAIExecutor(geminiApiKey)
    val webSearchTools = WebSearchTools(googleApiKey, searchEngineId)
    // not work in Android
    val mapTools = McpToolRegistryProvider.fromTransport(createMapMCP(mapboxAccessToken, npxCommandPath))
    val directionsTool = DirectionsTool(mapboxAccessToken)

    val toolRegistry = ToolRegistry {
        tool(askUser)
        tools(webSearchTools.asTools())
        tool(directionsTool)
    }

    return AIAgent<String, TripPlan>(
        promptExecutor = executor,
        llmModel = OpenAIModels.Chat.GPT5Mini,
        systemPrompt = """
        あなたは旅行プランナーです。ユーザーの指示に従って、旅行計画を立ててください。
        ただしユーザーの指示が少ないときは__ask_user__ツールを使って、1度はユーザーに情報提供を促してください。
        最小限の情報があればそれでよいです。観光スポットなども含めて、あなたが考えて提案してください。
        ユーザーに行きたい場所を尋ねず、あなたが提案してください。
        最小限の情報とは、旅行先、予算のことです。
        あまり細かく聞きすぎず、ある程度分かったところで計画を立ててください
        """.trimIndent(),
        toolRegistry = toolRegistry,
        strategy = createTripPlanningStrategy(askUser, webSearchTools, directionsTool),
    ) {
        install(OpenTelemetry) {
            setVerbose(true)
            addLangfuseExporter()
        }
        handleEvents {
            onToolCallStarting {
                when (val tool = it.tool) {
                    is AskUserInUI -> {
                        onMessageUpdate(
                            ChatMessage.ToolCall(
                                tool.name,
                                (it.toolArgs as AskUserInUI.Args).message
                            )
                        )
                    }

                    else -> {
                        onMessageUpdate(
                            ChatMessage.ToolCall(
                                tool.name,
                                "${tool.name} is Called"
                            )
                        )
                    }
                }
            }
            onLLMCallCompleted {
                when (val lastMessage = it.prompt.messages.last()) {
                    is Message.Assistant -> onMessageUpdate(ChatMessage.Assistant(lastMessage.content))
                    else -> { /* no-op */
                    }
                }
            }
        }
    }
}
