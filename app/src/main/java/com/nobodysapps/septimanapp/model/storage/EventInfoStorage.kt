package com.nobodysapps.septimanapp.model.storage

import androidx.core.content.edit
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonSyntaxException
import com.nobodysapps.septimanapp.model.EventInfo
import java.lang.IllegalArgumentException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EventInfoStorage @Inject constructor(
    private val prefs: SharedPreferences,
    private val jsonConverter: JsonConverter
) {

    /**
     * Parses assets/event_info.json content and stores it under the same keys as the
     * individual save methods. All-or-nothing: on malformed JSON, unparseable dates or an
     * unknown location key nothing is written, so previously stored values stay intact.
     */
    fun saveEventInfoFromJson(json: String) {
        val eventInfo = try {
            jsonConverter.fromJson<EventInfo?>(json, EventInfo::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
        val location = eventInfo?.location?.let { SeptimanaLocation.fromKeyOrNull(it) }
        val start = parseDateTime(eventInfo?.start)
        val end = parseDateTime(eventInfo?.end)
        if (location == null || start == null || end == null) {
            Log.w(TAG, "Invalid event info json, keeping stored values")
            return
        }
        saveSeptimanaStartEndTime(start, end)
        saveSeptimanaLocation(location)
    }

    private fun parseDateTime(dateTime: String?): Calendar? {
        if (dateTime == null) return null
        val format = SimpleDateFormat(EVENT_DATE_FORMAT, Locale.US).apply { isLenient = false }
        val date = try {
            format.parse(dateTime)
        } catch (e: ParseException) {
            null
        } ?: return null
        return Calendar.getInstance().apply { time = date }
    }

    fun saveSeptimanaStartEndTime(start: Calendar, end: Calendar) {
        prefs.edit {
            putLong(START_TIME_SEPTIMANA_KEY, start.timeInMillis)
            putLong(END_TIME_SEPTIMANA_KEY, end.timeInMillis)
        }
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
        prefs.edit {putString(LOCATION_KEY, location.key)}
    }

    fun loadSeptimanaLocation(): SeptimanaLocation {
        return SeptimanaLocation.fromKey(prefs.getString(LOCATION_KEY, null) ?: SeptimanaLocation.AMOENEBURG.key)
    }

    companion object {
        private const val TAG = "EventInfoStorage"
        const val EVENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm"

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
