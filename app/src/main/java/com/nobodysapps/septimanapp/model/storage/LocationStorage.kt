package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Location
import javax.inject.Inject

class LocationStorage @Inject constructor(
    private val prefs: SharedPreferences,
    private val jsonConverter: JsonConverter
) {

    fun saveLocations(locations: List<Location>, overallLocation: SeptimanaLocation) {
        val json = jsonConverter.toJson(LocationWrap(locations))
        saveLocations(json, overallLocation)
    }

    fun saveLocations(locationsJson: String, overallLocation: SeptimanaLocation) {
        val key = keyForLocation(overallLocation)
        prefs.edit().putString(key, locationsJson).apply()
    }

    fun loadLocations(overallLocation: SeptimanaLocation): List<Location>? {
        val key = keyForLocation(overallLocation)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson<LocationWrap>(json, LocationWrap::class.java).locations
    }

    private fun keyForLocation(overallLocation: SeptimanaLocation): String {
        return "${LOCATIONS_KEY}_${overallLocation.key}"
    }

    companion object {
        private const val LOCATIONS_KEY = "locations"
    }

    private data class LocationWrap(val locations: List<Location>)
}
