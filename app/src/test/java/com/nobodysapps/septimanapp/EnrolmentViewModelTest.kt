package com.nobodysapps.septimanapp

import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nobodysapps.septimanapp.model.EnrolInformation
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_YES
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_IN_PROGRESS
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_NOT_ASK_AGAIN
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_REMIND
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import com.nobodysapps.septimanapp.viewModel.EnrolmentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
class EnrolmentViewModelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var prefs: SharedPreferences
    private lateinit var informationStorage: EnrolInformationStorage
    private lateinit var eventInfoStorage: EventInfoStorage
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var viewModel: EnrolmentViewModel

    @Before
    fun setup() {
        prefs = context.getSharedPreferences("enrol_vm_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        informationStorage = EnrolInformationStorage(prefs, JsonConverter())
        eventInfoStorage = EventInfoStorage(prefs, JsonConverter())
        alarmScheduler = Mockito.mock(AlarmScheduler::class.java)
        notificationHelper = Mockito.mock(NotificationHelper::class.java)
        viewModel = EnrolmentViewModel(
            informationStorage, eventInfoStorage, alarmScheduler, notificationHelper
        )
    }

    /**
     * Mockito's any()/nullable() return null, which Kotlin's call-site null check rejects for
     * the non-null Calendar parameter — route them through Kotlin helpers to skip that check.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> anyMatcher(type: Class<T>): T {
        Mockito.any(type)
        return null as T
    }

    private fun verifyScheduledAlarms(expectedTimes: Int) {
        verify(alarmScheduler, times(expectedTimes)).scheduleAlarm(
            anyMatcher(Calendar::class.java),
            Mockito.nullable(PendingIntent::class.java)
        )
    }

    private fun fillCompletely() {
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "Mustermann")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_FIRSTNAME, "Max")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_STREET_ADDRESS, "Musterweg 1")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_POSTAL, "12345")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_CITY, "Musterstadt")
        viewModel.saveCountry("Deutschland")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_PHONE, "+49 123")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_MAIL, "max@example.org")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_AGE, "30")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_YEARS_LATIN, "3.5")
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_INSTRUMENT, "tuba")
        viewModel.saveRoomOccupancy(1)
        viewModel.saveRoomBathroom(1)
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_ROOM_REMARKS, "planta inferior")
        viewModel.saveImageConsent(ACCEPT_STATE_YES)
        viewModel.saveAddressConsent(ACCEPT_STATE_YES)
        viewModel.saveIsStudent(true)
    }

    @Test
    fun missingFieldsListsAllRequiredFieldsOnEmptyFormAndShrinksWhenFilled() {
        val empty = viewModel.loadEnrolInformation()
        assertEquals(
            EnrolInformation.RequiredField.values().toList(),
            empty.missingFields()
        )
        assertFalse(empty.isValid())

        fillCompletely()
        val filled = viewModel.loadEnrolInformation()
        assertTrue(filled.missingFields().isEmpty())
        assertTrue(filled.isValid())
    }

    @Test
    fun textFieldChangesArePersisted() {
        fillCompletely()
        val info = viewModel.loadEnrolInformation()
        assertEquals("Mustermann", info.name)
        assertEquals("planta inferior", info.roomRemarks)
        assertEquals(30, info.age)
        assertEquals(3.5f, info.yearsOfLatin)
    }

    @Test
    fun firstEditSchedulesContinueReminderAndAlternatesLikeBefore() {
        // Fresh form: state REMIND -> first edit goes to IN_PROGRESS and schedules.
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "M")
        assertEquals(ENROLLED_STATE_IN_PROGRESS, informationStorage.loadEnrolState())
        verifyScheduledAlarms(1)

        // Second edit: state was IN_PROGRESS -> falls back to REMIND, no new alarm.
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "Mu")
        assertEquals(ENROLLED_STATE_REMIND, informationStorage.loadEnrolState())
        verifyScheduledAlarms(1)

        // Third edit re-enters the scheduling branch -> reminder moves to "1 day after latest edit".
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "Mus")
        verifyScheduledAlarms(2)
    }

    @Test
    fun notAskAgainNeverSchedulesAReminder() {
        informationStorage.saveEnrolState(ENROLLED_STATE_NOT_ASK_AGAIN)
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "M")
        verifyScheduledAlarms(0)
    }

    @Test
    fun emptyInputDoesNotTouchTheStateMachine() {
        viewModel.onTextFieldChanged(EnrolmentViewModel.FIELD_NAME, "")
        assertEquals(ENROLLED_STATE_REMIND, informationStorage.loadEnrolState())
        verifyScheduledAlarms(0)
    }

    @Test
    fun buildsCompleteEnrolmentEmail() {
        fillCompletely()
        eventInfoStorage.saveEventInfoFromJson(
            """{ "start": "2099-08-01T16:00", "end": "2099-08-08T14:00", "location": "braunfels" }"""
        )

        val email = viewModel.buildEnrolmentEmail(context)

        assertEquals(context.getString(R.string.enrol_send_email_address), email.address)
        assertTrue(email.subject.contains("2099"))
        assertTrue(email.subject.contains("Mustermann"))
        for (expected in listOf(
            "Max", "Mustermann", "Musterweg 1", "12345", "Musterstadt", "Deutschland",
            "+49 123", "max@example.org", "tuba", "planta inferior", "Alter: 30"
        )) {
            assertTrue("body should contain '$expected'", email.body.contains(expected))
        }
    }

    @Test
    fun countryListIsSortedDistinctAndNonEmpty() {
        val countries = viewModel.countryList()
        assertTrue(countries.isNotEmpty())
        assertEquals(countries.sorted(), countries)
        assertEquals(countries.distinct().size, countries.size)
        assertTrue(countries.none { it.isBlank() })
    }
}
