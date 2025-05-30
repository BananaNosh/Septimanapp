package com.nobodysapps.septimanapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.nobodysapps.septimanapp.R


class ConfirmEnrolmentDialogFragment: MessageAndCheckboxDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialog: AlertDialog = super.onCreateDialog(savedInstanceState) as AlertDialog
            dialog.setTitle(R.string.dialog_confirm_enrolment_title)
            setCheckboxText(R.string.dialog_confirm_enrolment)
            setCheckbox2Text(R.string.dialog_confirm_enrolment_data_privacy)
            checkBox2?.movementMethod = LinkMovementMethod.getInstance()
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, _ ->}
            dialog.setOnShowListener {
                val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveBtn.isEnabled = false
                checkBox?.setOnCheckedChangeListener { _, isChecked ->
                    positiveBtn.isEnabled = isChecked && (checkBox2?.isChecked ?: false)
                }
                checkBox2?.setOnCheckedChangeListener { _, isChecked ->
                    positiveBtn.isEnabled = isChecked && (checkBox?.isChecked ?: false)
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}