import io.ktor.http.*
import org.example.tools.DirectionsTool

data class MapboxDirectionsConfig(
    val profile: String = "driving", // driving, walking, cycling, driving-traffic
    val alternatives: Boolean = true,
    val geometries: String = "geojson", // geojson, polyline, polyline6
    val language: String = "en",
    val overview: String = "simplified", // full, simplified, false
    val steps: Boolean = true,
    val accessToken: String
)

fun buildMapboxDirectionsUrl(
    coordinates: List<DirectionsTool.Args.Coordinate>,
    config: MapboxDirectionsConfig
): String {
    require(coordinates.size >= 2) { "少なくとも2つの座標が必要です" }
    require(coordinates.size <= 25) { "座標は最大25個までです" }

    val coordinatesString = coordinates.joinToString(";")

    val urlBuilder = URLBuilder("https://api.mapbox.com")
    urlBuilder.appendPathSegments("directions", "v5", "mapbox", config.profile, coordinatesString)

    urlBuilder.parameters.apply {
        append("alternatives", config.alternatives.toString())
        append("geometries", config.geometries)
        append("language", config.language)
        append("overview", config.overview)
        append("steps", config.steps.toString())
        append("access_token", config.accessToken)
    }

    return urlBuilder.buildString()
}
