package org.example.trip_ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TripMap(point: ActivityPoint, modifier: Modifier = Modifier)

data class ActivityPoint(val longitude: Double, val latitude: Double)
