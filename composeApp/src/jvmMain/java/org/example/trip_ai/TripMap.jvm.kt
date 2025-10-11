package org.example.trip_ai

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mapbox.api.staticmap.v1.MapboxStaticMap
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation
import com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils

@Composable
actual fun TripMap(point: ActivityPoint, modifier: Modifier) {
    val point = point.toMapboxPoint()
    val mapboxAccessToken = System.getenv("MAPBOX_ACCESS_TOKEN")

    val markerAnnotation = point.let {
        StaticMarkerAnnotation.builder()
            .lnglat(it)
            .build()
    }

    val url = MapboxStaticMap.builder()
        .accessToken(mapboxAccessToken)
        .cameraPoint(point)
        .cameraZoom(8.0)
        .staticMarkerAnnotations(listOf(markerAnnotation))
        .build()

    AsyncImage(
        model = "${url.url()}",
        contentDescription = null,
        modifier = modifier.size(200.dp)
    )
}

@Composable
actual fun TripMap(line: TransportationLine, modifier: Modifier) {
    val points = line.toMapboxPoints()
    val mapboxAccessToken = System.getenv("MAPBOX_ACCESS_TOKEN")

    if (points.isEmpty()) {
        Text("No route data.")
        return
    }


    val polyline = PolylineUtils.encode(points, 5)

    val staticPolylineAnnotation = StaticPolylineAnnotation.builder()
        .polyline(polyline)
        .strokeWidth(4.0)
        .build()


    val markers = mutableListOf<StaticMarkerAnnotation>()

    // first marker
    points.firstOrNull()?.let {
        markers.add(
            StaticMarkerAnnotation.builder()
                .lnglat(it)
                .build()
        )
    }

    // end marker
    if (points.size >= 2) {
        points.lastOrNull()?.let {
            markers.add(
                StaticMarkerAnnotation.builder()
                    .lnglat(it)
                    .build()
            )
        }
    }

    val url = MapboxStaticMap.builder()
        .accessToken(mapboxAccessToken)
        .staticPolylineAnnotations(listOf(staticPolylineAnnotation))
        .cameraAuto(true)
        .staticMarkerAnnotations(markers)
        .build()

    AsyncImage(
        model = "${url.url()}",
        contentDescription = null,
        modifier = modifier.size(200.dp)
    )
}

fun ActivityPoint.toMapboxPoint(): Point = Point.fromLngLat(coordinate.longitude, coordinate.latitude)

fun TransportationLine.toMapboxPoints(): List<Point> = line.map { Point.fromLngLat(it.longitude, it.latitude) }
