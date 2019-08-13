package com.nobodysapps.septimanapp.localization

import android.util.Log
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