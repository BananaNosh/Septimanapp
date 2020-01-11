package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.nobodysapps.septimanapp.model.EatingHabit
import com.nobodysapps.septimanapp.model.EnrolInformation
import com.nobodysapps.septimanapp.model.fromSerializablePair
import com.nobodysapps.septimanapp.model.toSerializablePair
import javax.inject.Inject


class EnrolInformationStorage @Inject constructor(
    private val prefs: SharedPreferences,
    private val jsonConverter: JsonConverter
) {

    fun saveName(name: String) {
        prefs.edit().putString(NAME_KEY, name).apply()
    }

    fun saveFirstame(firstname: String) {
        prefs.edit().putString(FIRSTNAME_KEY, firstname).apply()
    }

    fun saveStreet(street: String) {
        prefs.edit().putString(STREET_KEY, street).apply()
    }

    fun savePostal(postal: String) {
        prefs.edit().putString(POSTAL_KEY, postal).apply()
    }

    fun saveCity(city: String) {
        prefs.edit().putString(CITY_KEY, city).apply()
    }

    fun saveCountry(country: String) {
        prefs.edit().putString(COUNTRY_KEY, country).apply()
    }

    fun savePhone(phone: String) {
        prefs.edit().putString(PHONE_KEY, phone).apply()
    }

    fun saveMail(mail: String) {
        prefs.edit().putString(MAIL_KEY, mail).apply()
    }

    fun saveYearsOfLatin(yearsOfLatin: Float) {
        prefs.edit().putFloat(YEARS_LATIN_KEY, yearsOfLatin).apply()
    }

    fun saveInstrument(instrument: String) {

    }

    fun saveEatingHabit(eatingHabit: EatingHabit) {
        val json = jsonConverter.toJson(eatingHabit.toSerializablePair())
        prefs.edit().putString(EATING_HABIT_KEY, json).apply()
    }

    fun loadEnrolInformation(): EnrolInformation {
        val name = prefs.getString(NAME_KEY, null) ?: ""
        val firstname = prefs.getString(FIRSTNAME_KEY, null) ?: ""
        val street = prefs.getString(STREET_KEY, null) ?: ""
        val postal = prefs.getString(POSTAL_KEY, null) ?: ""
        val city = prefs.getString(CITY_KEY, null) ?: ""
        val country = prefs.getString(COUNTRY_KEY, null) ?: ""
        val phone = prefs.getString(PHONE_KEY, null) ?: ""
        val mail = prefs.getString(MAIL_KEY, null) ?: ""
        val yearsOfLatin = prefs.getFloat(YEARS_LATIN_KEY, 0f)
        val eatingHabitJson = prefs.getString(EATING_HABIT_KEY, null)
        val eatingHabitPair = jsonConverter.fromJson<Pair<Int, List<String>>?>(
            eatingHabitJson,
            object : TypeToken<Pair<Int, List<String>>>() {}.type
        )
        return EnrolInformation(
            name,
            firstname,
            street,
            postal,
            city,
            country,
            phone,
            mail,
            yearsOfLatin,
            if (eatingHabitPair != null) EatingHabit.fromSerializablePair(eatingHabitPair) else null
        )
    }
//
//    private fun keyForLocation(overallLocation: String): String {
//        return "${LOCATIONS_KEY}_${overallLocation}"
//    }

    companion object {
        private const val NAME_KEY = "name"
        private const val FIRSTNAME_KEY = "firstname"
        private const val STREET_KEY = "street"
        private const val POSTAL_KEY = "postal"
        private const val CITY_KEY = "city"
        private const val COUNTRY_KEY = "country"
        private const val PHONE_KEY = "phone"
        private const val MAIL_KEY = "mail"
        private const val YEARS_LATIN_KEY = "years_latin"
        private const val EATING_HABIT_KEY = "eating_habit"
    }
}
