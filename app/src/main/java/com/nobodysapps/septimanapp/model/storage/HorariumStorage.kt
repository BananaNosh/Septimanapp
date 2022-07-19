package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Horarium
import javax.inject.Inject

class HorariumStorage @Inject constructor(private val prefs: SharedPreferences, private val jsonConverter: JsonConverter) {

    fun saveHorarium(horarium: Horarium, year: Int, locale: String) {
        val json = jsonConverter.toJson(horarium)
        saveHorarium(json, year, locale)
    }

    fun saveHorarium(horariumJson: String, year: Int, locale: String) {
        val key = keyFromYearAndLocale(year, locale)
        prefs.edit().putString(key, horariumJson).apply()
    }

    fun loadHorarium(year: Int, locale: String): Horarium? {
        val key = keyFromYearAndLocale(year, locale)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson(json, Horarium::class.java)
    }

    private fun keyFromYearAndLocale(year: Int, locale: String): String {
        return "${HORARIUM_KEY}_${year}_$locale"
    }

    companion object {
        private const val HORARIUM_KEY = "horarium"
    }
}
