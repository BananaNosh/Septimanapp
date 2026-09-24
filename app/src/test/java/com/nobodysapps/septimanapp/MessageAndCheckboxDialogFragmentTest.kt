package com.nobodysapps.septimanapp

import android.content.DialogInterface
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import com.nobodysapps.septimanapp.dialog.ConfirmEnrolmentDialogFragment
import com.nobodysapps.septimanapp.dialog.MessageAndCheckboxDialogFragment
import com.nobodysapps.septimanapp.dialog.OutdatedHorariumDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * The checkbox dialogs have to deliver OK (and the checkbox state) even after they were recreated,
 * e.g. on rotation, when a listener set on the old instance would be lost.
 */
@RunWith(RobolectricTestRunner::class)
class MessageAndCheckboxDialogFragmentTest {

    @Before
    fun setup() {
        DialogResultHost.reset(
            OutdatedHorariumDialogFragment.REQUEST_KEY, ConfirmEnrolmentDialogFragment.REQUEST_KEY
        )
    }

    @Test
    fun outdatedHorariumOkDeliversUncheckedCheckbox() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), OutdatedHorariumDialogFragment())

        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(OutdatedHorariumDialogFragment.REQUEST_KEY to false), checkedResults())
    }

    @Test
    fun outdatedHorariumCheckedStateIsDeliveredAfterRecreation() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), OutdatedHorariumDialogFragment())
        checkBox(controller.get(), R.id.dialogCB).isChecked = true

        controller.recreate()
        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(OutdatedHorariumDialogFragment.REQUEST_KEY to true), checkedResults())
    }

    @Test
    fun confirmEnrolmentOkIsOnlyEnabledWithBothCheckboxes() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ConfirmEnrolmentDialogFragment())
        val okButton = DialogResultHost.shownDialog(controller.get())
            .getButton(DialogInterface.BUTTON_POSITIVE)

        assertFalse(okButton.isEnabled)
        checkBox(controller.get(), R.id.dialogCB).isChecked = true
        assertFalse(okButton.isEnabled)
        checkBox(controller.get(), R.id.dialogCB2).isChecked = true
        assertTrue(okButton.isEnabled)
    }

    @Test
    fun confirmEnrolmentIsDeliveredAfterRecreation() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ConfirmEnrolmentDialogFragment())
        checkBox(controller.get(), R.id.dialogCB).isChecked = true
        checkBox(controller.get(), R.id.dialogCB2).isChecked = true

        controller.recreate()
        val okButton = DialogResultHost.shownDialog(controller.get())
            .getButton(DialogInterface.BUTTON_POSITIVE)
        // The restored checkboxes are still checked, so OK must not be disabled again.
        assertTrue(okButton.isEnabled)
        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_POSITIVE)

        assertEquals(listOf(ConfirmEnrolmentDialogFragment.REQUEST_KEY to true), checkedResults())
    }

    @Test
    fun confirmEnrolmentCancelDeliversNothing() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        DialogResultHost.show(controller.get(), ConfirmEnrolmentDialogFragment())

        DialogResultHost.clickButton(controller.get(), DialogInterface.BUTTON_NEGATIVE)

        assertTrue(DialogResultHost.results.isEmpty())
    }

    private fun checkBox(activity: FragmentActivity, id: Int) =
        DialogResultHost.shownDialog(activity).findViewById<CheckBox>(id)

    private fun checkedResults() = DialogResultHost.results.map { (key, result) ->
        key to MessageAndCheckboxDialogFragment.isChecked(result)
    }
}
