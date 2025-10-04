package org.example.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor

fun createTripAgent(): AIAgent<String, String> {
    val apiKey = System.getenv("OPENAI_API_KEY")
    val executor = simpleOpenAIExecutor(apiKey)

    val toolRegistry = ToolRegistry {
        tool(AskUser)
    }

    return AIAgent(
        promptExecutor = executor,
        llmModel = OpenAIModels.Reasoning.O4Mini,
        systemPrompt = """
        あなたは旅行プランナーです。ユーザーの指示に従って、旅行計画を立ててください。
        ただしユーザーの指示が少ないときは__ask_user__ツールを使って、1度はユーザーに情報提供を促してください。
        最小限の情報があればそれでよいです。観光スポットなども含めて、あなたが考えて提案してください。
        ユーザーに行きたい場所を尋ねず、あなたが提案してください。
        最小限の情報とは、旅行先、予算のことです。
        あまり細かく聞きすぎず、ある程度分かったところで計画を立ててください
        """.trimIndent(),
        toolRegistry = toolRegistry,
        strategy = createTripPlanningStrategy()
    ) {
        install(OpenTelemetry) {
            setVerbose(true)
            addLangfuseExporter()
        }
    }
}
