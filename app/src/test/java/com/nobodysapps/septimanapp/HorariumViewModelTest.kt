package com.nobodysapps.septimanapp

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.alamkanak.weekview.WeekViewEvent
import com.nobodysapps.septimanapp.export.HorariumIcsExporter
import com.nobodysapps.septimanapp.model.Horarium
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import com.nobodysapps.septimanapp.viewModel.HorariumViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

/**
 * Tests the ics export offered by the horarium screen. The stored horarium has to use the year and
 * language the view model loads by default (current year, Latin on a non-German device).
 */
@RunWith(RobolectricTestRunner::class)
class HorariumViewModelTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var horariumStorage: HorariumStorage
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    @Before
    fun setup() {
        val jsonConverter = JsonConverter()
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("horarium_view_model_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        horariumStorage = HorariumStorage(prefs, jsonConverter)
    }

    @Test
    fun buildIcsWithoutHorariumIsNull() {
        assertNull(createViewModel().buildIcs("Septimana Latina", false))
    }

    @Test
    fun buildIcsWithEmptyHorariumIsNull() {
        horariumStorage.saveHorarium(Horarium(emptyList()), currentYear, "la")

        assertNull(createViewModel().buildIcs("Septimana Latina", false))
    }

    @Test
    fun buildIcsContainsStoredEventsAndTheShownYearInTheUid() {
        val start = Calendar.getInstance().apply {
            clear()
            set(currentYear, Calendar.AUGUST, 1, 18, 0, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 2) }
        horariumStorage.saveHorarium(
            Horarium(listOf(WeekViewEvent("ev_0", "cena", null, start, end, false))),
            currentYear,
            "la"
        )

        val ics = createViewModel().buildIcs("Septimana Latina", true)

        assertNotNull(ics)
        assertTrue(ics!!.contains("SUMMARY:cena"))
        assertTrue(ics.contains("UID:horarium_${currentYear}_la-ev_0@"))
        assertTrue(ics.contains("TRIGGER;RELATED=START:-PT5M"))  // nothing before the single event
    }

    private fun createViewModel() = HorariumViewModel(
        horariumStorage,
        prefs,
        EventInfoStorage(prefs, JsonConverter()),
        LocationStorage(prefs, JsonConverter()),
        HorariumIcsExporter()
    )
}
