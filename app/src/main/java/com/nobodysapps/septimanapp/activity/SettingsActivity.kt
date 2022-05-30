package com.nobodysapps.septimanapp.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.nobodysapps.septimanapp.BuildConfig
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.localization.LocaleHelper


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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
                    val useLatin = PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getBoolean(key, false)
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putString(
                        KEY_PREF_LANGUAGE, if (useLatin) "la" else "system"
                    ).apply()
                }
                KEY_PREF_LANGUAGE -> {
                    LocaleHelper.setLocale(
                        requireContext(),
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(key, "")
                            ?: ""
                    )
                    activity?.recreate() // necessary here because this Activity is currently running and thus a recreate() in onResume() would be too late
                }
            }
        }

        override fun onResume() {
            super.onResume()
            // documentation requires that a reference to the listener is kept as long as it may be called, which is the case as it can only be called from this Fragment
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
            preferenceScreen.findPreference<Preference>(KEY_LICENSES)?.setOnPreferenceClickListener {
                displayLicensesAlertDialog()
                true
            }
        }

        @SuppressLint("InflateParams")
        private fun displayLicensesAlertDialog() {
            val view =
                layoutInflater.inflate(R.layout.dialog_licenses, null) as WebView
            view.loadUrl("file:///android_asset/open_source_licenses.html")
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_licenses_title))
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .show()
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        companion object {
            const val KEY_USE_LATIN = "use_latin"
            const val KEY_PREF_LANGUAGE = "language"
            const val KEY_LICENSES = "licenses"
        }
    }
}