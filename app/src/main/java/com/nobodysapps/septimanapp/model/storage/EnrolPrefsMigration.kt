package com.nobodysapps.septimanapp.model.storage

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * One-time migration of the enrolment data from the plaintext default preferences into the
 * encrypted enrolment preferences. Safe to call on every start: a no-op once the legacy
 * preferences no longer contain any enrolment keys.
 */
object EnrolPrefsMigration {

    fun migrate(legacy: SharedPreferences, encrypted: SharedPreferences) {
        val toMigrate = legacy.all.filterKeys { it in EnrolInformationStorage.ALL_KEYS }
        if (toMigrate.isEmpty()) return
        encrypted.edit {
            toMigrate.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                }
            }
        }
        legacy.edit {
            toMigrate.keys.forEach { remove(it) }
        }
    }
}
