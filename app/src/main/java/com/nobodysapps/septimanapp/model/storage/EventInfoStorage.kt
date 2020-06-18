package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject

class EventInfoStorage @Inject constructor(private val prefs: SharedPreferences) {
    fun saveSeptimanaStartEndTime(start: Calendar, end: Calendar) {
        prefs.edit()
            .putLong(START_TIME_SEPTIMANA_KEY, start.timeInMillis)
            .putLong(END_TIME_SEPTIMANA_KEY, end.timeInMillis)
            .apply()
    }

    fun loadSeptimanaStartEndTime(): Pair<Calendar, Calendar>? {
        val startTime = prefs.getLong(START_TIME_SEPTIMANA_KEY, 0)
        val endTime = prefs.getLong(END_TIME_SEPTIMANA_KEY, 0)
        val today = Calendar.getInstance()
        val start: Calendar = today.clone() as Calendar
        start.timeInMillis = startTime
        val end: Calendar = start.clone() as Calendar
        end.timeInMillis = endTime
        if (today > end) return null
        return Pair(start, end)
    }

    fun saveSeptimanaLocation(location: SeptimanaLocation) {
        prefs.edit().putString(LOCATION_KEY, location.key).apply()
    }

    fun loadSeptimanaLocation(): SeptimanaLocation {
        return SeptimanaLocation.fromKey(prefs.getString(LOCATION_KEY, null) ?: SeptimanaLocation.AMOENEBURG.key)
    }

    companion object {
        private const val START_TIME_SEPTIMANA_KEY = "time_septimana_start"
        private const val END_TIME_SEPTIMANA_KEY = "time_septimana_end"
        private const val LOCATION_KEY = "septimana_location"
    }
}

enum class SeptimanaLocation(val key: String) {
    AMOENEBURG("amoeneburg"),
    BRAUNFELS("braunfels");

    companion object {
        fun fromKey(key: String): SeptimanaLocation {
            val location = fromKeyOrNull(key)
            location?.let {
                return it
            }
            throw IllegalArgumentException("No such id")
        }

        fun fromKeyOrNull(key: String): SeptimanaLocation? {
            for (location in values()) {
                if (location.key == key) return location
            }
            return null
        }
    }
}