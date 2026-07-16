package com.nobodysapps.septimanapp

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_NONE
import com.nobodysapps.septimanapp.model.EnrolInformation.Companion.ACCEPT_STATE_YES
import com.nobodysapps.septimanapp.model.Vegan
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_ENROLLED
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage.Companion.ENROLLED_STATE_REMIND
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Write/read round-trip tests for [EnrolInformationStorage]. The storage gets a plain
 * SharedPreferences instance here; the production wiring injects an encrypted one
 * (see SharedPreferencesModule), which Robolectric cannot provide (no Keystore).
 */
@RunWith(RobolectricTestRunner::class)
class EnrolInformationStorageTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var storage: EnrolInformationStorage

    @Before
    fun setup() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("enrol_storage_test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        storage = EnrolInformationStorage(prefs, JsonConverter())
    }

    @Test
    fun allFieldsSurviveARoundTrip() {
        storage.saveName("Mustermann")
        storage.saveFirstName("Max")
        storage.saveStreet("Musterweg 1")
        storage.savePostal("12345")
        storage.saveCity("Musterstadt")
        storage.saveCountry("Deutschland")
        storage.savePhone("+49 123 456")
        storage.saveMail("max@example.org")
        storage.saveStayInJohanneshaus(false)
        storage.saveYearsOfLatin(3.5f)
        storage.saveEatingHabit(Vegan(listOf("nuces", "gluten")))
        storage.saveInstrument("tuba")
        storage.saveRoomOccupancy(2)
        storage.saveRoomBathroom(1)
        storage.saveRoomRemarks("planta inferior")
        storage.saveAge(30)
        storage.saveIsStudent(true)
        storage.saveAddressConsent(ACCEPT_STATE_YES)
        storage.saveImageConsent(ACCEPT_STATE_YES)

        val info = storage.loadEnrolInformation()

        assertEquals("Mustermann", info.name)
        assertEquals("Max", info.firstname)
        assertEquals("Musterweg 1", info.street)
        assertEquals("12345", info.postal)
        assertEquals("Musterstadt", info.city)
        assertEquals("Deutschland", info.country)
        assertEquals("+49 123 456", info.phone)
        assertEquals("max@example.org", info.mail)
        assertEquals(false, info.stayInMainBuilding)
        assertEquals(3.5f, info.yearsOfLatin)
        assertEquals(2, info.eatingHabit?.serializationCode)
        assertEquals(listOf("nuces", "gluten"), info.eatingHabit?.allergens)
        assertEquals("tuba", info.instrument)
        assertEquals(2, info.roomOccupancy)
        assertEquals(1, info.roomBathroom)
        assertEquals("planta inferior", info.roomRemarks)
        assertEquals(30, info.age)
        assertEquals(true, info.isStudent)
        assertEquals(ACCEPT_STATE_YES, info.addressConsent)
        assertEquals(ACCEPT_STATE_YES, info.imageConsent)
        assertTrue(info.isValid())
    }

    @Test
    fun emptyStorageYieldsDefaults() {
        val info = storage.loadEnrolInformation()

        assertEquals("", info.name)
        assertEquals("", info.mail)
        assertEquals(true, info.stayInMainBuilding)
        assertEquals(0f, info.yearsOfLatin)
        assertEquals(null, info.eatingHabit)
        assertEquals(ACCEPT_STATE_NONE, info.addressConsent)
        assertEquals(ACCEPT_STATE_NONE, info.imageConsent)
        assertEquals(0, info.age)
        assertEquals(false, info.isValid())
    }

    @Test
    fun invalidConsentValuesAreRejected() {
        // Debug builds throw; release silently ignores. Unit tests run against debug.
        assertThrows(IllegalArgumentException::class.java) { storage.saveAddressConsent(3) }
        assertThrows(IllegalArgumentException::class.java) { storage.saveImageConsent(-1) }
        assertEquals(ACCEPT_STATE_NONE, storage.loadEnrolInformation().addressConsent)
    }

    @Test
    fun enrolStateDefaultsToRemindAndIgnoresInvalidValues() {
        assertEquals(ENROLLED_STATE_REMIND, storage.loadEnrolState())

        storage.saveEnrolState(ENROLLED_STATE_ENROLLED)
        assertEquals(ENROLLED_STATE_ENROLLED, storage.loadEnrolState())

        storage.saveEnrolState(7)
        assertEquals(ENROLLED_STATE_REMIND, storage.loadEnrolState())
    }
}
