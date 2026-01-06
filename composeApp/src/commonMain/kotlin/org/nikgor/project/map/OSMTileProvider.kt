package org.nikgor.project.map

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.nikgor.project.data.HttpClientProvider
import ovh.plrapps.mapcompose.core.TileStreamProvider

fun osmTileProvider(): TileStreamProvider =
    TileStreamProvider { row, col, zoom ->
        // Standard OSM: /zoom/x/y.png
        // col = x, row = y
        val url = "https://tile.openstreetmap.org/$zoom/$col/$row.png"

        try {
            val response = HttpClientProvider.client.get(url) {
                header(HttpHeaders.UserAgent, "MarcheRouteKMP/1.0 (your@email.com)")
            }
            if (response.status.value == 200) {
                val buffer = Buffer()
                buffer.write(response.body())
                buffer
            } else null
        } catch (e: Exception) {
            null
        }
    }