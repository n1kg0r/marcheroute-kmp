package org.nikgor.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nikgor.project.data.*
import org.nikgor.project.routing.RoutePlanner
import org.nikgor.project.map.*
import org.nikgor.project.ui.map.*

@Composable
fun HomeScreen() {
    // --- UI state ---
    var city by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("3") }
    var loading by remember { mutableStateOf(false) }

    // --- Data state ---
    var plan by remember { mutableStateOf<RoutePlan?>(null) }
    var contour by remember { mutableStateOf<CityContour?>(null) }
    var bbox by remember { mutableStateOf<BoundingBox?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ---------- FORM ----------
        Text(
            "MarcheRoute",
            style = MaterialTheme.typography.headlineMedium
        )

        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            singleLine = true
        )

        TextField(
            value = hours,
            onValueChange = { hours = it },
            label = { Text("Hours available") },
            singleLine = true
        )

        Button(
            enabled = !loading && city.isNotBlank(),
            onClick = {
                scope.launch {
                    loading = true

                    // 1) Generate route (POIs)
                    val routePlanner = RoutePlanner()
                    val route = routePlanner.planRoute(
                        city = city,
                        hours = hours.toDoubleOrNull() ?: 3.0
                    )
                    plan = route

                    // 2) Fetch contour + bbox
                    val geocoder = GeoCoder()
                    val overpass = OverpassClient()
                    val (_, cityBbox) = geocoder.geocode(city)

                    bbox = cityBbox
                    contour = overpass.queryCityContour(city)

                    loading = false
                }
            }
        ) {
            Text("Generate route")
        }

        if (loading) {
            CircularProgressIndicator()
        }

        // ---------- MAP ----------
        if (contour != null && bbox != null) {
            CityContourCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contour = contour!!,
                bbox = bbox!!,
                pois = plan?.stops ?: emptyList()
            )
        }

        // ---------- TEXTUAL LIST (optional but useful) ----------
        plan?.let {
            Spacer(Modifier.height(8.dp))
            Text("Stops:", style = MaterialTheme.typography.titleMedium)

            it.stops.forEachIndexed { i, poi ->
                Text("${i + 1}. ${poi.name}")
            }
        }
    }
}
