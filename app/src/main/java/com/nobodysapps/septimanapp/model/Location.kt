package com.nobodysapps.septimanapp.model

import org.osmdroid.util.GeoPoint

data class Location(private val id: String, private val coordinates: GeoPoint, private val description: String)