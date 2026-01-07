package escom.ipn.m

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import escom.ipn.m.data.preferences.PreferencesManager
import escom. ipn.m.data.preferences.UserPreferences
import escom.ipn.m.service.LocationService
import escom.ipn.m.ui.screens.HistoryScreen
import escom.ipn. m.ui.screens.HomeScreen
import escom.ipn.m.ui.screens.MapScreen
import escom.ipn.m.ui.screens.SettingsScreen
import escom.ipn.m.ui.theme.GPSTrackerTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    // Estados observables para la ubicación actual
    private var currentLatitude by mutableDoubleStateOf(0.0)
    private var currentLongitude by mutableDoubleStateOf(0.0)
    private var currentAccuracy by mutableFloatStateOf(0f)
    private var lastUpdateTimestamp by mutableLongStateOf(0L)
    private var isTrackingActive by mutableStateOf(false)

    // Receiver para actualizaciones de ubicación del servicio
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context:  Context?, intent: Intent?) {
            intent?.let {
                currentLatitude = it.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0)
                currentLongitude = it.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0)
                currentAccuracy = it.getFloatExtra(LocationService.EXTRA_ACCURACY, 0f)
                lastUpdateTimestamp = it.getLongExtra(LocationService. EXTRA_TIMESTAMP, 0L)
                isTrackingActive = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurar OSMDroid
        Configuration.getInstance().userAgentValue = packageName

        preferencesManager = PreferencesManager(this)

        setContent {
            val userPreferences by preferencesManager.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            GPSTrackerTheme(
                appTheme = userPreferences.theme,
                darkTheme = userPreferences.isDarkMode
            ) {
                MainApp(
                    currentLatitude = currentLatitude,
                    currentLongitude = currentLongitude,
                    currentAccuracy = currentAccuracy,
                    lastUpdateTimestamp = lastUpdateTimestamp,
                    isTrackingEnabled = isTrackingActive,
                    onStartTracking = {startLocationService()},
                    onStopTracking = {stopLocationService()}
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationService.BROADCAST_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationReceiver, filter,RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(locationReceiver, filter)
        }
    }

    override fun onPause() {
        super. onPause()
        try {
            unregisterReceiver(locationReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        ContextCompat.startForegroundService(this, serviceIntent)
        isTrackingActive = true
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        startService(serviceIntent)
        isTrackingActive = false
    }
}

@Composable
fun MainApp(
    currentLatitude:  Double,
    currentLongitude: Double,
    currentAccuracy: Float,
    lastUpdateTimestamp: Long,
    isTrackingEnabled: Boolean,
    onStartTracking:  () -> Unit,
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
                    onNavigateToSettings = { navController. navigate("settings") },
                    onNavigateToMap = { navController.navigate("map") }
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

            composable("map") {
                MapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}