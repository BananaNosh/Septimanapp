package com.nobodysapps.septimanapp

import android.app.AlertDialog
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.robolectric.Shadows.shadowOf

/**
 * Stands in for the screens (HorariumFragment, EnrolmentFragment), which show their dialogs as child
 * fragments and register the result listeners in onCreate. Records every result delivered under one
 * of [requestKeys].
 */
class DialogResultHost : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (key in requestKeys) {
            childFragmentManager.setFragmentResultListener(key, this) { requestKey, result ->
                results.add(requestKey to result)
            }
        }
    }

    companion object {
        private const val HOST_TAG = "host"
        const val DIALOG_TAG = "dialog"

        // Static, because recreating the activity replaces the fragment instance.
        val requestKeys = ArrayList<String>()
        val results = ArrayList<Pair<String, Bundle>>()

        fun reset(vararg keys: String) {
            requestKeys.clear()
            requestKeys.addAll(keys)
            results.clear()
        }

        /** Adds a host to [activity] and shows [dialog] as its child. */
        fun show(activity: FragmentActivity, dialog: DialogFragment) {
            val host = DialogResultHost()
            activity.supportFragmentManager.beginTransaction().add(host, HOST_TAG).commitNow()
            dialog.show(host.childFragmentManager, DIALOG_TAG)
            host.childFragmentManager.executePendingTransactions()
        }

        /** The dialog currently shown by the host in [activity], e.g. the one restored after recreation. */
        fun shownDialog(activity: FragmentActivity): AlertDialog {
            // Dialog.show() posts its OnShowListener call, let it run first.
            shadowOf(Looper.getMainLooper()).idle()
            val host = activity.supportFragmentManager.findFragmentByTag(HOST_TAG) as DialogResultHost
            val dialogFragment = host.childFragmentManager.findFragmentByTag(DIALOG_TAG) as DialogFragment
            return dialogFragment.dialog as AlertDialog
        }

        fun clickButton(activity: FragmentActivity, which: Int) {
            shownDialog(activity).getButton(which).performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
