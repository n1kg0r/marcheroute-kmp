package org.nikgor.project.map

import kotlin.math.*

fun lonToX(lon: Double): Double = (lon + 180.0) / 360.0

fun latToY(lat: Double): Double {
    // Replace Math.toRadians(lat) with (lat * PI / 180.0)
    val latRad = lat * PI / 180.0
    val merc = ln(tan(PI / 4.0) + (latRad / 2.0))
    return (1.0 - (merc / PI)) / 2.0
}