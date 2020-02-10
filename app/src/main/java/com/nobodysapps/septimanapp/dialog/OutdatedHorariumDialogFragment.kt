package com.nobodysapps.septimanapp.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.nobodysapps.septimanapp.R


class OutdatedHorariumDialogFragment : DialogFragment() {
    var listener: Listener? = null


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_outdated_horarium, null)
            val showAgainCheckbox = dialogView.findViewById<CheckBox>(R.id.notShowAgainCB)
            builder
                .setTitle(getString(R.string.dialog_outdated_horarium_title))
                .setView(dialogView)
                .setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    listener?.onOkClicked(showAgainCheckbox.isChecked)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface Listener {
        fun onOkClicked(notShowAgain: Boolean)
    }
}