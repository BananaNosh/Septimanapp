package com.nobodysapps.septimanapp.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.WeekViewLoader
import com.nobodysapps.septimanapp.localization.dateFormatSymbolsForLatin
import java.text.SimpleDateFormat
import java.util.*

class HorariumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WeekView(context, attrs) {

    private var events: List<WeekViewEvent> = ArrayList(80)
    private val weekViewDefaultDateTimeInterpreter: DateTimeInterpreter

    init {
        // set default start_date limit to today and end_date limit according to max shown days
        setupDefaultFormat()
        setupDateLimits()
        weekViewDefaultDateTimeInterpreter = dateTimeInterpreter
        setupDateTimeInterpreter()
        setupWeekLoader()
        Log.d(TAG, Locale.getAvailableLocales().toString())
    }

    private fun setupDefaultFormat() {
        todayBackgroundColor = dayBackgroundColor
    }

    private fun setupDateTimeInterpreter() {
        dateTimeInterpreter = object : DateTimeInterpreter {
            override fun interpretDate(date: Calendar): String {
                return SimpleDateFormat("EEEE", dateFormatSymbolsForLatin()).format(date.time)
            }

            override fun interpretTime(hour: Int, minutes: Int): String {
                return weekViewDefaultDateTimeInterpreter.interpretTime(hour, minutes)
            }
        }
    }

    /**
     * Set the date limit from startDate to endDate but at least that NUMBER_OF_SHOWN_DAYS days can be shown TODO change according to events
     * @param startDate the start date of the view
     * @param endDate the end date of the view
     */
    private fun setupDateLimits(startDate: Calendar = Calendar.getInstance(), endDate: Calendar = Calendar.getInstance()
    ) {
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)
        startDate.set(Calendar.MILLISECOND, 0)
        val startEndDiffDays =
            endDate.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR) // TODO does not work for change of year
        val newEndDate = startDate.clone() as Calendar
        newEndDate.add(
            Calendar.DAY_OF_YEAR,
            Math.max(Math.max(NUMBER_OF_SHOWN_DAYS_PORTRAIT, NUMBER_OF_SHOWN_DAYS_LANDSCAPE), startEndDiffDays) - 1
        )
        minDate = startDate
        maxDate = newEndDate
    }

    /**
     * Setup WeekViewLoader to always show the events specified with setEvents
     */
    private fun setupWeekLoader() {
        weekViewLoader = object : WeekViewLoader {
            override fun toWeekViewPeriodIndex(instance: Calendar): Double {
                return 0.0
            }

            override fun onLoad(periodIndex: Int): List<WeekViewEvent> {
                Log.d(TAG, events.toString())
                return events
            }
        }
    }

    /**
     * Tell the view to adapt according to orientation
     * Shows more days, when in landscape
     * @param isLandscape if the orienation is landscape
     */
    fun changeOrientation(isLandscape: Boolean) {
        if (isLandscape) {
            numberOfVisibleDays = NUMBER_OF_SHOWN_DAYS_LANDSCAPE
        } else {
            numberOfVisibleDays = NUMBER_OF_SHOWN_DAYS_PORTRAIT
        }
    }

    fun setEvents(events: List<WeekViewEvent>) {
        this.events = events
        setShownHoursAndDateLimitsAccordingToEvents()
    }

    /**
     * Limit shown hours to the earliest and latest occuring in events
     */
    private fun setShownHoursAndDateLimitsAccordingToEvents() {
        var earliestDate: Calendar? = null
        var latestDate: Calendar? = null
        var earliestHour = 23
        var latestHour = 0
        for (event in events) {
            val eventStartHour = event.startTime.get(Calendar.HOUR_OF_DAY)
            val eventEndHour = event.endTime.get(Calendar.HOUR_OF_DAY) + 1
            if (eventStartHour < earliestHour) {
                earliestHour = eventStartHour
            }
            if (eventEndHour > latestHour) {
                latestHour = eventEndHour
            }
            if (earliestDate == null || event.startTime < earliestDate) {
                earliestDate = event.startTime
            }
            if (latestDate == null || event.endTime > latestDate) {
                latestDate = event.endTime
            }
        }
        setLimitTime(earliestHour, latestHour)
        val today = Calendar.getInstance()
        setupDateLimits(earliestDate ?: today, latestDate ?: today)
    }

    companion object {

        private const val NUMBER_OF_SHOWN_DAYS_PORTRAIT = 3
        private const val NUMBER_OF_SHOWN_DAYS_LANDSCAPE = 8
        val TAG = "WeekView"
    }

}
