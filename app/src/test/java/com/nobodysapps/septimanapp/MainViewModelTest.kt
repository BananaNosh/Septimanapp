package com.nobodysapps.septimanapp

import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import com.nobodysapps.septimanapp.model.storage.SeptimanaLocation
import com.nobodysapps.septimanapp.utils.CalendarUtils
import com.nobodysapps.septimanapp.viewModel.MainViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*


class MainViewModelTest {

    @Mock
    val eventInfoStorage: EventInfoStorage = Mockito.mock(EventInfoStorage::class.java)
    @Mock
    val locationStorage: LocationStorage = Mockito.mock(LocationStorage::class.java)
    private lateinit var calendarUtils: CalendarUtils
    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        val startTime = Calendar.getInstance()
        startTime.set(2021, 6, 31, 16, 30)
        val endTime = startTime.clone() as Calendar
        endTime.set(2021, 7, 7, 14, 0)
        `when`(eventInfoStorage.loadSeptimanaStartEndTime()).thenReturn(Pair(startTime, endTime))
        `when`(eventInfoStorage.loadSeptimanaLocation()).thenReturn(SeptimanaLocation.AMOENEBURG)
    }

    @Test
    fun testShouldShowRouteHint() {
        val now = Calendar.getInstance()
        now[2016, Calendar.JULY, 2, 0, 0] = 0
        calendarUtils = CalendarUtils(now)
        mainViewModel = MainViewModel(eventInfoStorage, locationStorage, calendarUtils)
        assertFalse(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 30, 20, 0] = 0
        assertTrue(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 30, 16, 0] = 0
        assertTrue(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 30, 4, 29] = 0
        assertFalse(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 30, 4, 31] = 0
        assertTrue(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 31, 16, 30] = 0
        assertTrue(mainViewModel.shouldShowRouteHint)
        now[2021, Calendar.JULY, 31, 16, 31] = 0
        assertFalse(mainViewModel.shouldShowRouteHint)
    }
}