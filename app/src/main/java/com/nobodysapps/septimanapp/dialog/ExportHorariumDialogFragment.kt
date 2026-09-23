package com.nobodysapps.septimanapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.nobodysapps.septimanapp.R

/**
 * Asked before the horarium is exported. Both buttons export; they only decide whether the exported
 * events carry a reminder some minutes before they start.
 */
class ExportHorariumDialogFragment : DialogFragment() {

    var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.dialog_export_horarium_title)
                .setMessage(R.string.dialog_export_horarium)
                .setPositiveButton(R.string.dialog_export_horarium_with_reminders) { _, _ ->
                    listener?.onExportChosen(withReminders = true)
                }
                .setNegativeButton(R.string.dialog_export_horarium_without_reminders) { _, _ ->
                    listener?.onExportChosen(withReminders = false)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface Listener {
        fun onExportChosen(withReminders: Boolean)
    }
}
