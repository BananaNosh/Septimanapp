package com.nobodysapps.septimanapp.dependencyInjection

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nobodysapps.septimanapp.model.storage.EnrolPrefsMigration
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class SharedPreferencesModule {
    @Provides
    @SeptimanappApplicationScope
    @Inject
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    }

    /**
     * Encrypted preferences for the enrolment data (PII). Falls back to the default plaintext
     * preferences only if the Keystore is unusable even after a reset, so the app never crashes
     * on start.
     */
    @Provides
    @SeptimanappApplicationScope
    @EnrolPrefs
    @Inject
    fun provideEnrolSharedPreferences(
        context: Context,
        defaultPrefs: SharedPreferences
    ): SharedPreferences {
        val encrypted = createEncryptedPrefs(context) ?: return defaultPrefs
        EnrolPrefsMigration.migrate(defaultPrefs, encrypted)
        return encrypted
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences? {
        return try {
            buildEncryptedPrefs(context)
        } catch (e: Exception) {
            // Corrupted keyset, e.g. after a backup restore without the device's Keystore key.
            // Clearing through the prefs API also clears the framework's in-memory cache.
            context.getSharedPreferences(ENROL_PREFS_FILENAME, Context.MODE_PRIVATE)
                .edit().clear().commit()
            try {
                buildEncryptedPrefs(context)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            ENROL_PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val ENROL_PREFS_FILENAME = "enrol_prefs"
    }
}
