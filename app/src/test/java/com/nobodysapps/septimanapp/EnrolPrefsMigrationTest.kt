package com.nobodysapps.septimanapp

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.nobodysapps.septimanapp.model.storage.EnrolPrefsMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests the one-time move of enrolment values from the legacy plaintext "pref" file into the
 * (here: stand-in) encrypted enrolment prefs. Key names are written out literally on purpose —
 * they are the on-disk contract of existing installations.
 */
@RunWith(RobolectricTestRunner::class)
class EnrolPrefsMigrationTest {

    private lateinit var legacy: SharedPreferences
    private lateinit var encrypted: SharedPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        legacy = context.getSharedPreferences("legacy_test", Context.MODE_PRIVATE)
        encrypted = context.getSharedPreferences("encrypted_test", Context.MODE_PRIVATE)
        legacy.edit().clear().commit()
        encrypted.edit().clear().commit()
    }

    @Test
    fun movesAllValueTypesAndRemovesThemFromLegacy() {
        legacy.edit()
            .putString("name", "Mustermann")
            .putString("eating_habit", "[2,[\"nuces\"]]")
            .putBoolean("johanneshaus", false)
            .putFloat("years_latin", 3.5f)
            .putInt("age", 30)
            .putInt("enrolled_state", 2)
            .commit()

        EnrolPrefsMigration.migrate(legacy, encrypted)

        assertEquals("Mustermann", encrypted.getString("name", null))
        assertEquals("[2,[\"nuces\"]]", encrypted.getString("eating_habit", null))
        assertEquals(false, encrypted.getBoolean("johanneshaus", true))
        assertEquals(3.5f, encrypted.getFloat("years_latin", 0f))
        assertEquals(30, encrypted.getInt("age", 0))
        assertEquals(2, encrypted.getInt("enrolled_state", -1))
        assertTrue(legacy.all.isEmpty())
    }

    @Test
    fun leavesNonEnrolmentKeysInLegacy() {
        legacy.edit()
            .putString("name", "Mustermann")
            .putString("horarium_2025_la", "{}")
            .putBoolean("language_dialog_shown", true)
            .commit()

        EnrolPrefsMigration.migrate(legacy, encrypted)

        assertFalse(legacy.contains("name"))
        assertEquals("{}", legacy.getString("horarium_2025_la", null))
        assertTrue(legacy.getBoolean("language_dialog_shown", false))
        assertFalse(encrypted.contains("horarium_2025_la"))
    }

    @Test
    fun secondRunIsANoOpAndKeepsNewerValues() {
        legacy.edit().putString("name", "Mustermann").commit()
        EnrolPrefsMigration.migrate(legacy, encrypted)

        // The user edits the (already migrated) value, then migration runs again on next start.
        encrypted.edit().putString("name", "Novus Nomen").commit()
        EnrolPrefsMigration.migrate(legacy, encrypted)

        assertEquals("Novus Nomen", encrypted.getString("name", null))
    }
}
