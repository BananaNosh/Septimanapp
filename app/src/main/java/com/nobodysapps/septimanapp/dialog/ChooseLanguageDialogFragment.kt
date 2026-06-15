package com.nobodysapps.septimanapp.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.SettingsActivity
import com.nobodysapps.septimanapp.localization.LocaleHelper
import java.util.*

class ChooseLanguageDialogFragment : DialogFragment() {
    // dialog_choose_language carries a trailing "%s" sentinel in the default locale to flag
    // the German fallback; the String.format below intentionally strips it (see usesDefault).
    @SuppressLint("StringFormatInvalid")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            var dialogMessage = getString(R.string.dialog_choose_language)
            var usesDefault = false
            if (dialogMessage.contains("%s")) {
                // default strings are used
                dialogMessage = String.format(dialogMessage, "")
                usesDefault = true
            }
            builder.setMessage(dialogMessage)
                .setPositiveButton(
                    Locale("la").displayLanguage
                ) { _, _ ->
                    saveLanguageUse(true)
                    dismiss()
                    activity?.recreate()
                }
                .setNegativeButton(
                    (if (usesDefault) Locale.GERMAN else Locale.getDefault()).displayLanguage
                ) { _, _ ->
                    // User cancelled the dialog
                    saveLanguageUse(false)
                    dismiss()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun saveLanguageUse(useLatin: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.edit {
            putBoolean(SettingsActivity.SettingsFragment.KEY_USE_LATIN, useLatin)
            putString(SettingsActivity.SettingsFragment.KEY_PREF_LANGUAGE, if (useLatin) "la" else "system")
        }
        if (useLatin) {
            context?.let {
                LocaleHelper.setLocale(it, "la")
            }
        }
    }
}
