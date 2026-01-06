package org.nikgor.project.map

import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.ui.state.MapState
import kotlin.math.pow


class MapViewModel {
    private val maxZoom = 19
    private val tileSize = 256

    // Calculate as Double first to avoid overflow issues during intermediate steps
    private val mapSize = (tileSize.toDouble() * 2.0.pow(maxZoom)).toInt()

    val mapState = MapState(
        levelCount = maxZoom + 1,
        fullWidth = mapSize,
        fullHeight = mapSize,
        workerCount = 16
    ).apply {
        addLayer(osmTileProvider())
    }

    suspend fun centerOn(lat: Double, lon: Double) {
        // Important: mapcompose-mp coordinates are normalized [0..1]
        // scrollTo uses these normalized values
        mapState.scrollTo(
            x = lonToX(lon),
            y = latToY(lat),
            destScale = 0.05 // Try a smaller scale if the map looks empty
        )
    }
}