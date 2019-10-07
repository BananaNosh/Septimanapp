package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.model.EnrolInformation
import javax.inject.Inject


class EnrolInformationStorage @Inject constructor(private val prefs: SharedPreferences, private val jsonConverter: JsonConverter) {

    fun saveName(name: String) {
        prefs.edit().putString(NAME_KEY, name).apply()
    }

    fun saveFirstame(firstname: String) {
        prefs.edit().putString(FIRSTNAME_KEY, firstname).apply()
    }

    fun loadEnrolInformation() : EnrolInformation {
        val name = prefs.getString(NAME_KEY, null) ?: ""
        val firstname = prefs.getString(FIRSTNAME_KEY, null) ?: ""
        return EnrolInformation(name, firstname)
    }
//
//    private fun keyForLocation(overallLocation: String): String {
//        return "${LOCATIONS_KEY}_${overallLocation}"
//    }

    companion object {
        private const val NAME_KEY = "name"
        private const val FIRSTNAME_KEY = "firstname"
    }
}
