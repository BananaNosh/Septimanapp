package com.nobodysapps.septimanapp.model

/**
 * Gson DTO for assets/event_info.json — the yearly-editable event data.
 * Dates are "yyyy-MM-dd'T'HH:mm", location is a [com.nobodysapps.septimanapp.model.storage.SeptimanaLocation] key.
 */
data class EventInfo(
    val start: String?,
    val end: String?,
    val location: String?
)
