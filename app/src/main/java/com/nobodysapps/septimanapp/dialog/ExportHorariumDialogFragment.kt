package com.nobodysapps.septimanapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.nobodysapps.septimanapp.R

/**
 * Asked before the horarium is exported. Both buttons export; they only decide whether the exported
 * events carry a reminder some minutes before they start.
 *
 * The choice is delivered as a fragment result under [REQUEST_KEY] (see [withReminders]) instead of
 * via a listener field, so it still arrives after the dialog was recreated, e.g. on rotation.
 */
class ExportHorariumDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.dialog_export_horarium_title)
                .setMessage(R.string.dialog_export_horarium)
                .setPositiveButton(R.string.dialog_export_horarium_with_reminders) { _, _ ->
                    setResult(withReminders = true)
                }
                .setNegativeButton(R.string.dialog_export_horarium_without_reminders) { _, _ ->
                    setResult(withReminders = false)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setResult(withReminders: Boolean) {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            Bundle().apply { putBoolean(KEY_WITH_REMINDERS, withReminders) }
        )
    }

    companion object {
        const val REQUEST_KEY = "ExportHorariumDialogFragment.request"
        private const val KEY_WITH_REMINDERS = "withReminders"

        /** Reads the user's choice from a result delivered under [REQUEST_KEY]. */
        fun withReminders(result: Bundle) = result.getBoolean(KEY_WITH_REMINDERS)
    }
}
