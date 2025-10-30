package org.example.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// refer to https://github.com/JetBrains/koog/blob/0260bcf0753dd7218caf1e041eae7cd2687f32aa/examples/simple-examples/src/main/kotlin/ai/koog/agents/example/websearch/WebSearchAgent.kt

private val json =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
    }

@Serializable
data class WebSearchResult(
    val items: List<SearchItem>? = null,
) {
    @Serializable
    data class SearchItem(
        val link: String,
        val title: String,
        val snippet: String? = null,
        val displayLink: String? = null,
    )
}

@Serializable
data class WebPageScrapingResult(
    val body: String,
)

class WebSearchTool(
    private val googleApiKey: String,
    private val searchEngineId: String,
) : SimpleTool<WebSearchTool.Args>() {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }

    @Serializable
    data class Args(
        @property:LLMDescription("The query to search")
        val query: String,
        @property:LLMDescription("Number of results to return (1-10, default: 10)")
        val num: Int = 10,
    )

    override val name: String = "webSearch"
    override val description: String = "Search for a query on Google."
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override suspend fun doExecute(args: Args): String {
        val response =
            httpClient.get("https://www.googleapis.com/customsearch/v1") {
                parameter("key", googleApiKey)
                parameter("cx", searchEngineId)
                parameter("q", args.query)
                parameter("num", args.num.coerceIn(1, 10))
            }

        val result = response.body<WebSearchResult>()
        return Json.encodeToString(WebSearchResult.serializer(), result)
    }
}

object WebScrapeTool : SimpleTool<WebScrapeTool.Args>() {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }

    @Serializable
    data class Args(
        @property:LLMDescription("The URL to scrape")
        val url: String,
    )

    override val name: String = "Scrape"
    override val description: String = "Scrape a web page for content"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override suspend fun doExecute(args: Args): String {
        val response =
            httpClient.get(args.url) {
                header("User-Agent", "Mozilla/5.0 (compatible; WebSearchBot/1.0)")
            }

        val htmlContent: String = response.body()

        // Simple HTML to text conversion (removes tags)
        val textContent =
            htmlContent
                .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
                .replace(Regex("<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        val result = WebPageScrapingResult(body = textContent)
        return Json.encodeToString(WebPageScrapingResult.serializer(), result)
    }
}
