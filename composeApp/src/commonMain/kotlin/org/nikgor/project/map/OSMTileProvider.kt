package org.nikgor.project.map

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header // Add this import
import io.ktor.http.HttpHeaders // Add this import
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.nikgor.project.data.HttpClientProvider
import ovh.plrapps.mapcompose.core.TileStreamProvider


fun osmTileProvider(): TileStreamProvider =
    TileStreamProvider { row, col, zoom ->
        val url = "https://tile.openstreetmap.org/$zoom/$col/$row.png"

        // Manual log so you see it even if SLF4J is broken
        println("Fetching Tile: $url")

        try {
            val response = HttpClientProvider.client.get(url) {
                header(HttpHeaders.UserAgent, "MarcheRouteKMP/1.0 (gorbachev.nm@gmail.com)")
            }

            if (response.status.value == 200) {
                val bytes: ByteArray = response.body()
                val buffer = Buffer()
                buffer.write(bytes)
                buffer
            } else {
                println("OSM Tile Error: ${response.status} for $url")
                null
            }
        } catch (e: Exception) {
            println("OSM Network Failure: ${e.message}")
            e.printStackTrace()
            null
        }
    }