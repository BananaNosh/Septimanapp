package com.nobodysapps.septimanapp

import com.alamkanak.weekview.WeekViewEvent
import com.nobodysapps.septimanapp.export.HorariumIcsExporter
import com.nobodysapps.septimanapp.model.Horarium
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Tests the iCalendar document produced for the horarium export.
 */
@RunWith(RobolectricTestRunner::class)
class HorariumIcsExporterTest {

    private lateinit var exporter: HorariumIcsExporter

    @Before
    fun setup() {
        exporter = HorariumIcsExporter()
    }

    @Test
    fun envelopeAndEventCount() {
        val ics = export(horariumOf(event("ev_0"), event("ev_1")))

        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"))
        assertTrue(ics.contains("VERSION:2.0\r\n"))
        assertTrue(ics.contains("X-WR-CALNAME:Septimana Latina 2026\r\n"))
        assertEquals(2, occurrences(ics, "BEGIN:VEVENT"))
        assertEquals(2, occurrences(ics, "END:VEVENT"))
    }

    @Test
    fun timedEventUsesFloatingLocalTimes() {
        val ics = export(
            horariumOf(
                event("ev_0", start = calendar(2026, Calendar.AUGUST, 1, 16, 0), durationMinutes = 120)
            )
        )

        assertTrue(ics.contains("DTSTART:20260801T160000\r\n"))
        assertTrue(ics.contains("DTEND:20260801T180000\r\n"))
    }

    @Test
    fun allDayEventUsesDateValuesWithExclusiveEnd() {
        val ics = export(
            horariumOf(
                event(
                    "ev_0",
                    start = calendar(2026, Calendar.AUGUST, 1, 0, 0),
                    durationMinutes = 0,
                    allDay = true
                )
            )
        )

        assertTrue(ics.contains("DTSTART;VALUE=DATE:20260801\r\n"))
        assertTrue(ics.contains("DTEND;VALUE=DATE:20260802\r\n"))
    }

    @Test
    fun dtstampIsInUtc() {
        val now = calendar(2026, Calendar.JULY, 15, 12, 30)
        val ics = exporter.export(horariumOf(event("ev_0")), CALENDAR_NAME, UID_PREFIX, now = now)

        val expected = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(now.time)
        assertTrue(ics.contains("DTSTAMP:$expected\r\n"))
    }

    @Test
    fun summaryIsWhitespaceNormalizedAndEscaped() {
        val ics = export(horariumOf(event("ev_0", name = "cena\n\n"), event("ev_1", name = "a, b; c\\d")))

        assertTrue(ics.contains("SUMMARY:cena\r\n"))
        assertTrue(ics.contains("SUMMARY:a\\, b\\; c\\\\d\r\n"))
    }

    @Test
    fun uidContainsPrefixAndIdentifierAndIsStable() {
        val horarium = horariumOf(event("ev_3"))

        val first = export(horarium)
        val second = export(horarium)

        assertTrue(first.contains("UID:horarium_2026_la-ev_3@septimanapp.nobodysapps.com\r\n"))
        assertEquals(uidLine(first), uidLine(second))
    }

    @Test
    fun locationIsWrittenWhenGiven() {
        val withLocation = exporter.export(
            horariumOf(event("ev_0")), CALENDAR_NAME, UID_PREFIX, location = "Braunfels"
        )
        val withoutLocation = export(horariumOf(event("ev_0")))

        assertTrue(withLocation.contains("LOCATION:Braunfels\r\n"))
        assertFalse(withoutLocation.contains("LOCATION:"))
    }

    @Test
    fun remindersAreAddedOnlyWhenRequested() {
        val withReminders = exportWithReminders(horariumOf(event("ev_0"), event("ev_1")))
        val withoutReminders = export(horariumOf(event("ev_0"), event("ev_1")))

        assertEquals(2, occurrences(withReminders, "BEGIN:VALARM"))
        assertTrue(withReminders.contains("ACTION:DISPLAY\r\n"))
        assertFalse(withoutReminders.contains("VALARM"))
        assertFalse(withoutReminders.contains("TRIGGER"))
    }

    @Test
    fun eventFollowingAnotherOneIsRemindedAtItsStart() {
        val cena = event("ev_0", start = calendar(2026, Calendar.AUGUST, 1, 18, 0), durationMinutes = 75)
        val colloquia =
            event("ev_1", start = calendar(2026, Calendar.AUGUST, 1, 19, 15), durationMinutes = 45)

        val ics = exportWithReminders(horariumOf(cena, colloquia))

        assertEquals(listOf(TRIGGER_BEFORE, TRIGGER_AT_START), triggers(ics))
    }

