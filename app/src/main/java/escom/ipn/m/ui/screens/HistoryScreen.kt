package escom.ipn.m.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy. LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose. foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose. material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui. text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit. dp
import escom.ipn. m.R
import escom.ipn.m.data.model.LocationData
import escom. ipn.m. data.storage.LocationStorage
import escom.ipn. m.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext. current
    val locationStorage = remember { LocationStorage(context) }

    var locations by remember { mutableStateOf(locationStorage.getLocationsSortedByDate()) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<LocationData?>(null) }

    // Diálogo para confirmar limpieza de historial
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpiar historial") },
            text = { Text("¿Estás seguro de que deseas eliminar todo el historial de ubicaciones?  Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        locationStorage.clearAllLocations()
                        locations = emptyList()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults. textButtonColors(
                        contentColor = MaterialTheme.colorScheme. error
                    )
                ) {
                    Text("Eliminar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para confirmar eliminación individual
    showDeleteDialog?. let { location ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar ubicación") },
            text = {
                Text("¿Eliminar la ubicación registrada el ${location.getFormattedDateTime()}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        locationStorage.deleteLocation(location. id)
                        locations = locationStorage.getLocationsSortedByDate()
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme. colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de Ubicaciones",
                        fontWeight = FontWeight. Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R. drawable.ic_back),
                            contentDescription = "Volver",
                            tint = MaterialTheme. colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (locations.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable. ic_delete),
                                contentDescription = "Limpiar historial",
                                tint = MaterialTheme. colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Contador de ubicaciones
            if (locations.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${locations.size} ubicaciones registradas",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (locations.isEmpty()) {
                // Estado vacío
                EmptyHistoryState()
            } else {
                // Lista de ubicaciones
                LazyColumn(
                    modifier = Modifier. fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement. spacedBy(12.dp)
                ) {
                    items(
                        items = locations,
                        key = { it.id }
                    ) { location ->
                        LocationHistoryItem(
                            location = location,
                            onDelete = { showDeleteDialog = location }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable. ic_history),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant. copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sin ubicaciones registradas",
            style = MaterialTheme.typography. titleLarge,
            fontWeight = FontWeight. SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia el rastreo para comenzar a guardar tu historial de ubicaciones",
            style = MaterialTheme.typography. bodyMedium,
            textAlign = TextAlign. Center,
            color = MaterialTheme. colorScheme.onSurfaceVariant. copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationHistoryItem(
    location: LocationData,
    onDelete: () -> Unit
) {
    val accuracyColor = when {
        location.accuracy <= 5 -> AccuracyExcellent
        location.accuracy <= 10 -> AccuracyGood
        location.accuracy <= 20 -> AccuracyGood
        location. accuracy <= 50 -> AccuracyModerate
        else -> AccuracyLow
    }

    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults. cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de precisión
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accuracyColor. copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R. drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accuracyColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de ubicación
            Column(
                modifier = Modifier. weight(1f)
            ) {
                // Fecha y hora
                Text(
                    text = location.getFormattedDateTime(),
                    style = MaterialTheme.typography. titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Coordenadas
                Text(
                    text = location.getFormattedCoordinates(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Precisión
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accuracyColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location.getAccuracyDescription(),
                        style = MaterialTheme. typography.bodySmall,
                        color = accuracyColor
                    )
                }
            }

            // Botón eliminar
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Eliminar",
                    tint = MaterialTheme. colorScheme.error. copy(alpha = 0.7f)
                )
            }
        }
    }
}