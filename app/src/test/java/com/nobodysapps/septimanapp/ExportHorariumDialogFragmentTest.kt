package com.nobodysapps.septimanapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.nobodysapps.septimanapp.dialog.ExportHorariumDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * The export dialog has to deliver the chosen option even after it was recreated, e.g. on rotation,
 * when a listener set on the old instance would be lost.
 */
@RunWith(RobolectricTestRunner::class)
class ExportHorariumDialogFragmentTest {

    @Before
    fun setup() {
        HostFragment.results.clear()
    }

    @Test
    fun positiveButtonExportsWithReminders() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        showDialog(controller.get())

        clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(true), HostFragment.results)
    }

    @Test
    fun negativeButtonExportsWithoutReminders() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        showDialog(controller.get())

        clickButton(controller.get(), DialogInterface.BUTTON_NEGATIVE)

        assertEquals(listOf(false), HostFragment.results)
    }

    @Test
    fun choiceIsDeliveredAfterRecreation() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        showDialog(controller.get())

        controller.recreate()
        clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(true), HostFragment.results)
    }

    private fun showDialog(activity: FragmentActivity) {
        val host = HostFragment()
        activity.supportFragmentManager.beginTransaction().add(host, HOST_TAG).commitNow()
        ExportHorariumDialogFragment().show(host.childFragmentManager, DIALOG_TAG)
        host.childFragmentManager.executePendingTransactions()
    }

    private fun clickButton(activity: FragmentActivity, which: Int) {
        val host = activity.supportFragmentManager.findFragmentByTag(HOST_TAG) as HostFragment
        val dialogFragment =
            host.childFragmentManager.findFragmentByTag(DIALOG_TAG) as ExportHorariumDialogFragment
        (dialogFragment.dialog as AlertDialog).getButton(which).performClick()
        shadowOf(Looper.getMainLooper()).idle()
    }

    /** Stands in for the HorariumFragment, which registers the same listener in onCreate. */
    class HostFragment : Fragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            childFragmentManager.setFragmentResultListener(
                ExportHorariumDialogFragment.REQUEST_KEY, this
            ) { _, result ->
                results.add(ExportHorariumDialogFragment.withReminders(result))
            }
        }

        companion object {
            // Static, because recreation replaces the fragment instance.
            val results = ArrayList<Boolean>()
        }
    }

    companion object {
        private const val HOST_TAG = "host"
        private const val DIALOG_TAG = "dialog"
    }
}
