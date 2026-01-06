package escom.ipn.m.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationData(
    val id: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude:  Double,
    val accuracy: Float,          // Precisión en metros
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "gps"  // Proveedor (gps, network, fused)
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
            val sdf = SimpleDateFormat("HH: mm:ss", Locale.getDefault())
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
                accuracy <= 5 -> "Excelente (${"%.1f".format(accuracy)}m)"
                accuracy <= 10 -> "Muy buena (${"%. 1f".format(accuracy)}m)"
                accuracy <= 20 -> "Buena (${"%.1f".format(accuracy)}m)"
                accuracy <= 50 -> "Moderada (${"%.1f".format(accuracy)}m)"
                else -> "Baja (${"%.1f".format(accuracy)}m)"
            }
        }

        /**
         * Obtiene las coordenadas formateadas
         */
        fun getFormattedCoordinates(): String {
            return "Lat:  ${"%.6f".format(latitude)}, Lon: ${"%.6f".format(longitude)}"
        }
}