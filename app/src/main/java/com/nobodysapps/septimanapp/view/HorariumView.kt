package com.nobodysapps.septimanapp.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.WeekViewLoader
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.localization.dateFormatSymbolsForLatin
import com.nobodysapps.septimanapp.model.Horarium
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

class HorariumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    WeekView(context, attrs) {
    private var events: List<WeekViewEvent> = ArrayList(80)

    private val weekViewDefaultDateTimeInterpreter: DateTimeInterpreter
    private var numberOfDrawn = 0

    var daysToShowOnToggleDayView = 1
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
        setZoomEndListener { hourHeight ->
            Log.d(TAG, "$hourHeight hour height, text size $textSize")
        }
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
                val eventsWithBreakAfterwardsIndices = ArrayList<Int>()
                var lastEndTime: Calendar? = null
                val copiedEvents = events.mapIndexed { index, ev ->
                    val isSameDay =
                        ev.startTime.get(Calendar.DATE) == lastEndTime?.get(Calendar.DATE)
                    if (lastEndTime != null && isSameDay && ev.startTime != lastEndTime) {
                        eventsWithBreakAfterwardsIndices.add(index - 1)
                    }
                    lastEndTime = ev.endTime
                    ev.clone()
                }
                copiedEvents.forEachIndexed { index, ev ->
                    ev.endTime.add(Calendar.MINUTE, -1)  // to show a border between events

                    val now = Calendar.getInstance()
                    // TODO remove next 2 lines
                    now.set(2019, 6, 30)
                    if (now.after(ev.startTime) && now.before(ev.endTime)) { //is current event
                        ev.color = R.color.colorPrimary //TODO set other color
                    }

                    if (displayTimeInEvent) {
                        val startTimeString =
                            timeStringForTime(ev.startTime) + if (index in eventsWithBreakAfterwardsIndices) "-${timeStringForTime(
                                events[index].endTime
                            )}" else ""
                        val showTimeInSameLine = ev.duration() <= MAX_MINUTES_FOR_EVENT_TITLE_IN_FIRST_ROW
                        ev.name =
                            "$startTimeString ${if (showTimeInSameLine) "" else "\n"}${ev.name}"
                    }
                }
                return copiedEvents
            }

            private fun timeStringForTime(time: Calendar) =
                if (!DateFormat.is24HourFormat(context)) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(time.time)
                } else {
                    dateTimeInterpreter.interpretTime(
                        time.get(Calendar.HOUR_OF_DAY),
                        time.get(Calendar.MINUTE)
                    )
                }
        }
    }

    private fun setupDefaultFormat() {
        todayBackgroundColor = dayBackgroundColor
        maxHourHeight *= 2
    }

    private fun setupDateTimeInterpreter() {
        dateTimeInterpreter = object : DateTimeInterpreter {
            @SuppressLint("SimpleDateFormat")
            override fun interpretDate(date: Calendar): String {
                val locale = Locale.getDefault()
                val dateFormat = "EEEE"
                return when (locale.displayLanguage) {
                    "la" -> SimpleDateFormat(dateFormat, dateFormatSymbolsForLatin())
                    else -> SimpleDateFormat(dateFormat, locale)
                }.format(date.time)
            }

            override fun interpretTime(hour: Int, minutes: Int): String {
                return weekViewDefaultDateTimeInterpreter.interpretTime(hour, minutes)
            }
        }
    }

    /**
     * Set the date limit from startDate to endDate but at least that NUMBER_OF_SHOWN_DAYS days can be shown
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
            endDate.get(Calendar.DAY_OF_YEAR) - startDate.get(Calendar.DAY_OF_YEAR) // !!! does not work for change of year
        val newEndDate = startDate.clone() as Calendar
        newEndDate.add(
            Calendar.DAY_OF_YEAR,
            max(
                max(NUMBER_OF_SHOWN_DAYS_PORTRAIT, NUMBER_OF_SHOWN_DAYS_LANDSCAPE),
                startEndDiffDays
            ) - 1
        )
        minDate = startDate
        maxDate = newEndDate
    }

    /**
     * Tell the view to adapt according to orientation
     * Shows more days, when in landscape
     * @param isLandscape if the orientation is landscape
     */
    fun changeOrientation(isLandscape: Boolean) {
        if (isLandscape) {
            numberOfVisibleDays = NUMBER_OF_SHOWN_DAYS_LANDSCAPE
            daysToShowOnToggleDayView = NUMBER_OF_SHOWN_DAYS_LANDSCAPE_ZOOMED
        } else {
            numberOfVisibleDays = NUMBER_OF_SHOWN_DAYS_PORTRAIT
            daysToShowOnToggleDayView = 1
        }
    }

    fun setHorarium(horarium: Horarium) {
        this.events = horarium.events
        goToToday()

        setShownHoursAndDateLimitsAccordingToEvents()

        notifyDatasetChanged()
    }

    fun hasHorarium() : Boolean {
        return events.isNotEmpty()
    }

    private fun setHourHeightAccordingToShortestEvent(events: List<WeekViewEvent>) {
        if (events.isEmpty()) return
        Log.d(TAG, "$hourHeight hour height, text size $textSize")
        val shortestEvent = events.sortedBy { it.duration() }[0]
        val shortestDurationInHours = shortestEvent.duration() / 60f
        hourHeight = (textSize * 2 / shortestDurationInHours).toInt()
    }

    /**
     * Limit shown hours to the earliest and latest occurring in events
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
        if (earliestDate != null) {
            goToDate(earliestDate)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when (numberOfDrawn) {
            0 -> // workaround as in WeekView in first draw the hourHeight is set
                setHourHeightAccordingToShortestEvent(events)
            1 -> // also set hour after the second draw
                goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toDouble())
        }
        numberOfDrawn++
    }

    fun toggleDayView() {
        val newToggleDays = numberOfVisibleDays
        numberOfVisibleDays = daysToShowOnToggleDayView
        daysToShowOnToggleDayView = newToggleDays
    }


    companion object {

        const val NUMBER_OF_SHOWN_DAYS_PORTRAIT = 3
        const val NUMBER_OF_SHOWN_DAYS_LANDSCAPE = 8
        const val NUMBER_OF_SHOWN_DAYS_LANDSCAPE_ZOOMED = 4
        const val TAG = "HorariumView"
        private const val MAX_MINUTES_FOR_EVENT_TITLE_IN_FIRST_ROW = 45
    }

}

private fun WeekViewEvent.clone() =
    WeekViewEvent(
        identifier,
        name,
        location,
        startTime.clone() as Calendar,
        endTime.clone() as Calendar,
        isAllDay
    )

private fun WeekViewEvent.duration(): Int =
    ((endTime.timeInMillis - startTime.timeInMillis) / 1000 / 60).toInt()