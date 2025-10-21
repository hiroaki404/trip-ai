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
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.message.Message
import org.example.tools.*
import org.example.trip_ai.ChatMessage

suspend fun createTripAgent(
    askUser: AskUserInUI,
    feedbackTool: FeedbackUserInUI,
    onMessageUpdate: (ChatMessage) -> Unit
): AIAgent<String, TripPlan> {
    // not work in Android
    val openAIApiKey = System.getenv("OPENAI_API_KEY")
    val geminiApiKey = System.getenv("GEMINI_API_KEY")
    val openRouterApiKey = System.getenv("OPEN_ROUTER_API_KEY")

    val googleApiKey = System.getenv("CUSTOM_SEARCH_API_KEY")
    val searchEngineId = System.getenv("SEARCH_ENGINE_ID")
    val mapboxAccessToken = System.getenv("MAPBOX_ACCESS_TOKEN")
    val npxCommandPath = System.getenv("NPX_COMMAND_PATH")

    val openAIExecutor = simpleOpenAIExecutor(openAIApiKey)
    val geminiExecutor = simpleGoogleAIExecutor(geminiApiKey)
    val openRouterExecutor = simpleOpenRouterExecutor(openRouterApiKey)
    val ollamaExecutor = simpleOllamaAIExecutor()

    val webSearchTools = WebSearchTools(googleApiKey, searchEngineId)
    // not work in Android
    val mapTools = McpToolRegistryProvider.fromTransport(createMapMCP(mapboxAccessToken, npxCommandPath))
    val directionsTool = DirectionsTool(mapboxAccessToken)
    val calendarTool = CalendarTool

    val toolRegistry = ToolRegistry {
        tool(askUser)
        tool(feedbackTool)
        tools(webSearchTools.asTools())
        tool(directionsTool)
        tool(calendarTool)
    }

    return AIAgent<String, TripPlan>(
        promptExecutor = geminiExecutor,
        llmModel = GoogleModels.Gemini2_0Flash,
        systemPrompt = """
        あなたは旅行プランナーです。ユーザーの指示に従って、旅行計画を立ててください。
        ただしユーザーの指示が少ないときは__ask_user__ツールを使って、1度はユーザーに情報提供を促してください。
        最小限の情報があればそれでよいです。観光スポットなども含めて、あなたが考えて提案してください。
        ユーザーに行きたい場所を尋ねず、あなたが提案してください。
        最小限の情報とは、旅行先、予算のことです。
        あまり細かく聞きすぎず、ある程度分かったところで計画を立ててください
        """.trimIndent(),
        toolRegistry = toolRegistry,
        strategy = createTripPlanningStrategy(askUser, feedbackTool, webSearchTools, directionsTool, calendarTool),
        maxIterations = 100
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
                            ChatMessage.AskToolCall(
                                (it.toolArgs as AskUserInUI.Args).message
                            )
                        )
                    }

                    is FeedbackUserInUI -> {
                        onMessageUpdate(
                            ChatMessage.FeedbackToolCall(
                                (it.toolArgs as FeedbackUserInUI.Args).summary
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
