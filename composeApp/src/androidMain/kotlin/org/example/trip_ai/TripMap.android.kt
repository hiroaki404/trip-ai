package org.example.trip_ai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage

@Composable
actual fun TripMap(point: ActivityPoint, modifier: Modifier) {
    if (LocalInspectionMode.current) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Blue
        ) {}
    } else {
        MapboxMap(
            modifier = modifier,
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(9.0)
                    center(point.toMapboxPoint())
                }
            }
        ) {
            MapContent(point)
        }
    }
}

@Composable
fun MapContent(activityPoint: ActivityPoint) {
    val image = rememberIconImage(R.drawable.ic_red_circle)
    PointAnnotation(point = activityPoint.toMapboxPoint()) {
        iconImage = image
    }
}

@Composable
actual fun TripMap(line: TransportationLine, modifier: Modifier) {
    if (LocalInspectionMode.current) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Blue
        ) {}
    } else {
        MapboxMap(
            modifier = modifier,
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(9.0)
                    if (line.line.isNotEmpty()) {
                        center(Point.fromLngLat(line.line.first().longitude, line.line.first().latitude))
                    }
                }
            }
        ) {
            MapContent(line)
        }
    }
}

@Composable
fun MapContent(line: TransportationLine) {
    PolylineAnnotation(
        points = line.toMapboxPoints(),
    ) {
        lineWidth = 10.0
        lineColor = Color.Red.copy(alpha = 0.5f)
    }
}

fun ActivityPoint.toMapboxPoint(): Point = Point.fromLngLat(coordinate.longitude, coordinate.latitude)

fun TransportationLine.toMapboxPoints(): List<Point> = line.map { Point.fromLngLat(it.longitude, it.latitude) }
