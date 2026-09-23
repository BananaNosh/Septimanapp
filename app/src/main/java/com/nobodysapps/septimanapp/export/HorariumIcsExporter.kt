package com.nobodysapps.septimanapp.export

import com.alamkanak.weekview.WeekViewEvent
import com.nobodysapps.septimanapp.model.Horarium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * Turns a [Horarium] into an iCalendar (RFC 5545) document that can be imported by any calendar app.
 *
 * Times are written as *floating* local times (no `Z`, no `TZID`): the horarium JSON only stores
 * wall-clock fields, so there is no timezone to preserve, and floating times keep exactly that
 * meaning without shipping a VTIMEZONE block.
 */
class HorariumIcsExporter @Inject constructor() {

    /**
     * @param uidPrefix distinguishes the horaria of different years/languages, e.g. "horarium_2026_la";
     *                  together with the event identifier it forms a stable UID, so re-importing
     *                  updates the events instead of duplicating them.
     * @param location shown as the events' location, usually the title of the main septimana location.
     * @param withReminders adds a VALARM to every event, see [reminderTrigger] for its offset.
     *                      Android's calendar import ignores VALARMs, see EXTENSIONS.md.
     */
    fun export(
        horarium: Horarium,
        calendarName: String,
        uidPrefix: String,
        location: String? = null,
        withReminders: Boolean = false,
        now: Calendar = Calendar.getInstance()
    ): String {
        val lines = ArrayList<String>()
        lines.add("BEGIN:VCALENDAR")
        lines.add("VERSION:2.0")
        lines.add("PRODID:-//nobodysapps//Septimanapp//EN")
        lines.add("CALSCALE:GREGORIAN")
        lines.add("METHOD:PUBLISH")
        lines.add("X-WR-CALNAME:${escapeText(calendarName)}")
        val dtStamp = formatUtc(now)
        for (event in horarium.events) {
            val trigger = if (withReminders) reminderTrigger(event, horarium.events) else null
            lines.addAll(eventLines(event, uidPrefix, location, trigger, dtStamp))
        }
        lines.add("END:VCALENDAR")
        return lines.flatMap { fold(it) }.joinToString(separator = LINE_BREAK, postfix = LINE_BREAK)
    }

    /**
     * Most events of the horarium follow each other without a pause; a reminder five minutes early
     * would then fire in the middle of the preceding event. It is therefore only used when there
     * really is a break before the event, otherwise the reminder is due at the event's start.
     *
     * All day events are ignored as predecessors (they cover the whole day) and get their reminder
     * at the start too, where five minutes early would mean the night before.
     */
    private fun reminderTrigger(event: WeekViewEvent, events: List<WeekViewEvent>): String {
        if (event.isAllDay) return TRIGGER_AT_START
        val hasEventDirectlyBefore = events.any { other ->
            other !== event && !other.isAllDay &&
                    other.startTime.before(event.startTime) &&
                    !other.endTime.before(event.startTime)  // ends at or after this event starts
        }
        return if (hasEventDirectlyBefore) TRIGGER_AT_START else TRIGGER_BEFORE_START
    }

    private fun eventLines(
        event: WeekViewEvent,
        uidPrefix: String,
        location: String?,
        reminderTrigger: String?,
        dtStamp: String
    ): List<String> {
        val summary = normalizeWhitespace(event.name)
        val lines = ArrayList<String>()
        lines.add("BEGIN:VEVENT")
        lines.add("UID:$uidPrefix-${event.identifier}@$UID_DOMAIN")
        lines.add("DTSTAMP:$dtStamp")
        if (event.isAllDay) {
            lines.add("DTSTART;VALUE=DATE:${formatDate(event.startTime)}")
            lines.add("DTEND;VALUE=DATE:${formatDate(dayAfter(event.endTime))}")
        } else {
            lines.add("DTSTART:${formatFloating(event.startTime)}")
            lines.add("DTEND:${formatFloating(event.endTime)}")
        }
        lines.add("SUMMARY:${escapeText(summary)}")
        val eventLocation = event.location?.takeIf { it.isNotBlank() } ?: location
        if (!eventLocation.isNullOrBlank()) {
            lines.add("LOCATION:${escapeText(normalizeWhitespace(eventLocation))}")
        }
        if (reminderTrigger != null) {
            lines.add("BEGIN:VALARM")
            lines.add("ACTION:DISPLAY")
            lines.add(reminderTrigger)
            lines.add("DESCRIPTION:${escapeText(summary)}")
            lines.add("END:VALARM")
        }
        lines.add("END:VEVENT")
        return lines
    }

    private fun dayAfter(calendar: Calendar) = (calendar.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, 1)
    }

    private fun formatUtc(calendar: Calendar) =
        format(calendar, "yyyyMMdd'T'HHmmss'Z'", TimeZone.getTimeZone("UTC"))

    private fun formatFloating(calendar: Calendar) = format(calendar, "yyyyMMdd'T'HHmmss")

    private fun formatDate(calendar: Calendar) = format(calendar, "yyyyMMdd")

    private fun format(calendar: Calendar, pattern: String, timeZone: TimeZone? = null): String {
        val formatter = SimpleDateFormat(pattern, Locale.US)
        formatter.timeZone = timeZone ?: calendar.timeZone
        return formatter.format(calendar.time)
    }

    /**
     * Event names in the horarium assets contain layout line breaks ("cena\n\n"); they would break
     * the ics content line, so all whitespace is collapsed to single spaces.
     */
    private fun normalizeWhitespace(text: String?) = (text ?: "").replace(WHITESPACE, " ").trim()

    private fun escapeText(text: String) = text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")

    /**
     * Splits a content line into chunks of at most [MAX_LINE_OCTETS] octets, continuation lines
     * starting with a space as required by RFC 5545.
     */
    private fun fold(line: String): List<String> {
        if (octets(line) <= MAX_LINE_OCTETS) return listOf(line)
        val chunks = ArrayList<String>()
        var chunk = StringBuilder()
        var chunkOctets = 0
        var limit = MAX_LINE_OCTETS
        var i = 0
        while (i < line.length) {
            val charCount = Character.charCount(line.codePointAt(i))
            val codePoint = line.substring(i, i + charCount)
            val codePointOctets = octets(codePoint)
            if (chunkOctets + codePointOctets > limit) {
                chunks.add(chunk.toString())
                chunk = StringBuilder()
                chunkOctets = 0
                limit = MAX_LINE_OCTETS - 1  // the leading space of a continuation line counts too
            }
            chunk.append(codePoint)
            chunkOctets += codePointOctets
            i += charCount
        }
        chunks.add(chunk.toString())
        return chunks.mapIndexed { index, chunkText -> if (index == 0) chunkText else " $chunkText" }
    }

    private fun octets(text: String) = text.toByteArray(Charsets.UTF_8).size

    companion object {
        const val REMINDER_MINUTES_BEFORE_EVENT = 5
        const val FILE_EXTENSION = "ics"
        const val MIME_TYPE = "text/calendar"

        // RELATED=START is the default of RFC 5545, but stating it leaves nothing to interpret for
        // the importing calendar app.
        private const val TRIGGER_BEFORE_START =
            "TRIGGER;RELATED=START:-PT${REMINDER_MINUTES_BEFORE_EVENT}M"
        private const val TRIGGER_AT_START = "TRIGGER;RELATED=START:PT0S"

        private const val UID_DOMAIN = "septimanapp.nobodysapps.com"
        private const val LINE_BREAK = "\r\n"
        private const val MAX_LINE_OCTETS = 75
        private val WHITESPACE = Regex("\\s+")
    }
}
