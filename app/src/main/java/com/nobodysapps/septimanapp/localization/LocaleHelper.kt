package com.nobodysapps.septimanapp.localization

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import com.nobodysapps.septimanapp.activity.SettingsActivity
import java.util.*


/**
 * Manages setting of the app's locale.
 */
object LocaleHelper {

    fun onAttach(context: Context): Context {
        val locale = getPersistedLocale(context)
        return setLocale(context, locale)
    }

    fun getPersistedLocale(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SettingsActivity.SettingsFragment.KEY_PREF_LANGUAGE, null) ?: Locale.getDefault().language
    }

    /**
     * Set the app's locale to the one specified by the given String.
     *
     * @param context
     * @param localeSpec a locale specification as used for Android resources (NOTE: does not
     * support country and variant codes so far); the special string "system" sets
     * the locale to the locale specified in system settings
     * @return
     */
    @Suppress("DEPRECATION")
    fun setLocale(context: Context, localeSpec: String): Context {
        val locale: Locale = if (localeSpec == "system") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales.get(0)
            } else {

                Resources.getSystem().configuration.locale
            }
        } else {
            Locale(localeSpec)
        }
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }
}