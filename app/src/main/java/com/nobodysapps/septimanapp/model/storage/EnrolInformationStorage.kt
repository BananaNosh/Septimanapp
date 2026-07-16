package com.nobodysapps.septimanapp.model.storage

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.nobodysapps.septimanapp.BuildConfig
import com.nobodysapps.septimanapp.model.EatingHabit
import com.nobodysapps.septimanapp.model.EnrolInformation
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NONE
import com.nobodysapps.septimanapp.model.fromSerializablePair
import com.nobodysapps.septimanapp.model.toSerializablePair
import com.nobodysapps.septimanapp.dependencyInjection.EnrolPrefs
import javax.inject.Inject


class EnrolInformationStorage @Inject constructor(
    @EnrolPrefs private val prefs: SharedPreferences,
    private val jsonConverter: JsonConverter
) {

    fun saveName(name: String) {
        prefs.edit {putString(NAME_KEY, name)}
    }

    fun saveFirstName(firstName: String) {
        prefs.edit {putString(FIRSTNAME_KEY, firstName)}
    }

    fun saveStreet(street: String) {
        prefs.edit {putString(STREET_KEY, street)}
    }

    fun savePostal(postal: String) {
        prefs.edit {putString(POSTAL_KEY, postal)}
    }

    fun saveCity(city: String) {
        prefs.edit {putString(CITY_KEY, city)}
    }

    fun saveCountry(country: String) {
        prefs.edit {putString(COUNTRY_KEY, country)}
    }

    fun savePhone(phone: String) {
        prefs.edit {putString(PHONE_KEY, phone)}
    }

    fun saveMail(mail: String) {
        prefs.edit {putString(MAIL_KEY, mail)}
    }

    fun saveStayInJohanneshaus(stay: Boolean) {
        prefs.edit {putBoolean(JOHANNESHAUS_KEY, stay)}
    }

    fun saveYearsOfLatin(yearsOfLatin: Float) {
        prefs.edit {putFloat(YEARS_LATIN_KEY, yearsOfLatin)}
    }

    fun saveEatingHabit(eatingHabit: EatingHabit) {
        val json = jsonConverter.toJson(eatingHabit.toSerializablePair())
        prefs.edit {putString(EATING_HABIT_KEY, json)}
    }

    fun saveInstrument(instrument: String) {
        prefs.edit {putString(INSTRUMENT_KEY, instrument)}
    }

    fun saveRoomOccupancy(occupancy: Int) {
        prefs.edit {putInt(ROOM_OCCUPANCY_KEY, occupancy)}
    }

    fun saveRoomBathroom(bathroom: Int) {
        prefs.edit {putInt(ROOM_BATHROOM_KEY, bathroom)}
    }

    fun saveRoomRemarks(remarks: String) {
        prefs.edit {putString(ROOM_REMARKS_KEY, remarks)}
    }

    fun saveAge(age: Int) {
        prefs.edit {putInt(AGE_KEY, age)}
    }

    fun saveIsStudent(isStudent: Boolean) {
        prefs.edit {putBoolean(STUDENT_KEY, isStudent)}
    }

    fun saveAddressConsent(consent: Int) {
        when (consent) {
            in 0..2-> {
                prefs.edit {putInt(ADDRESS_CONSENT_KEY, consent)}
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    throw IllegalArgumentException("Illegal address consent")
                }
            }
        }
    }

    fun saveImageConsent(consent: Int) {
        when (consent) {
            in 0..2-> {
                prefs.edit {putInt(IMAGE_CONSENT_KEY, consent)}
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    throw IllegalArgumentException("Illegal address consent")
                }
            }
        }
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
        val stayInJohanneshaus = prefs.getBoolean(JOHANNESHAUS_KEY, true)
        val yearsOfLatin = prefs.getFloat(YEARS_LATIN_KEY, 0f)
        val eatingHabitJson = prefs.getString(EATING_HABIT_KEY, null)
        val eatingHabitPair = jsonConverter.fromJson<Pair<Int, List<String>>?>(
            eatingHabitJson,
            object : TypeToken<Pair<Int, List<String>>>() {}.type
        )
        val instrument = prefs.getString(INSTRUMENT_KEY, null) ?: ""
        val imageConsent = prefs.getInt(IMAGE_CONSENT_KEY, ACCEPT_STATE_NONE)
        val addressConsent = prefs.getInt(ADDRESS_CONSENT_KEY, ACCEPT_STATE_NONE)
        val roomOccupancy = prefs.getInt(ROOM_OCCUPANCY_KEY, 0)
        val roomBathroom = prefs.getInt(ROOM_BATHROOM_KEY, 0)
        val roomRemarks = prefs.getString(ROOM_REMARKS_KEY, null) ?: ""
        val age = prefs.getInt(AGE_KEY, 0)
        val isStudent = prefs.getBoolean(STUDENT_KEY, false)
        return EnrolInformation(
            name,
            firstname,
            street,
            postal,
            city,
            country,
            phone,
            mail,
            stayInJohanneshaus,
            yearsOfLatin,
            if (eatingHabitPair != null) EatingHabit.fromSerializablePair(eatingHabitPair) else null,
            instrument,
            imageConsent,
            addressConsent,
            roomOccupancy,
            roomBathroom,
            roomRemarks,
            age,
            isStudent
        )
    }

    fun saveEnrolState(state: Int) {
        prefs.edit {putInt(ENROLLED_STATE_KEY, state)}
    }

    fun loadEnrolState(): Int {
        val loaded = prefs.getInt(ENROLLED_STATE_KEY, -1)
        return when(loaded) {
            in 1..3 -> loaded
            else -> ENROLLED_STATE_REMIND
        }
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
        private const val JOHANNESHAUS_KEY = "johanneshaus"
        private const val YEARS_LATIN_KEY = "years_latin"
        private const val EATING_HABIT_KEY = "eating_habit"
        private const val INSTRUMENT_KEY = "instrument"
        private const val ADDRESS_CONSENT_KEY = "address_consent"
        private const val IMAGE_CONSENT_KEY = "image_consent"
        private const val ROOM_OCCUPANCY_KEY = "room_occupancy"
        private const val ROOM_BATHROOM_KEY = "room_bathroom"
        private const val ROOM_REMARKS_KEY = "room_remarks"
        private const val AGE_KEY = "age"
        private const val STUDENT_KEY = "is_student"

        private const val ENROLLED_STATE_KEY = "enrolled_state"

        const val ENROLLED_STATE_REMIND = 0
        const val ENROLLED_STATE_ENROLLED = 1
        const val ENROLLED_STATE_IN_PROGRESS = 2
        const val ENROLLED_STATE_NOT_ASK_AGAIN = 3

        /** All keys this storage owns; used by [EnrolPrefsMigration]. */
        internal val ALL_KEYS = setOf(
            NAME_KEY, FIRSTNAME_KEY, STREET_KEY, POSTAL_KEY, CITY_KEY, COUNTRY_KEY,
            PHONE_KEY, MAIL_KEY, JOHANNESHAUS_KEY, YEARS_LATIN_KEY, EATING_HABIT_KEY,
            INSTRUMENT_KEY, ADDRESS_CONSENT_KEY, IMAGE_CONSENT_KEY, ROOM_OCCUPANCY_KEY,
            ROOM_BATHROOM_KEY, ROOM_REMARKS_KEY, AGE_KEY, STUDENT_KEY, ENROLLED_STATE_KEY
        )
    }
}
