package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Location
import javax.inject.Inject

class LocationStorage @Inject constructor(private val prefs: SharedPreferences, private val jsonConverter: JsonConverter) {

    fun saveLocations(locations: List<Location>, overallLocation: String = "") {
        val key = keyForLocation(overallLocation)
        val json = jsonConverter.toJson(LocationWrap(locations))
        prefs.edit().putString(key, json).apply()
    }

    fun loadLocations(overallLocation: String=""): List<Location>? {
        val key = keyForLocation(overallLocation)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson(json, LocationWrap::class.java).locations
    }

    companion object {
        private const val LOCATIONS_KEY = "locations"

        fun keyForLocation(overallLocation: String): String {
            return "${LOCATIONS_KEY}_${overallLocation}"
        }
    }

    private data class LocationWrap(val locations: List<Location>)
}
