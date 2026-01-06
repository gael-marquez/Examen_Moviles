package escom.ipn.m.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gps_tracker_settings")

/**
 * Enumeración para los temas disponibles
 */
enum class AppTheme {
    GUINDA_IPN,    // Tema Guinda (IPN)
    AZUL_ESCOM     // Tema Azul (ESCOM)
}

/**
 * Enumeración para los intervalos de rastreo
 */
enum class TrackingInterval(val milliseconds: Long, val displayName: String) {
    INTERVAL_1_MIN(60_000L, "1 minuto"),
    INTERVAL_5_MIN(300_000L, "5 minutos"),
    INTERVAL_15_MIN(900_000L, "15 minutos"),
    INTERVAL_30_MIN(1_800_000L, "30 minutos"),
    INTERVAL_1_HOUR(3_600_000L, "1 hora")
}

/**
 * Clase de datos para las preferencias de la app
 */
data class UserPreferences(
    val theme: AppTheme = AppTheme.GUINDA_IPN,
    val trackingInterval:  TrackingInterval = TrackingInterval. INTERVAL_5_MIN,
    val isTrackingEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val showAccuracy: Boolean = true,
    val notificationsEnabled: Boolean = true
)

/**
 * Gestor de preferencias usando DataStore
 */
class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val TRACKING_INTERVAL = stringPreferencesKey("tracking_interval")
        val IS_TRACKING_ENABLED = booleanPreferencesKey("is_tracking_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SHOW_ACCURACY = booleanPreferencesKey("show_accuracy")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    /**
     * Flow de preferencias del usuario
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            theme = try {
                AppTheme.valueOf(preferences[PreferencesKeys.THEME] ?: AppTheme.GUINDA_IPN.name)
            } catch (e: Exception) {
                AppTheme.GUINDA_IPN
            },
            trackingInterval = try {
                TrackingInterval. valueOf(
                    preferences[PreferencesKeys.TRACKING_INTERVAL] ?:  TrackingInterval. INTERVAL_5_MIN.name
                )
            } catch (e:  Exception) {
                TrackingInterval. INTERVAL_5_MIN
            },
            isTrackingEnabled = preferences[PreferencesKeys.IS_TRACKING_ENABLED] ?: false,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
            showAccuracy = preferences[PreferencesKeys.SHOW_ACCURACY] ?: true,
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?:  true
        )
    }

    /**
     * Actualiza el tema seleccionado
     */
    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    /**
     * Actualiza el intervalo de rastreo
     */
    suspend fun updateTrackingInterval(interval: TrackingInterval) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRACKING_INTERVAL] = interval.name
        }
    }

    /**
     * Actualiza el estado del rastreo
     */
    suspend fun updateTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TRACKING_ENABLED] = enabled
        }
    }

    /**
     * Actualiza el modo oscuro/claro
     */
    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys. IS_DARK_MODE] = isDarkMode
        }
    }

    /**
     * Actualiza si se muestra la precisión
     */
    suspend fun updateShowAccuracy(show: Boolean) {
        context. dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ACCURACY] = show
        }
    }

    /**
     * Actualiza las notificaciones
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context. dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Obtiene el tema actual como Flow
     */
    val themeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        try {
            AppTheme.valueOf(preferences[PreferencesKeys.THEME] ?: AppTheme. GUINDA_IPN.name)
        } catch (e: Exception) {
            AppTheme. GUINDA_IPN
        }
    }

    /**
     * Obtiene el intervalo actual como Flow
     */
    val trackingIntervalFlow: Flow<TrackingInterval> = context.dataStore.data.map { preferences ->
        try {
            TrackingInterval.valueOf(
                preferences[PreferencesKeys. TRACKING_INTERVAL] ?: TrackingInterval.INTERVAL_5_MIN.name
            )
        } catch (e:  Exception) {
            TrackingInterval. INTERVAL_5_MIN
        }
    }

    /**
     * Obtiene el estado del modo oscuro como Flow
     */
    val isDarkModeFlow: Flow<Boolean> = context. dataStore.data. map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }

    /**
     * Obtiene el estado del rastreo como Flow
     */
    val isTrackingEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_TRACKING_ENABLED] ?: false
    }
}