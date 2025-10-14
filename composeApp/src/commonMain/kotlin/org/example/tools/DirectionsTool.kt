package org.example.tools

import MapboxDirectionsConfig
import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import buildMapboxDirectionsUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.agent.Line
import org.example.storage.LineStorage

@Serializable
data class RouteResponse(
    val routes: List<Route>,
    val code: String,
    val uuid: String
) {
    @Serializable
    data class Route(
        val duration: Double,
        val distance: Double,
        val geometry: Geometry
    ) {
        @Serializable
        data class Geometry(
            val coordinates: List<List<Double>>,
        )
    }

    fun toLine(): Line {
        val coordinates = routes.firstOrNull()?.geometry?.coordinates?.map {
            Line.Point(it[0], it[1])
        } ?: emptyList()
        return Line(id = uuid, points = coordinates)
    }
}


class DirectionsTool(
    val mapboxAccessToken: String,
) : SimpleTool<DirectionsTool.Args>() {
    @Serializable
    data class Args(
        @property:LLMDescription("Array of coordinate objects with longitude and latitude properties to visit in order. Must include at least 2 coordinate pairs (starting and ending points). Up to 25 coordinates total are supported.")
        val coordinates: List<Coordinate>

    ) {
        @Serializable
        data class Coordinate(
            @property:LLMDescription("Longitude of the coordinate")
            val longitude: Double,
            @property:LLMDescription("Latitude of the coordinate")
            val latitude: Double
        ) {
            override fun toString(): String = "$longitude,$latitude"
        }
    }

    override val name: String = "directions_tool"
    override val description: String = "Get lineId of directions from one place to another using Mapbox API"
    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override suspend fun doExecute(args: Args): String {
        val dynamicUrl = buildMapboxDirectionsUrl(
            coordinates = args.coordinates,
            config = MapboxDirectionsConfig(accessToken = mapboxAccessToken)
        )
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val response = client.get(dynamicUrl)
        val line = response.body<RouteResponse>().toLine()

        LineStorage.saveLine(line)

        return line.id
    }
}
