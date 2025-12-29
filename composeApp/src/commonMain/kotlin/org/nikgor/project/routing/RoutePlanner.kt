package org.nikgor.project.routing
import org.nikgor.project.data.*




class RoutePlanner {

    private val geocoder = GeoCoder()
    private val overpass = OverpassClient()

    suspend fun planRoute(
        city: String,
        hours: Double
    ): RoutePlan {
        val (center, bbox) = geocoder.geocode(city)
        val pois = overpass.queryPois(bbox)

        val maxStops = (hours * 2).toInt().coerceAtLeast(1)
        val selected = pois.take(maxStops)

        return RoutePlan(
            city = city,
            center = center,
            stops = selected
        )
    }
}
