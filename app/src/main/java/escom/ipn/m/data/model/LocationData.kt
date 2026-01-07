package escom.ipn.m.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de datos para almacenar información de ubicación
 */
data class LocationData(
    val id: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude:  Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "gps"
) {
    /**
     * Obtiene la fecha formateada
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Obtiene la hora formateada
     */
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Obtiene fecha y hora formateadas
     */
    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Obtiene la precisión como texto descriptivo
     */
    fun getAccuracyDescription(): String {
        return when {
            accuracy <= 5 -> "Excelente (${String.format(Locale.US, "%.1f", accuracy)}m)"
            accuracy <= 10 -> "Muy buena (${String.format(Locale.US, "%.1f", accuracy)}m)"
            accuracy <= 20 -> "Buena (${String. format(Locale. US, "%.1f", accuracy)}m)"
            accuracy <= 50 -> "Moderada (${String. format(Locale. US, "%.1f", accuracy)}m)"
            else -> "Baja (${String. format(Locale. US, "%.1f", accuracy)}m)"
        }
    }

    /**
     * Obtiene las coordenadas formateadas
     */
    fun getFormattedCoordinates(): String {
        val lat = String.format(Locale.US, "%.6f", latitude)
        val lon = String.format(Locale.US, "%.6f", longitude)
        return "Lat: $lat, Lon: $lon"
    }
}