package escom.ipn.m.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import escom.ipn.m.R
import escom.ipn.m.data.storage.LocationStorage
import escom.ipn.m.ui.theme.AccuracyExcellent
import escom.ipn.m.ui.theme.AccuracyGood
import escom.ipn.m.ui.theme.AccuracyLow
import escom.ipn.m.ui.theme.AccuracyModerate
import escom.ipn.m.ui.theme.TrackingActive
import escom.ipn.m.ui.theme.TrackingInactive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentLatitude: Double,
    currentLongitude: Double,
    currentAccuracy: Float,
    lastUpdateTimestamp:  Long,
    isTrackingEnabled:  Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings:  () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GPS Tracker",
                        fontWeight = FontWeight. Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map),
                            contentDescription = "Mapa",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            painter = painterResource(id = R. drawable.ic_history),
                            contentDescription = "Historial",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            painter = painterResource(id = R. drawable.ic_settings),
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackingStatusIndicator(isTracking = isTrackingEnabled)

            Spacer(modifier = Modifier.height(24.dp))

            LocationCard(
                latitude = currentLatitude,
                longitude = currentLongitude,
                accuracy = currentAccuracy,
                timestamp = lastUpdateTimestamp,
                isTracking = isTrackingEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))

            TrackingButton(
                isTracking = isTrackingEnabled,
                hasPermission = hasLocationPermission,
                onClick = {
                    if (! hasLocationPermission) {
                        locationPermissionLauncher. launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES. TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        if (isTrackingEnabled) {
                            onStopTracking()
                        } else {
                            onStartTracking()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ver mapa
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                OutlinedButton(
                    onClick = onNavigateToMap,
                    modifier = Modifier. fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_map),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver en Mapa")
                }
            }

            if (! hasLocationPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                PermissionWarningCard(
                    message = "Se requieren permisos de ubicación para rastrear",
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            QuickStatsCard(context = context)
        }
    }
}

@Composable
fun TrackingStatusIndicator(isTracking:  Boolean) {
    val color by animateColorAsState(
        targetValue = if (isTracking) TrackingActive else TrackingInactive,
        animationSpec = tween(500),
        label = "statusColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition. animateFloat(
        initialValue = 1f,
        targetValue = if (isTracking) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode. Reverse
        ),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .scale(if (isTracking) scale else 1f)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isTracking) "Rastreo Activo" else "Rastreo Inactivo",
            style = MaterialTheme. typography.titleMedium,
            color = color,
            fontWeight = FontWeight. SemiBold
        )
    }
}

@Composable
fun LocationCard(
    latitude: Double,
    longitude: Double,
    accuracy: Float,
    timestamp: Long,
    isTracking: Boolean
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults. cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍 Ubicación Actual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier. height(16.dp))

            if (latitude != 0.0 || longitude != 0.0) {
                CoordinateRow(label = "Latitud", value = String.format(Locale.US, "%.6f", latitude))
                Spacer(modifier = Modifier.height(8.dp))
                CoordinateRow(label = "Longitud", value = String.format(Locale.US, "%.6f", longitude))

                Spacer(modifier = Modifier. height(16.dp))

                AccuracyIndicator(accuracy = accuracy)

                Spacer(modifier = Modifier. height(12.dp))

                if (timestamp > 0) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale. getDefault())
                    Text(
                        text = "Última actualización: ${sdf.format(Date(timestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = if (isTracking) "Obteniendo ubicación..." else "Inicia el rastreo para ver tu ubicación",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CoordinateRow(label: String, value:  String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AccuracyIndicator(accuracy:  Float) {
    val (color, description) = when {
        accuracy <= 5 -> AccuracyExcellent to "Excelente"
        accuracy <= 10 -> AccuracyGood to "Muy buena"
        accuracy <= 20 -> AccuracyGood to "Buena"
        accuracy <= 50 -> AccuracyModerate to "Moderada"
        else -> AccuracyLow to "Baja"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color. copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Precisión:",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$description (${String.format(Locale.US, "%.1f", accuracy)}m)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight. SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun TrackingButton(
    isTracking:  Boolean,
    hasPermission: Boolean,
    onClick: () -> Unit
) {
    val buttonColor by animateColorAsState(
        targetValue = if (isTracking)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults. buttonColors(containerColor = buttonColor)
    ) {
        Icon(
            painter = painterResource(
                id = if (isTracking) R.drawable.ic_stop else R.drawable.ic_play
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = when {
                ! hasPermission -> "Otorgar Permisos"
                isTracking -> "Detener Rastreo"
                else -> "Iniciar Rastreo"
            },
            style = MaterialTheme. typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PermissionWarningCard(
    message:  String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults. cardColors(
            containerColor = AccuracyModerate.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = AccuracyModerate
            )
        }
    }
}

@Composable
fun QuickStatsCard(context: Context) {
    val locationStorage = remember { LocationStorage(context) }
    var locationsCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        locationsCount = locationStorage.getLocationsCount()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                value = "$locationsCount",
                label = "Ubicaciones\nregistradas"
            )
        }
    }
}

@Composable
fun StatItem(value:  String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}