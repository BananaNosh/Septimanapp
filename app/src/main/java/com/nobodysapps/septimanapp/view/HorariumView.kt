package com.nobodysapps.septimanapp.view

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.TimePicker
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.WeekViewLoader
import com.nobodysapps.septimanapp.localization.dateFormatSymbolsForLatin
import com.nobodysapps.septimanapp.model.Horarium
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class HorariumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : WeekView(context, attrs),
    TimePickerDialog.OnTimeSetListener {
    private var events: List<WeekViewEvent> = ArrayList(80)

    private val weekViewDefaultDateTimeInterpreter: DateTimeInterpreter
    var displayTimeInEvent = true
        set(value) {
            if (value != field) {
                notifyDatasetChanged()
            }
            field = value
        }

    init {
        // set default start_date limit to today and end_date limit according to max shown days
        setupDefaultFormat()
        setupDateLimits()
        weekViewDefaultDateTimeInterpreter = dateTimeInterpreter
        setupDateTimeInterpreter()
        setupWeekLoader()
        Log.d(TAG, Locale.getAvailableLocales().toString())
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
//        val newEvents = events.toMutableList()
//        newEvents.add(WeekViewEvent("ev${events.size}", "New Event", clickedTime, endTime))
//        events = newEvents
//        notifyDatasetChanged()
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
    private fun setupDateLimits(
        startDate: Calendar = Calendar.getInstance(), endDate: Calendar = Calendar.getInstance()
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
            max(max(NUMBER_OF_SHOWN_DAYS_PORTRAIT, NUMBER_OF_SHOWN_DAYS_LANDSCAPE), startEndDiffDays) - 1
        )
        minDate = startDate
        maxDate = newEndDate
    }

    /**
     * Setup WeekViewLoader to always show the events specified with setHorarium
     */
    private fun setupWeekLoader() {
        weekViewLoader = object : WeekViewLoader {
            override fun toWeekViewPeriodIndex(instance: Calendar): Double {
                return 0.0
            }

            override fun onLoad(periodIndex: Int): List<WeekViewEvent> {
                Log.d(TAG, events.toString())
                events.forEach {
                    it.endTime.add(Calendar.MINUTE, -1)  // to show a border between events
                    if (displayTimeInEvent) {
                        val startTimeString = dateTimeInterpreter
                            .interpretTime(it.startTime.get(Calendar.HOUR_OF_DAY), it.startTime.get(Calendar.MINUTE))
                        it.name = "$startTimeString \n${it.name}"
                    }
                }
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
            hourHeight *= 2
        } else {
            numberOfVisibleDays = NUMBER_OF_SHOWN_DAYS_PORTRAIT
        }
    }

    fun setHorarium(horarium: Horarium) {
        this.events = horarium.events
        setShownHoursAndDateLimitsAccordingToEvents()
        notifyDatasetChanged()
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
                earliestDate = event.startTime.clone() as Calendar?
            }
            if (latestDate == null || event.endTime > latestDate) {
                latestDate = event.endTime.clone() as Calendar?
            }
        }
        if (earliestHour < latestHour) {
            setLimitTime(earliestHour, latestHour)
        }
        val today = Calendar.getInstance()
        setupDateLimits(earliestDate ?: today, latestDate ?: today)
    }


    companion object {

        private const val NUMBER_OF_SHOWN_DAYS_PORTRAIT = 3
        private const val NUMBER_OF_SHOWN_DAYS_LANDSCAPE = 8
        val TAG = "HorariumView"
    }

}
