package com.nobodysapps.septimanapp

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device tests for the encrypted enrolment prefs (the real Keystore-backed
 * EncryptedSharedPreferences, which Robolectric cannot exercise). Verifies the round trip
 * through [EnrolInformationStorage], that nothing lands on disk in plaintext, and that the
 * legacy plaintext values are migrated over.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class EncryptedEnrolStorageTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val module = SharedPreferencesModule()
    private val plainPrefs = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

    @Test
    fun roundTripThroughEncryptedPrefsAndNoPlaintextOnDisk() {
        val encrypted = module.provideEnrolSharedPreferences(context, plainPrefs)
        encrypted.edit().clear().commit()
        val storage = EnrolInformationStorage(encrypted, JsonConverter())

        storage.saveName("Mustermann")
        storage.saveMail("max@example.org")
        storage.saveAge(30)

        val info = storage.loadEnrolInformation()
        assertEquals("Mustermann", info.name)
        assertEquals("max@example.org", info.mail)
        assertEquals(30, info.age)

        // The raw prefs file must contain neither the key names nor the values in plaintext.
        val raw = context.getSharedPreferences(
            SharedPreferencesModule.ENROL_PREFS_FILENAME, Context.MODE_PRIVATE
        )
        assertFalse(raw.all.keys.any { it == "name" || it == "mail" || it == "age" })
        assertFalse(raw.all.values.filterIsInstance<String>().any { it.contains("Mustermann") })
    }

    @Test
    fun legacyPlaintextValuesAreMigrated() {
        module.provideEnrolSharedPreferences(context, plainPrefs).edit().clear().commit()
        plainPrefs.edit().putString("name", "Legacy").putInt("age", 42).commit()

        val encrypted = module.provideEnrolSharedPreferences(context, plainPrefs)

        val info = EnrolInformationStorage(encrypted, JsonConverter()).loadEnrolInformation()
        assertEquals("Legacy", info.name)
        assertEquals(42, info.age)
        assertFalse(plainPrefs.contains("name"))
        assertFalse(plainPrefs.contains("age"))
    }
}
