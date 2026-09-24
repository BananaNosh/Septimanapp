package com.nobodysapps.septimanapp.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.nobodysapps.septimanapp.R

/**
 * A message with one or two checkboxes and an OK button. OK is delivered as a fragment result under
 * the subclass' [requestKey] (read the first checkbox with [isChecked]) instead of via a listener
 * field, so it still arrives after the dialog was recreated, e.g. on rotation.
 */
abstract class MessageAndCheckboxDialogFragment: DialogFragment() {
    /** Key the OK result is delivered under, in the fragment manager the dialog was shown in. */
    protected abstract val requestKey: String
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
                    parentFragmentManager.setFragmentResult(
                        requestKey,
                        Bundle().apply { putBoolean(KEY_IS_CHECKED, checkBox?.isChecked ?: false) }
                    )
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

    companion object {
        private const val KEY_IS_CHECKED = "isChecked"

        /** Reads from an OK result whether the first checkbox was checked. */
        fun isChecked(result: Bundle) = result.getBoolean(KEY_IS_CHECKED)
    }
}
