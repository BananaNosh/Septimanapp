package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Horarium

import java.util.Calendar
import javax.inject.Inject

class HorariumStorage @Inject constructor(private val prefs: SharedPreferences, private val jsonConverter: JsonConverter) {

    fun saveHorarium(horarium: Horarium, year: Int, locale: String) {
        val key = keyFromYearAndLocale(year, locale)
        val json = jsonConverter.toJson(horarium)
        prefs.edit().putString(key, json).apply()
    }

    fun loadHorarium(year: Int, locale: String): Horarium? {
        val key = keyFromYearAndLocale(year, locale)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson(json, Horarium::class.java)
    }

    companion object {
        private const val HORARIUM_KEY = "horarium"

        fun keyFromYearAndLocale(year: Int, locale: String): String {
            return "${HORARIUM_KEY}_${year}_$locale"
        }
    }
}
