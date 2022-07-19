package com.nobodysapps.septimanapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nobodysapps.septimanapp.R


class ConfirmEnrolmentDialogFragment: MessageAndCheckboxDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialog: AlertDialog = super.onCreateDialog(savedInstanceState) as AlertDialog
            dialog.setTitle(R.string.dialog_confirm_enrolment_title)
            setCheckboxText(R.string.dialog_confirm_enrolment)
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->}
            dialog.setOnShowListener {
                val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveBtn.isEnabled = false
                checkBox?.setOnCheckedChangeListener { _, isChecked ->
                    positiveBtn.isEnabled = isChecked
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}