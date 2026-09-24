package com.nobodysapps.septimanapp

import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import com.nobodysapps.septimanapp.dialog.ExportHorariumDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * The export dialog has to deliver the chosen option even after it was recreated, e.g. on rotation,
 * when a listener set on the old instance would be lost.
 */
@RunWith(RobolectricTestRunner::class)
class ExportHorariumDialogFragmentTest {

    @Before
    fun setup() {
        DialogResultHost.reset(ExportHorariumDialogFragment.REQUEST_KEY)
    }

    @Test
    fun positiveButtonExportsWithReminders() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ExportHorariumDialogFragment())

        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(true), withRemindersResults())
    }

    @Test
    fun negativeButtonExportsWithoutReminders() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ExportHorariumDialogFragment())

        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_NEGATIVE)

        assertEquals(listOf(false), withRemindersResults())
    }

    @Test
    fun choiceIsDeliveredAfterRecreation() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ExportHorariumDialogFragment())

        controller.recreate()
        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(true), withRemindersResults())
    }

    private fun withRemindersResults() =
        DialogResultHost.results.map { (_, result) -> ExportHorariumDialogFragment.withReminders(result) }
}
