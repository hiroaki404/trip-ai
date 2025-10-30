package org.example.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
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

    val webSearchTool = WebSearchTool(googleApiKey, searchEngineId)
    val webScrapeTool = WebScrapeTool
    // not work in Android
    val mapTools = McpToolRegistryProvider.fromTransport(createMapMCP(mapboxAccessToken, npxCommandPath))
    val directionsTool = DirectionsTool(mapboxAccessToken)
    val calendarTool = CalendarTool

    val toolRegistry = ToolRegistry {
        tool(askUser)
        tool(feedbackTool)
        tool(webSearchTool)
        tool(webScrapeTool)
        tool(directionsTool)
        tool(calendarTool)
    }

    return AIAgent<String, TripPlan>(
        promptExecutor = openRouterExecutor,
        llmModel = OpenRouterModels.Gemini2_5Flash,
        systemPrompt = """
        あなたは旅行プランナーです。ユーザーの指示に従って、旅行計画を立ててください。
        ただしユーザーの指示が少ないときは__ask_user__ツールを使って、1度はユーザーに情報提供を促してください。
        最小限の情報があればそれでよいです。観光スポットなども含めて、あなたが考えて提案してください。
        ユーザーに行きたい場所を尋ねず、あなたが提案してください。
        最小限の情報とは、旅行先、予算のことです。
        あまり細かく聞きすぎず、ある程度分かったところで計画を立ててください
        
        ユーザーの希望を聞き出したらその後、ツールを役立てて旅行計画を立ててください。
        ツールはsearch、scrape、directions_toolがあります。
        ツールは必要に応じて使ってください。ただし使いすぎるとコストが悪化するため、全体の旅行プランに対してツールの使用回数は合計8回までにとどめてください。
        使用回数の目安は改めて指示を出します。
        **ツールの使用上限は絶対に守ってください**
        
        そして旅行計画を作成後、ユーザーにフィードバッグを求めます。
        __feedback_user__ツールを使ってユーザーに提示します。このとき承諾が得られれば、
        もはや_feedback_user__ツールを使わないで、計画を確定させます。
        承諾した場合nullを出力してこのフェーズを終わります。
        ユーザーが拒否した場合また計画を直し、一旦追加の要望を出力して処理を終了して前のフェーズに戻ります。
        旅行計画が確定されたらcalendar_toolを使ってカレンダーに登録します。calendar_toolは一度しか使ってはいけません。
        詳細の指示は改めて指示を出します。
        """.trimIndent(),
        toolRegistry = toolRegistry,
        strategy = createTripPlanningStrategy(
            askUser,
            feedbackTool,
            webSearchTool,
            webScrapeTool,
            directionsTool,
            calendarTool
        ),
        maxIterations = 150
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
                        // for presentation
                        val toolName = if (tool.name == "directions_tool") "mapTool" else tool.name
                        val content =
                            if (toolName == "webSearch") (it.toolArgs as WebSearchTool.Args).query else null
                        onMessageUpdate(
                            ChatMessage.ToolCall(
                                toolName,
                                content
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
