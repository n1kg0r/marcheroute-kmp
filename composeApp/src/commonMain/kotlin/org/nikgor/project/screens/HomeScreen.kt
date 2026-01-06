package org.nikgor.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nikgor.project.data.*
import org.nikgor.project.routing.RoutePlanner
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.api.*

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.Icons

import org.nikgor.project.map.*

import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun HomeScreen() {

    // ---------- UI STATE ----------
    var city by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("3") }
    var loading by remember { mutableStateOf(false) }

    // ---------- DATA ----------
    var plan by remember { mutableStateOf<RoutePlan?>(null) }

    // Used ONLY to trigger centering after layout
    var pendingCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val mapViewModel = remember { MapViewModel() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ---------- FORM ----------
        Text("MarcheRoute", style = MaterialTheme.typography.headlineMedium)

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

                    val route = RoutePlanner().planRoute(
                        city = city,
                        hours = hours.toDoubleOrNull() ?: 3.0
                    )

                    plan = route
                    pendingCenter = route.center.lat to route.center.lon

                    loading = false
                }
            }
        ) {
            Text("Generate route")
        }

        if (loading) CircularProgressIndicator()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val delta = event.changes.first().scrollDelta.y
                                val factor = if (delta > 0) 0.9f else 1.1f

                                mapViewModel.mapState.scale *= factor
                            }
                        }
                    }
                }
        ) {
            MapUI(
                modifier = Modifier.fillMaxSize(),
                state = mapViewModel.mapState
            )
        }

        // ---------- APPLY ROUTE ----------
        // ... inside HomeScreen Composable ...
        LaunchedEffect(plan) {
            val currentPlan = plan ?: return@LaunchedEffect

            // 1. Clear previous markers/paths if necessary
            mapViewModel.mapState.removePath("route")

            // 2. Add POI markers
            currentPlan.stops.forEach { poi ->
                mapViewModel.mapState.addMarker(
                    id = "poi-${poi.id}",
                    x = lonToX(poi.lon),
                    y = latToY(poi.lat)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = poi.name,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Red
                    )
                }
            }

            // 3. Draw the walking route
            mapViewModel.mapState.addPath(
                id = "route",
                color = Color(0xFF1E88E5),
                width = 4.dp
            ) {
                addPoints(currentPlan.stops.map {
                    lonToX(it.lon) to latToY(it.lat)
                })
            }

            // 4. Center the map
            mapViewModel.centerOn(currentPlan.center.lat, currentPlan.center.lon)
        }


        // ---------- TEXT LIST ----------
        plan?.let {
            Spacer(Modifier.height(8.dp))
            Text("Stops:", style = MaterialTheme.typography.titleMedium)
            it.stops.forEachIndexed { i, poi ->
                Text("${i + 1}. ${poi.name}")
            }
        }
    }
}
