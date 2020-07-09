package com.nobodysapps.septimanapp.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import java.util.*

class MainViewModel(private val eventInfoStorage: EventInfoStorage, private val locationStorage: LocationStorage): ViewModel() {
    val septimanaStartTime: Calendar? by lazy {
        val eventTime = eventInfoStorage.loadSeptimanaStartEndTime()
        eventTime?.let {
            if (it.first.after(Calendar.getInstance()))
                it.first
            else
                null
        }
    }

    val shouldShowRouteHint: Boolean
        get() {
            val septimanaStartTime = eventInfoStorage.loadSeptimanaStartEndTime()?.first
            val earliest: Calendar? = septimanaStartTime?.clone() as Calendar?
            earliest?.add(Calendar.HOUR_OF_DAY, -SHOW_ROUTE_HINT_OFFSET_HOURS)
            val now = Calendar.getInstance()
            return septimanaStartTime != null && earliest != null && earliest.before(now) && septimanaStartTime.after(now)
        }

    val septimanaLocationUri: Uri? by lazy {
        val mainLocation =
            locationStorage.loadLocations(eventInfoStorage.loadSeptimanaLocation())?.first {
                it.isMain
            }
        mainLocation?.let {
            val uri =
                "geo:${it.coordinates.longitude},${it.coordinates.latitude}?q=${it.titleForLocale(
                    Locale.GERMAN
                )}"
            Uri.parse(uri)
        }
    }

    companion object {
        const val SHOW_ROUTE_HINT_OFFSET_HOURS = 36
    }
}