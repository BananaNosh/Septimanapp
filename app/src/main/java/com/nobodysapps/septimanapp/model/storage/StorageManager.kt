package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import com.nobodysapps.septimanapp.viewModel.HorariumViewModel
import java.util.*
import javax.inject.Inject

class StorageManager @Inject constructor(private val sharedPreferences: SharedPreferences,
                                         private val eventInfoStorage: EventInfoStorage,
                                         private val enrolInformationStorage: EnrolInformationStorage) {

    fun resetAfterSeptimana() {
        val currentSavedYear = sharedPreferences.getInt(CURRENT_SEPTIMANA_YEAR_KEY, 0)
        val septimanaYear = eventInfoStorage.loadSeptimanaStartEndTime()?.first?.get(Calendar.YEAR) ?: 0
        if (currentSavedYear != septimanaYear) {
            sharedPreferences.edit().putInt(CURRENT_SEPTIMANA_YEAR_KEY, septimanaYear).apply()

            enrolInformationStorage.saveEnrolState(EnrolInformationStorage.ENROLLED_STATE_REMIND)
            sharedPreferences.edit().putBoolean(HorariumViewModel.SHOW_AGAIN_KEY, true).apply()
        }
    }

    companion object {
        private const val CURRENT_SEPTIMANA_YEAR_KEY = "current_year"
    }
}