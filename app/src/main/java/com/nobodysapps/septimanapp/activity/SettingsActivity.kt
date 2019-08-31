package com.nobodysapps.septimanapp.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.localization.LocaleHelper
import java.util.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


class SettingsActivity : SeptimanappActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (context == null) return
            when (key) {
                KEY_USE_LATIN -> {
                    val useLatin = PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(key, false)
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
                        KEY_PREF_LANGUAGE, if (useLatin) "la" else "system"
                    ).apply()
                }
                KEY_PREF_LANGUAGE -> {
                    LocaleHelper.setLocale(
                        context!!,
                        PreferenceManager.getDefaultSharedPreferences(context).getString(key, "")
                            ?: ""
                    )
                    activity?.recreate() // necessary here because this Activity is currently running and thus a recreate() in onResume() would be too late
                }
            }
        }

        override fun onResume() {
            super.onResume()
            // documentation requires that a reference to the listener is kept as long as it may be called, which is the case as it can only be called from this Fragment
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        companion object {
            const val KEY_USE_LATIN = "use_latin"
            const val KEY_PREF_LANGUAGE = "language"
        }
    }
}