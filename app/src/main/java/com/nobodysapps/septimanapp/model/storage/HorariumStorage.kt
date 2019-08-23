package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Horarium

import java.util.Calendar
import javax.inject.Inject

class HorariumStorage @Inject constructor(private val prefs: SharedPreferences, private val jsonConverter: JsonConverter) {

    fun saveHorarium(horarium: Horarium, startDate: Calendar) {
        val key = keyFromStartDate(startDate)
        val json = jsonConverter.toJson(horarium)
        prefs.edit().putString(key, json).apply()
    }

    fun loadHorarium(startDate: Calendar): Horarium? {
        val key = keyFromStartDate(startDate)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson(json, Horarium::class.java)
    }

    companion object {
        private const val HORARIUM_KEY = "horarium"

        fun keyFromStartDate(startDate: Calendar): String {
            return String.format("%s_%s", HORARIUM_KEY, startDate.get(Calendar.YEAR))
        }
    }
}
