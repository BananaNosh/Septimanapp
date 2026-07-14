package com.nobodysapps.septimanapp.model

import java.util.*

/**
 * The set of proposita (elective course topics / Wahlgruppen) offered at a Septimana.
 * Wraps a list so Gson binds it from a named top-level object, mirroring the horarium/locations
 * JSON shape.
 */
data class Proposita(val proposita: List<Propositum>)

/**
 * A single course topic. [author] is language-independent (presenter's name); [subjectMap] and
 * [descriptionMap] hold the short subject and full description keyed by language ("la", "de"),
 * mirroring [Location]'s locale-map approach.
 */
data class Propositum(
    val id: String,
    val author: String,
    private val subjectMap: Map<String, String>,
    private val descriptionMap: Map<String, String>
) {
    fun subjectForLocale(locale: Locale): String =
        subjectMap[locale.language] ?: subjectMap.values.firstOrNull() ?: ""

    fun descriptionForLocale(locale: Locale): String =
        descriptionMap[locale.language] ?: descriptionMap.values.firstOrNull() ?: ""
}
