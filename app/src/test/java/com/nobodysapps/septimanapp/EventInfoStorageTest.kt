package com.nobodysapps.septimanapp

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import com.nobodysapps.septimanapp.model.storage.SeptimanaLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

/**
 * Tests parsing/storing of the assets/event_info.json content. Round-trip tests must use
 * future dates: loadSeptimanaStartEndTime() returns null once the event end has passed.
 */
@RunWith(RobolectricTestRunner::class)
class EventInfoStorageTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var storage: EventInfoStorage

    @Before
    fun setup() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("event_info_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        storage = EventInfoStorage(prefs, JsonConverter())
    }

    @Test
    fun validJsonRoundTrip() {
        storage.saveEventInfoFromJson(
            """{ "start": "2099-08-01T16:00", "end": "2099-08-08T14:00", "location": "braunfels" }"""
        )

        val startEnd = storage.loadSeptimanaStartEndTime()
        assertNotNull(startEnd)
        val (start, end) = startEnd!!
        assertEquals(2099, start.get(Calendar.YEAR))
        assertEquals(Calendar.AUGUST, start.get(Calendar.MONTH))
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(16, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
        assertEquals(0, start.get(Calendar.SECOND))
        assertEquals(8, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(SeptimanaLocation.BRAUNFELS, storage.loadSeptimanaLocation())
    }

    @Test
    fun unknownLocationKeepsStoredValues() {
        storage.saveEventInfoFromJson(
            """{ "start": "2099-08-01T16:00", "end": "2099-08-08T14:00", "location": "amoeneburg" }"""
        )

        storage.saveEventInfoFromJson(
            """{ "start": "2100-08-01T16:00", "end": "2100-08-08T14:00", "location": "atlantis" }"""
        )

        assertEquals(SeptimanaLocation.AMOENEBURG, storage.loadSeptimanaLocation())
        assertEquals(2099, storage.loadSeptimanaStartEndTime()!!.first.get(Calendar.YEAR))
    }

    @Test
    fun malformedJsonAndDatesAreSafeNoOps() {
        storage.saveEventInfoFromJson(
            """{ "start": "2099-08-01T16:00", "end": "2099-08-08T14:00", "location": "braunfels" }"""
        )

        storage.saveEventInfoFromJson("not json at all")
        storage.saveEventInfoFromJson("""{ "start": "morgen", "end": "2100-08-08T14:00", "location": "braunfels" }""")
        storage.saveEventInfoFromJson("""{ "end": "2100-08-08T14:00", "location": "braunfels" }""")

        assertEquals(2099, storage.loadSeptimanaStartEndTime()!!.first.get(Calendar.YEAR))
        assertEquals(SeptimanaLocation.BRAUNFELS, storage.loadSeptimanaLocation())
    }
}
