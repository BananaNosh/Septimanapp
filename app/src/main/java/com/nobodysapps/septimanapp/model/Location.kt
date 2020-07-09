package com.nobodysapps.septimanapp.model

import org.osmdroid.util.GeoPoint
import java.util.*

data class Location(
    val id: String,
    private val titleMap: Map<String, String>,
    val coordinates: GeoPoint,
    private val descriptionMap: Map<String, String>,
    val isMain: Boolean = false
) {
    val title: String
        get() = titleForLocale(Locale.getDefault())

    fun titleForLocale(locale: Locale): String = titleMap[locale.language] ?: titleMap.values.firstOrNull() ?: ""

    val description: String
        get() = descriptionMap[Locale.getDefault().language] ?: descriptionMap.values.firstOrNull()
        ?: ""
}