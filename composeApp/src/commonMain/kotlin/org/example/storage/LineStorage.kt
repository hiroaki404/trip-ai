package org.example.storage

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.agent.Line

@Serializable
data class LinesData(
    val lines: Map<String, Line>
)

object LineStorage {
    private val storage = InMemoryLineStorage
    private val file = File("lines.json")

    init {
        if (file.exists()) {
            try {
                val jsonString = file.readText()
                if (jsonString.isNotBlank()) {
                    storage.deserializeLines(jsonString)
                }
            } catch (e: Exception) {
                println("Failed to load lines from file: ${e.message}")
            }
        }
    }

    fun saveLine(line: Line) {
        storage.saveLineInMemory(line)
        saveToFile()
    }

    fun getLine(id: String): Line? {
        return storage.getLineFromMemory(id)
    }

    fun getAllLines(): Map<String, Line> {
        return storage.getAllLinesFromMemory()
    }

    private fun saveToFile() {
        try {
            val jsonString = storage.serializeLines()
            file.writeText(jsonString)
        } catch (e: Exception) {
            println("Failed to save lines to file: ${e.message}")
        }
    }
}

object InMemoryLineStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val lines = mutableMapOf<String, Line>()

    fun saveLineInMemory(line: Line) {
        lines[line.id] = line
    }

    fun getLineFromMemory(id: String): Line? {
        return lines[id]
    }

    fun getAllLinesFromMemory(): Map<String, Line> {
        return lines.toMap()
    }

    fun serializeLines(): String {
        val linesData = LinesData(
            lines = lines.mapValues { it.value }
        )

        return json.encodeToString(linesData)
    }

    fun deserializeLines(jsonString: String) {
        val linesData = json.decodeFromString<LinesData>(jsonString)
        lines.clear()
        lines.putAll(linesData.lines.mapValues { it.value })
    }
}
