package com.nobodysapps.septimanapp.localization

import android.content.Context
import android.util.Log
import com.nobodysapps.septimanapp.R
import java.text.DateFormatSymbols
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
    // TODO add months (days)
    Log.d("LatinDateFormatSymbols", latinDFS.shortWeekdays.toString())
    return latinDFS
}

/**
 * Gets the displayLanguage string(the name of the language) for the given locale
 * in the default locale's language
 * @param context the context of the application
 * @param locale the locale whose name is returned
 */
fun localizedDisplayLanguage(context: Context?, locale: Locale): String {
    val appLocale = Locale.getDefault()
    return when (appLocale) {
        Locale("la") -> {
            if (context == null) return ""
            return displayLanguageInLatin(context, locale)
        }
        else -> locale.displayLanguage
    }
}

private fun displayLanguageInLatin(context: Context, locale: Locale): String {
    return context.getString(
        R.string.language, when (locale) {
            Locale("la") -> "Latina"
            Locale.GERMAN -> "Theodisca"
            Locale.FRENCH -> "Francogallica"
            Locale.ENGLISH -> "Anglica"
            else -> "lingua ignota"
        }
    )
}