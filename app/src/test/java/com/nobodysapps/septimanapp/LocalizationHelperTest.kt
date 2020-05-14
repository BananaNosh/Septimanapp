package com.nobodysapps.septimanapp

import org.junit.Test
// TODO consider using this package
//import com.google.common.truth.Truth.assertThat
import com.nobodysapps.septimanapp.localization.dateFormatSymbolsForLatin
import junit.framework.TestCase.assertEquals
import java.text.SimpleDateFormat
import java.util.*

class LatinDSFTest{

    val latinDFS = dateFormatSymbolsForLatin()
    val format = "EEE dd MMM yyyy"

    @Test
    fun latinDSF_ShortMonthsDisplayCorrectly(){
        val latinSimpleDateFormat = SimpleDateFormat(format, latinDFS)
        val date = Calendar.Builder().setDate(2020, 4, 14).build()
        assertEquals("Iov 14 Mai. 2020", latinSimpleDateFormat.format(date.getTime()))
    }
}

