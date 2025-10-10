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

fun ActivityPoint.toMapboxPoint(): Point = Point.fromLngLat(longitude, latitude)
