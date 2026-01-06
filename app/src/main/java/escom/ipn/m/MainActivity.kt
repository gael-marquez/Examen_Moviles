package escom.ipn.m

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content. IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import escom.ipn.m.data.preferences.AppTheme
import escom.ipn.m.data.preferences. PreferencesManager
import escom.ipn.m.service.LocationService
import escom.ipn.m.ui.screens.HistoryScreen
import escom.ipn.m.ui.screens.HomeScreen
import escom.ipn.m.ui.screens.SettingsScreen
import escom.ipn.m.ui.theme.GPSTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    // Receiver para actualizaciones de ubicación del servicio
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val latitude = it.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                val longitude = it.getDoubleExtra(LocationService. EXTRA_LONGITUDE, 0.0)
                val accuracy = it.getFloatExtra(LocationService.EXTRA_ACCURACY, 0f)
                val timestamp = it.getLongExtra(LocationService. EXTRA_TIMESTAMP, 0L)

                // Actualizar estado en Compose
                currentLatitude. doubleValue = latitude
                currentLongitude.doubleValue = longitude
                currentAccuracy. floatValue = accuracy
                lastUpdateTimestamp.longValue = timestamp
            }
        }
    }

    // Estados observables para la ubicación actual
    private val currentLatitude = mutableDoubleStateOf(0.0)
    private val currentLongitude = mutableDoubleStateOf(0.0)
    private val currentAccuracy = mutableFloatStateOf(0f)
    private val lastUpdateTimestamp = mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)

        setContent {
            val userPreferences by preferencesManager. userPreferencesFlow
                .collectAsStateWithLifecycle(
                    initialValue = escom.ipn. m.data.preferences.UserPreferences()
                )

            GPSTrackerTheme(
                appTheme = userPreferences.theme,
                darkTheme = userPreferences.isDarkMode
            ) {
                MainApp(
                    currentLatitude = currentLatitude.doubleValue,
                    currentLongitude = currentLongitude.doubleValue,
                    currentAccuracy = currentAccuracy.floatValue,
                    lastUpdateTimestamp = lastUpdateTimestamp.longValue,
                    isTrackingEnabled = userPreferences.isTrackingEnabled,
                    onStartTracking = { startLocationService() },
                    onStopTracking = { stopLocationService() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Registrar receiver para actualizaciones de ubicación
        val filter = IntentFilter(LocationService.BROADCAST_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. TIRAMISU) {
            registerReceiver(locationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationReceiver, filter)
        }
    }

    override fun onPause() {
        super. onPause()
        try {
            unregisterReceiver(locationReceiver)
        } catch (e:  Exception) {
            e.printStackTrace()
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationService:: class.java).apply {
            action = LocationService.ACTION_STOP
        }
        startService(serviceIntent)
    }
}

@Composable
fun MainApp(
    currentLatitude: Double,
    currentLongitude: Double,
    currentAccuracy: Float,
    lastUpdateTimestamp:  Long,
    isTrackingEnabled:  Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier. fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    currentLatitude = currentLatitude,
                    currentLongitude = currentLongitude,
                    currentAccuracy = currentAccuracy,
                    lastUpdateTimestamp = lastUpdateTimestamp,
                    isTrackingEnabled = isTrackingEnabled,
                    onStartTracking = onStartTracking,
                    onStopTracking = onStopTracking,
                    onNavigateToHistory = { navController. navigate("history") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("history") {
                HistoryScreen(
                    onNavigateBack = { navController. popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}