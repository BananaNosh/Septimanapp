package com.nobodysapps.septimanapp.model.storage

import androidx.core.content.edit
import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.Proposita
import javax.inject.Inject

class PropositaStorage @Inject constructor(
    private val prefs: SharedPreferences,
    private val jsonConverter: JsonConverter
) {

    fun saveProposita(proposita: Proposita, year: Int) {
        val json = jsonConverter.toJson(proposita)
        saveProposita(json, year)
    }

    fun saveProposita(propositaJson: String, year: Int) {
        val key = keyFromYear(year)
        prefs.edit { putString(key, propositaJson) }
    }

    fun loadProposita(year: Int): Proposita? {
        val key = keyFromYear(year)
        val json = prefs.getString(key, null) ?: return null
        return jsonConverter.fromJson(json, Proposita::class.java)
    }

    private fun keyFromYear(year: Int): String {
        return "${PROPOSITA_KEY}_$year"
    }

    companion object {
        private const val PROPOSITA_KEY = "proposita"
    }
}
