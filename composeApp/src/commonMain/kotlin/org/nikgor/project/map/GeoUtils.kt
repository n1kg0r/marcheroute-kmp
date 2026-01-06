package org.nikgor.project.map

import kotlin.math.*

fun lonToX(lon: Double): Double = (lon + 180.0) / 360.0

fun latToY(lat: Double): Double {
    val latRad = lat * PI / 180.0
    // FIX: Parentheses now correctly enclose (PI/4 + lat/2) inside tan()
    val merc = ln(tan((PI / 4.0) + (latRad / 2.0)))

    // Web Mercator Y (0 = North, 1 = South)
    return ((1.0 - (merc / PI)) / 2.0).coerceIn(0.0, 1.0)
}