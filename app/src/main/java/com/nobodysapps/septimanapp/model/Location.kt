package com.nobodysapps.septimanapp.model

import org.osmdroid.util.GeoPoint
import java.util.*

data class Location(
    val id: String,
    private val titleMap: Map<String, String>,
    val coordinates: GeoPoint,
    private val descriptionMap: Map<String, String>
) {
    val title: String
        get() = titleMap[Locale.getDefault().language] ?: titleMap.values.firstOrNull() ?: ""

    val description: String
        get() = descriptionMap[Locale.getDefault().language] ?: descriptionMap.values.firstOrNull() ?: ""
}