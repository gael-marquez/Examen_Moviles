package escom.ipn.m.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform. LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import escom.ipn.m.R
import escom.ipn.m.data.storage.LocationStorage
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext. current
    val locationStorage = remember { LocationStorage(context) }
    val locations = remember { locationStorage.getLocationsSortedByDate(ascending = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mapa de Recorrido",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme. colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(innerPadding)
        ) {
            if (locations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No hay ubicaciones registradas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Mapa OpenStreetMap
                val mapView = remember { MapView(context) }

                DisposableEffect(Unit) {
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    mapView.setMultiTouchControls(true)
                    mapView.controller.setZoom(15.0)

                    // Centrar en la última ubicación
                    val lastLocation = locations.last()
                    val centerPoint = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                    mapView.controller.setCenter(centerPoint)

                    // Agregar marcadores para cada ubicación
                    locations.forEachIndexed { index, location ->
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(location.latitude, location.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Punto ${index + 1}"
                        marker.snippet = location.getFormattedDateTime()
                        mapView.overlays.add(marker)
                    }

                    // Dibujar línea del recorrido si hay más de un punto
                    if (locations.size > 1) {
                        val polyline = Polyline()
                        polyline.setPoints(locations.map { GeoPoint(it. latitude, it.longitude) })
                        polyline.outlinePaint. color = android.graphics.Color. BLUE
                        polyline.outlinePaint.strokeWidth = 5f
                        mapView.overlays. add(0, polyline)
                    }

                    onDispose {
                        mapView.onDetach()
                    }
                }

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}