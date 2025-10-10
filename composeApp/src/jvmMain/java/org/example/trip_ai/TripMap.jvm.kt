package org.example.trip_ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun TripMap(point: ActivityPoint, modifier: Modifier) {
    // no display on desktop
}

@Composable
actual fun TripMap(line: TransportationLine, modifier: Modifier) {
    // no display on desktop
}
