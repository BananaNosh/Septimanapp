package com.nobodysapps.septimanapp.model

import android.content.Context
import com.nobodysapps.septimanapp.R
import kotlinx.android.synthetic.main.fragment_enrolment.*

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
    val veggieDay: Boolean
) {

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