    @Test
    fun eventAfterABreakIsRemindedFiveMinutesEarly() {
        val first = event("ev_0", start = calendar(2026, Calendar.AUGUST, 1, 18, 0), durationMinutes = 60)
        val afterBreak =
            event("ev_1", start = calendar(2026, Calendar.AUGUST, 1, 19, 30), durationMinutes = 30)

        val ics = exportWithReminders(horariumOf(first, afterBreak))

        assertEquals(listOf(TRIGGER_BEFORE, TRIGGER_BEFORE), triggers(ics))
    }

    @Test
    fun parallelEventDoesNotCountAsPredecessor() {
        val start = calendar(2026, Calendar.AUGUST, 1, 10, 30)
        val ics = exportWithReminders(
            horariumOf(
                event("ev_0", start = start, durationMinutes = 45),
                event("ev_1", start = start, durationMinutes = 60)
            )
        )

        assertEquals(listOf(TRIGGER_BEFORE, TRIGGER_BEFORE), triggers(ics))
    }

    @Test
    fun allDayEventIsRemindedAtItsStartAndIsNoPredecessor() {
        val allDay = event(
            "ev_0",
            start = calendar(2026, Calendar.AUGUST, 1, 0, 0),
            durationMinutes = 0,
            allDay = true
        )
        val duringTheDay =
            event("ev_1", start = calendar(2026, Calendar.AUGUST, 1, 18, 0), durationMinutes = 60)

        val ics = exportWithReminders(horariumOf(allDay, duringTheDay))

        assertEquals(listOf(TRIGGER_AT_START, TRIGGER_BEFORE), triggers(ics))
    }

    @Test
    fun longLinesAreFolded() {
        val longName = "colloquium de rebus Latinis atque de linguae Latinae usu cotidiano in Septimana"
        val ics = export(horariumOf(event("ev_0", name = longName)))

        val lines = ics.split("\r\n").filter { it.isNotEmpty() }
        assertTrue(lines.all { it.toByteArray(Charsets.UTF_8).size <= 75 })
        val summaryIndex = lines.indexOfFirst { it.startsWith("SUMMARY:") }
        assertTrue(lines[summaryIndex + 1].startsWith(" "))
        val unfolded = lines[summaryIndex] + lines[summaryIndex + 1].removePrefix(" ")
        assertEquals("SUMMARY:$longName", unfolded)
    }

    @Test
    fun horariumWithoutEventsGivesEmptyCalendar() {
        val ics = export(Horarium(emptyList()))

        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"))
        assertFalse(ics.contains("BEGIN:VEVENT"))
    }

    private fun export(horarium: Horarium) =
        exporter.export(horarium, CALENDAR_NAME, UID_PREFIX)

    private fun exportWithReminders(horarium: Horarium) =
        exporter.export(horarium, CALENDAR_NAME, UID_PREFIX, withReminders = true)

    /** The TRIGGER lines in the order of the events they belong to. */
    private fun triggers(ics: String) = ics.split("\r\n").filter { it.startsWith("TRIGGER") }

    private fun horariumOf(vararg events: WeekViewEvent) = Horarium(events.toList())

    private fun event(
        id: String,
        name: String = "cena",
        start: Calendar = calendar(2026, Calendar.AUGUST, 1, 18, 0),
        durationMinutes: Int = 60,
        allDay: Boolean = false
    ): WeekViewEvent {
        val end = (start.clone() as Calendar).apply { add(Calendar.MINUTE, durationMinutes) }
        return WeekViewEvent(id, name, null, start, end, allDay)
    }

    private fun calendar(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }

    private fun uidLine(ics: String) = ics.split("\r\n").first { it.startsWith("UID:") }

    private fun occurrences(text: String, part: String) = text.split(part).size - 1

    companion object {
        private const val CALENDAR_NAME = "Septimana Latina 2026"
        private const val UID_PREFIX = "horarium_2026_la"
        private const val TRIGGER_BEFORE = "TRIGGER;RELATED=START:-PT5M"
        private const val TRIGGER_AT_START = "TRIGGER;RELATED=START:PT0S"
    }
}
