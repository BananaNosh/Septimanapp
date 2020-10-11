package com.nobodysapps.septimanapp.utils

import java.util.*

class CalendarUtils(private val predefinedCalendar: Calendar?=null) {
    val calendar: Calendar
        get() {
            return when(predefinedCalendar) {
                null -> Calendar.getInstance()
                else -> predefinedCalendar
            }
        }
}