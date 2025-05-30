package com.nobodysapps.septimanapp.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nobodysapps.septimanapp.R

open class MessageAndCheckboxDialogFragment: DialogFragment() {
    var listener: Listener? = null
    private var textView: TextView? = null
    protected var checkBox: CheckBox? = null
    protected var checkBox2: CheckBox? = null


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_with_checkbox, null)
            checkBox = dialogView.findViewById(R.id.dialogCB)
            checkBox2 = dialogView.findViewById(R.id.dialogCB2)
            textView = dialogView.findViewById(R.id.dialogTV)
            builder
                .setView(dialogView)
                .setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    listener?.onOkClicked(checkBox?.isChecked ?: false)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    protected fun setMessage(@Suppress("SameParameterValue") id: Int) {
        textView?.setText(id)
    }

    @Suppress("unused")
    protected fun setMessage(text: CharSequence) {
        textView?.text = text
    }

    protected fun setCheckboxText(id: Int) {
        checkBox?.setText(id)
    }

    @Suppress("unused")
    protected fun setCheckboxText(text: CharSequence) {
        checkBox?.text = text
    }

    protected fun setCheckbox2Text(id: Int) {
        checkBox2?.visibility = CheckBox.VISIBLE
        checkBox2?.setText(id)
    }

    @Suppress("unused")
    protected fun setCheckbox2Text(text: CharSequence) {
        checkBox2?.visibility = CheckBox.VISIBLE
        checkBox2?.text = text
    }

    interface Listener {
        fun onOkClicked(isChecked: Boolean)
    }
}