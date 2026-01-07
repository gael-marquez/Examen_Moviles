package escom.ipn.m.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import escom.ipn.m.MainActivity
import escom.ipn.m.R
import escom.ipn.m.data.model.LocationData
import escom.ipn.m.data.preferences.PreferencesManager
import escom.ipn.m.data.preferences.TrackingInterval
import escom.ipn.m.data.storage.LocationStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationService : Service() {

    companion object {
        const val TAG = "LocationService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "gps_tracker_channel"
        const val CHANNEL_NAME = "Rastreo GPS"

        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"
        const val ACTION_UPDATE_INTERVAL = "ACTION_UPDATE_INTERVAL"

        const val BROADCAST_LOCATION_UPDATE = "escom.ipn.m.LOCATION_UPDATE"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_ACCURACY = "extra_accuracy"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationStorage: LocationStorage
    private lateinit var preferencesManager: PreferencesManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentInterval: Long = TrackingInterval.INTERVAL_5_MIN.milliseconds
    private var isTracking = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio creado")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationStorage = LocationStorage(this)
        preferencesManager = PreferencesManager(this)

        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
            ACTION_UPDATE_INTERVAL -> updateInterval()
            else -> startTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
        Log.d(TAG, "Servicio destruido")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para notificaciones de rastreo GPS"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis(),
                        provider = location.provider ?: "fused"
                    )

                    serviceScope.launch {
                        locationStorage.saveLocation(locationData)
                        Log.d(TAG, "Ubicación guardada: ${locationData.latitude}, ${locationData.longitude}")
                    }

                    sendLocationBroadcast(locationData)
                    updateNotification(locationData)
                }
            }
        }
    }

    private fun startTracking() {
        if (isTracking) {
            Log.d(TAG, "Ya se está rastreando")
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Sin permisos de ubicación")
            stopSelf()
            return
        }

        serviceScope.launch {
            val preferences = preferencesManager.userPreferencesFlow.first()
            currentInterval = preferences.trackingInterval.milliseconds

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                currentInterval
            ).apply {
                setMinUpdateIntervalMillis(currentInterval / 2)
                setWaitForAccurateLocation(true)
            }.build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                isTracking = true
                preferencesManager.updateTrackingEnabled(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceCompat.startForeground(
                        this@LocationService,
                        NOTIFICATION_ID,
                        createNotification(null),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, createNotification(null))
                }

                Log.d(TAG, "Rastreo iniciado con intervalo: ${currentInterval}ms")
            } catch (e: SecurityException) {
                Log.e(TAG, "Error de seguridad: ${e.message}")
                stopSelf()
            }
        }
    }

    private fun stopTracking() {
        if (!isTracking) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false

        serviceScope.launch {
            preferencesManager.updateTrackingEnabled(false)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "Rastreo detenido")
    }

    private fun updateInterval() {
        if (!isTracking) return

        serviceScope.launch {
            val preferences = preferencesManager.userPreferencesFlow.first()
            val newInterval = preferences.trackingInterval.milliseconds

            if (newInterval != currentInterval) {
                currentInterval = newInterval
                fusedLocationClient.removeLocationUpdates(locationCallback)

                if (ContextCompat.checkSelfPermission(
                        this@LocationService,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        currentInterval
                    ).apply {
                        setMinUpdateIntervalMillis(currentInterval / 2)
                        setWaitForAccurateLocation(true)
                    }.build()

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    Log.d(TAG, "Intervalo actualizado: ${currentInterval}ms")
                }
            }
        }
    }

    private fun createNotification(location: LocationData?): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = location?.let {
            "Lat: ${String.format(java.util.Locale.US, "%.4f", it.latitude)}, Lon: ${String.format(java.util.Locale.US, "%.4f", it.longitude)}"
        } ?: "Iniciando rastreo..."

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Tracker Activo")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(location: LocationData) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(location))
    }

    private fun sendLocationBroadcast(location: LocationData) {
        val intent = Intent(BROADCAST_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LATITUDE, location.latitude)
            putExtra(EXTRA_LONGITUDE, location.longitude)
            putExtra(EXTRA_ACCURACY, location.accuracy)
            putExtra(EXTRA_TIMESTAMP, location.timestamp)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}