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
        prefs.edit().putInt(LOCATION_KEY, location.id).apply()
    }

    fun loadSeptimanaLocation(): SeptimanaLocation {
        return SeptimanaLocation.fromId(prefs.getInt(LOCATION_KEY, 0))
    }

    companion object {
        private const val START_TIME_SEPTIMANA_KEY = "time_septimana_start"
        private const val END_TIME_SEPTIMANA_KEY = "time_septimana_end"
        private const val LOCATION_KEY = "septimana_location"
    }
}

enum class SeptimanaLocation(val id: Int) {
    AMOENEBURG(0),
    BRAUNFELS(1);

    companion object {
        fun fromId(id: Int): SeptimanaLocation {
            for (location in values()) {
                if (location.id == id) return location
            }
            throw IllegalArgumentException("No such id")
        }
    }
}