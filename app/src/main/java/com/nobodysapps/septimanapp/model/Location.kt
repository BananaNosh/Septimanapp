package com.nobodysapps.septimanapp.model

import org.osmdroid.util.GeoPoint

data class Location(val id: String, val coordinates: GeoPoint, val description: String)