package org.example.trip_ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TripMap(point: ActivityPoint, modifier: Modifier = Modifier)

@Composable
expect fun TripMap(line: TransportationLine, modifier: Modifier = Modifier)

data class Coordinate(val longitude: Double, val latitude: Double)

data class ActivityPoint(val coordinate: Coordinate)

data class TransportationLine(val line: List<Coordinate>)
