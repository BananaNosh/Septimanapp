package com.nobodysapps.septimanapp.viewModel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.model.Horarium
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import java.util.*

class HorariumViewModel(val horariumStorage: HorariumStorage, val sharedPreferences: SharedPreferences) : ViewModel() {

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
            sharedPreferences.edit().putBoolean(SHOW_AGAIN_KEY, value)
                .apply()
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
        return horariumStorage.loadHorarium(currentYear, horariumLanguage.language)
    }

    companion object {
        const val SHOW_AGAIN_KEY = "show_again"
    }

}