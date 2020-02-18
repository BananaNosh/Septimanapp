package com.nobodysapps.septimanapp.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import com.nobodysapps.septimanapp.R


class OutdatedHorariumDialogFragment: MessageAndCheckboxDialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.setTitle(R.string.dialog_outdated_horarium_title)
            setMessage(R.string.dialog_outdated_horarium)
            setCheckboxText(R.string.dialog_do_not_show_again)
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}