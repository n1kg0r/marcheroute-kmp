package org.nikgor.project.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import org.nikgor.project.data.*
import org.nikgor.project.map.normalizePoints
import androidx.compose.ui.graphics.drawscope.Stroke


@Composable
fun CityContourCanvas(
    contour: CityContour,
    bbox: BoundingBox,
    modifier: Modifier = Modifier,
    pois: List<Poi> = emptyList()
) {
    val normalized = remember(contour) {
        normalizePoints(contour.points, bbox)
    }

    val normalizedPois = remember(pois, bbox) {
        pois.map { poi ->
            val x = (poi.lon - bbox.west) / (bbox.east - bbox.west)
            val y = 1.0 - (poi.lat - bbox.south) / (bbox.north - bbox.south)
            x.toFloat() to y.toFloat()
        }
    }


    Canvas(modifier = modifier.fillMaxSize()) {
        if (normalized.size < 2) return@Canvas

        val path = Path().apply {
            val (sx, sy) = normalized.first()
            moveTo(sx * size.width, sy * size.height)

            normalized.drop(1).forEach { (x, y) ->
                lineTo(x * size.width, y * size.height)
            }
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFF2E7D32),
            style = Stroke(width = 4f)
        )

        if (normalizedPois.size >= 2) {
            val poiPath = Path().apply {
                val (sx, sy) = normalizedPois.first()
                moveTo(sx * size.width, sy * size.height)

                normalizedPois.drop(1).forEach { (x, y) ->
                    lineTo(x * size.width, y * size.height)
                }
            }

            drawPath(
                path = poiPath,
                color = Color(0xFF90CAF9), // light blue
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(12f, 8f)
                    )
                )
            )
        }

        normalizedPois.forEach { (x, y) ->
            drawCircle(
                color = Color(0xFFD32F2F),
                radius = 6f,
                center = Offset(
                    x * size.width,
                    y * size.height
                )
            )
        }


    }
}
