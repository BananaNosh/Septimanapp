package com.nobodysapps.septimanapp.model

import android.content.Context
import com.nobodysapps.septimanapp.R

data class EnrolInformation(
    val name: String,
    val firstname: String,
    val street: String,
    val postal: String,
    val city: String,
    val country: String,
    val phone: String,
    val mail: String,
    val stayInMainBuilding: Boolean,
    val yearsOfLatin: Float,
    val eatingHabit: EatingHabit?,
    val instrument: String,
    val imageConsent: Int,
    val addressConsent: Int,
    val roomOccupancy: Int = 0,   // 0 = keine Angabe; index into the room-occupancy arrays
    val roomBathroom: Int = 0,    // 0 = keine Angabe; index into the room-bathroom arrays
    val roomRemarks: String = "",
    val age: Int = 0,
    val isStudent: Boolean = false
) {

    /**
     * The required fields that are not filled in yet. Single source of truth for
     * [isValid] and for the "missing fields" snackbar in the enrolment form.
     */
    fun missingFields(): List<RequiredField> {
        val missing = mutableListOf<RequiredField>()
        if (name.isBlank()) missing.add(RequiredField.NAME)
        if (firstname.isBlank()) missing.add(RequiredField.FIRSTNAME)
        if (street.isBlank()) missing.add(RequiredField.STREET)
        if (postal.isBlank()) missing.add(RequiredField.POSTAL)
        if (city.isBlank()) missing.add(RequiredField.CITY)
        if (country.isBlank()) missing.add(RequiredField.COUNTRY)
        if (phone.isBlank()) missing.add(RequiredField.PHONE)
        if (mail.isBlank()) missing.add(RequiredField.MAIL)
        if (age <= 0) missing.add(RequiredField.AGE)
        if (addressConsent == ACCEPT_STATE_NONE) missing.add(RequiredField.ADDRESS_CONSENT)
        if (imageConsent == ACCEPT_STATE_NONE) missing.add(RequiredField.IMAGE_CONSENT)
        return missing
    }

    fun isValid(): Boolean = missingFields().isEmpty()

    enum class RequiredField(val labelRes: Int) {
        NAME(R.string.enrol_last_name_hint),
        FIRSTNAME(R.string.enrol_first_name_hint),
        STREET(R.string.enrol_street_hint),
        POSTAL(R.string.enrol_postal_code_hint),
        CITY(R.string.enrol_city_hint),
        COUNTRY(R.string.enrol_country_hint),
        PHONE(R.string.enrol_phone_hint),
        MAIL(R.string.enrol_mail_hint),
        AGE(R.string.enrol_age_label),
        ADDRESS_CONSENT(R.string.enrol_field_address_consent),
        IMAGE_CONSENT(R.string.enrol_field_image_consent)
    }

    companion object {
        const val ACCEPT_STATE_NONE = 0
        const val ACCEPT_STATE_YES = 1
        const val ACCEPT_STATE_NO = 2
    }
}

open class EatingHabit(val allergens: List<String>) {
    fun information(context: Context): String {
        val allergensString =
            if (allergens.isNotEmpty()) "${context.getString(R.string.eating_habit_allergens)}: ${allergens.joinToString { it }}" else ""
        val additionalInformation = additionalInformation(context)
        return if (additionalInformation.isNotBlank()) {
            "${if (allergensString.isNotBlank()) "$allergensString - " else ""}$additionalInformation"
        } else {
            allergensString
        }
    }

    protected open fun additionalInformation(context: Context) = ""

    open val serializationCode = 0

    companion object
}

open class Vegetarian(allergens: List<String>) : EatingHabit(allergens) {
    override fun additionalInformation(context: Context): String =
        context.getString(R.string.eating_habit_vegetarian)

    override val serializationCode = 1
}

class Vegan(allergens: List<String>) : Vegetarian(allergens) {
    override fun additionalInformation(context: Context): String =
        context.getString(R.string.eating_habit_vegan)

    override val serializationCode = 2
}


fun EatingHabit.toSerializablePair() = Pair(serializationCode, allergens)

fun EatingHabit.Companion.fromSerializablePair(pair: Pair<Int, List<String>>) = when (pair.first) {
    1 -> Vegetarian(pair.second)
    2 -> Vegan(pair.second)
    else -> EatingHabit(pair.second)
}

fun EatingHabit.Companion.create(isVegan: Boolean, isVegetarian: Boolean, allergens: List<String>) =
    when {
        isVegan -> Vegan(allergens)
        isVegetarian -> Vegetarian(allergens)
        else -> EatingHabit(
            allergens
        )
    }