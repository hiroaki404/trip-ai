package org.example.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
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

class WebSearchTools(
    private val googleApiKey: String,
    private val searchEngineId: String,
) : ToolSet {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }

    @Tool
    @LLMDescription("Search for a query on Google.")
    @Suppress("unused")
    suspend fun search(
        @LLMDescription("The query to search")
        query: String,
        @LLMDescription("Number of results to return (1-10, default: 10)")
        num: Int = 10,
    ): WebSearchResult {
        val response =
            httpClient.get("https://www.googleapis.com/customsearch/v1") {
                parameter("key", googleApiKey)
                parameter("cx", searchEngineId)
                parameter("q", query)
                parameter("num", num.coerceIn(1, 10))
            }

        return response.body<WebSearchResult>()
    }

    @Tool
    @LLMDescription("Scrape a web page for content")
    suspend fun scrape(
        @LLMDescription("The URL to scrape")
        url: String,
    ): WebPageScrapingResult {
        val response =
            httpClient.get(url) {
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

        return WebPageScrapingResult(body = textContent)
    }
}
