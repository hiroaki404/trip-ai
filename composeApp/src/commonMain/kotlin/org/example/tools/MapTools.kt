package org.example.tools

import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.mcp.defaultStdioTransport
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import kotlinx.coroutines.delay

suspend fun createMapMCP(mapboxAccessToken: String, npxCommandPath: String): StdioClientTransport {
    // Start MCP server
    val process = ProcessBuilder(
        npxCommandPath,
        "-y",
        "@mapbox/mcp-server",
        "-e",
        "MAPBOX_ACCESS_TOKEN=${mapboxAccessToken}"
    ).start()

    // Stupid straightforward way to wait for the MCP server to boot
    delay(1000)

    // Create transport to MCP
    return McpToolRegistryProvider.defaultStdioTransport(process)
}
