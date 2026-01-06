package escom.ipn.m.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import escom.ipn.m.data.model.LocationData
import java.io.File

/**
 * Clase para gestionar el almacenamiento de ubicaciones en JSON
 */
class LocationStorage(private val context: Context) {

    private val gson = Gson()
    private val fileName = "location_history.json"

    /**
     * Obtiene el archivo de almacenamiento
     */
    private fun getFile(): File {
        return File(context. filesDir, fileName)
    }

    /**
     * Guarda una nueva ubicación en el historial
     */
    fun saveLocation(location: LocationData) {
        val locations = getAllLocations().toMutableList()
        locations.add(location)
        saveAllLocations(locations)
    }

    /**
     * Guarda múltiples ubicaciones
     */
    fun saveLocations(newLocations: List<LocationData>) {
        val locations = getAllLocations().toMutableList()
        locations.addAll(newLocations)
        saveAllLocations(locations)
    }

    /**
     * Obtiene todas las ubicaciones guardadas
     */
    fun getAllLocations(): List<LocationData> {
        val file = getFile()

        if (!file. exists()) {
            return emptyList()
        }

        return try {
            val json = file.readText()
            if (json.isBlank()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<LocationData>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene las ubicaciones ordenadas por fecha (más recientes primero)
     */
    fun getLocationsSortedByDate(ascending: Boolean = false): List<LocationData> {
        val locations = getAllLocations()
        return if (ascending) {
            locations.sortedBy { it.timestamp }
        } else {
            locations. sortedByDescending { it. timestamp }
        }
    }

    /**
     * Obtiene las últimas N ubicaciones
     */
    fun getLastLocations(count: Int): List<LocationData> {
        return getLocationsSortedByDate().take(count)
    }

    /**
     * Obtiene la última ubicación registrada
     */
    fun getLastLocation(): LocationData? {
        return getLocationsSortedByDate().firstOrNull()
    }

    /**
     * Obtiene ubicaciones dentro de un rango de fechas
     */
    fun getLocationsByDateRange(startTime: Long, endTime: Long): List<LocationData> {
        return getAllLocations().filter {
            it.timestamp in startTime..endTime
        }. sortedByDescending { it. timestamp }
    }

    /**
     * Elimina una ubicación por ID
     */
    fun deleteLocation(id: Long) {
        val locations = getAllLocations().toMutableList()
        locations.removeAll { it.id == id }
        saveAllLocations(locations)
    }

    /**
     * Limpia todo el historial
     */
    fun clearAllLocations() {
        val file = getFile()
        if (file.exists()) {
            file. writeText("[]")
        }
    }

    /**
     * Obtiene el número total de ubicaciones
     */
    fun getLocationsCount(): Int {
        return getAllLocations().size
    }

    /**
     * Guarda toda la lista de ubicaciones (uso interno)
     */
    private fun saveAllLocations(locations: List<LocationData>) {
        try {
            val json = gson.toJson(locations)
            getFile().writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exporta el historial como String JSON
     */
    fun exportAsJson(): String {
        return gson.toJson(getAllLocations())
    }

    /**
     * Verifica si hay ubicaciones guardadas
     */
    fun hasLocations(): Boolean {
        return getAllLocations().isNotEmpty()
    }
}