package com.nobodysapps.septimanapp.viewModel

import androidx.core.content.edit
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.export.HorariumIcsExporter
import com.nobodysapps.septimanapp.model.Horarium
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import java.util.*

class HorariumViewModel(
    val horariumStorage: HorariumStorage,
    val sharedPreferences: SharedPreferences,
    private val eventInfoStorage: EventInfoStorage,
    private val locationStorage: LocationStorage,
    private val icsExporter: HorariumIcsExporter
) : ViewModel() {

    /** The year of the horarium currently shown, which is not the current year after [usePreviousHorarium]. */
    var shownYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        private set

    var horariumLanguage: Locale = when (Locale.getDefault()) {
        Locale.GERMAN -> Locale.GERMAN
        else -> Locale("la")
    }
        set(value) {
            if (value != field) {
                field = value
                horarium.value = loadHorariumInCorrectLanguage()
            }
            field = value
        }

    var horarium: MutableLiveData<Horarium?> = MutableLiveData(loadHorariumInCorrectLanguage())

    var shouldShowWarning: Boolean
        get() {
            return sharedPreferences.getBoolean(SHOW_AGAIN_KEY, true)
        }
        set(value) {
            sharedPreferences.edit {putBoolean(SHOW_AGAIN_KEY, value)
                }
        }

    fun hasPreviousHorarium(): Boolean {
        return loadPreviousHorarium() != null
    }

    fun usePreviousHorarium() {
        horarium.value = loadPreviousHorarium()
    }

    private fun loadPreviousHorarium(): Horarium? {
        val previousYear = Calendar.getInstance().get(Calendar.YEAR) - 1
        val previousHorarium = loadHorariumInCorrectLanguage(previousYear)
        return previousHorarium
    }

    fun toggleHorariumLanguage() {
        horariumLanguage = toggledHorariumLocale()
    }

    fun toggledHorariumLocale(): Locale {
        val la = Locale("la")
        return when (horariumLanguage) {
            la -> Locale.GERMAN
            else -> la
        }
    }

    private fun loadHorariumInCorrectLanguage(year: Int? = null): Horarium? {
        val currentYear = year ?: Calendar.getInstance().get(Calendar.YEAR)
        val loaded = horariumStorage.loadHorarium(currentYear, horariumLanguage.language)
        if (loaded != null) {
            shownYear = currentYear
        }
        return loaded
    }

    /**
     * Builds the iCalendar document for the currently shown horarium, or null if there is nothing
     * to export.
     */
    fun buildIcs(calendarName: String, withReminders: Boolean): String? {
        val currentHorarium = horarium.value ?: return null
        if (currentHorarium.events.isEmpty()) return null
        return icsExporter.export(
            currentHorarium,
            calendarName,
            uidPrefix = "horarium_${shownYear}_${horariumLanguage.language}",
            location = mainLocationTitle(),
            withReminders = withReminders
        )
    }

    private fun mainLocationTitle(): String? {
        val locations = locationStorage.loadLocations(eventInfoStorage.loadSeptimanaLocation())
        return locations?.firstOrNull { it.isMain }?.titleForLocale(horariumLanguage)
    }

    companion object {
        const val SHOW_AGAIN_KEY = "show_again"
    }

}
