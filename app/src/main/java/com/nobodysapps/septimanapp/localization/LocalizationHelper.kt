package com.nobodysapps.septimanapp.localization

import android.annotation.SuppressLint
import android.content.Context
import com.nobodysapps.septimanapp.R
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


fun dateFormatSymbolsForLatin(alternativeWeekDays: Boolean = false): DateFormatSymbols {
    val latinDFS = DateFormatSymbols(Locale("la"))
    val days = arrayOf(
        "",
        "dies Solis",
        "dies Lunae",
        "dies Martis",
        "dies Mercurii",
        "dies Iovis",
        "dies Veneris",
        "dies Saturni"
    )
    if (alternativeWeekDays) {
        days[1] = "dies Dominicus"
        days[7] = "dies Sabbati"
    }
    val shortDays = days.map {
        if (it.isEmpty()) {
            ""
        } else {
            it.split(" ")[1].slice(IntRange(0, 2))
        }
    }.toTypedArray()
    latinDFS.weekdays = days
    latinDFS.shortWeekdays = shortDays
    val months = arrayOf(
        "Ianuarius",
        "Februarius",
        "Martius",
        "Aprilis",
        "Maius",
        "Iunius",
        "Iulius",
        "Augustus",
        "September",
        "October",
        "November",
        "December"
    )
    val shortMonths = arrayOf(
        "Ian.",
        "Feb.",
        "Mart.",
        "Apr.",
        "Mai.",
        "Iun.",
        "Iul.",
        "Aug.",
        "Sep.",
        "Oct.",
        "Nov.",
        "Dec."
    )
    latinDFS.months = months
    latinDFS.shortMonths = shortMonths
    return latinDFS
}

fun simpleDateFormat(dateFormat: String, date: Calendar): String {
    val locale = Locale.getDefault()
    return when (locale.language) {
        "la" -> SimpleDateFormat(dateFormat, dateFormatSymbolsForLatin())
        else -> SimpleDateFormat(dateFormat, locale)
    }.format(date.time)
}

/**
 * Gets the displayLanguage string(the name of the language) for the given locale
 * in the default locale's language
 * @param context the context of the application
 * @param locale the locale whose name is returned
 */
fun localizedDisplayLanguage(context: Context?, locale: Locale): String {
    @Suppress("MoveVariableDeclarationIntoWhen")
    val appLocale = Locale.getDefault()
    return when (appLocale) {
        Locale("la") -> {
            if (context == null) return ""
            return displayLanguageInLatin(context, locale)
        }
        else -> locale.displayLanguage
    }
}

@SuppressLint("StringFormatInvalid")
private fun displayLanguageInLatin(context: Context, locale: Locale): String {
    return context.getString(
        R.string.language, context.getString(
            when (locale) {
                Locale("la") -> R.string.language_latin
                Locale.GERMAN -> R.string.language_german
                Locale.FRENCH -> R.string.language_french
                Locale.ENGLISH -> R.string.language_english
                else -> R.string.language_unknown
            }
        )
    )
}