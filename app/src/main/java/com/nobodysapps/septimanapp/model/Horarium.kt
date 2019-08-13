package com.nobodysapps.septimanapp.model

import com.alamkanak.weekview.WeekViewEvent
import java.util.*

data class Horarium(val events: List<WeekViewEvent>, val language: Locale)
