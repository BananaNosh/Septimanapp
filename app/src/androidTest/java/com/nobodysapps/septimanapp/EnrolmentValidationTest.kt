package com.nobodysapps.septimanapp

import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the required-field handling of the enrolment form: tapping "send" with
 * required fields missing must keep the user on the form and tell them, in the snackbar, exactly
 * which fields are still missing (see [EnrolmentFragment] /
 * [com.nobodysapps.septimanapp.model.EnrolInformation.isValid]).
 *
 * These drive the views directly on the UI thread rather than through Espresso: Espresso's input
 * injection (and even `onView`) crash on Android 16, so [performClick]/[setText] are used instead.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class EnrolmentValidationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext

    // launchActivity = false: each test launches with an intent that deep-links to the enrolment form.
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Before
    fun startFromEmptyForm() {
        // General app state lives in the "pref" SharedPreferences, the enrolment data in the
        // encrypted "enrol_prefs" file (see SharedPreferencesModule). Clear both so every test
        // starts from a blank form — the enrolment prefs through the encrypted instance, so the
        // Tink keysets survive. Pre-set the "language dialog shown" flag so the first-run
        // ChooseLanguageDialog does not cover the form.
        val plainPrefs = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
        plainPrefs.edit()
            .clear()
            .putBoolean("language_dialog_shown", true)
            .commit()
        SharedPreferencesModule().provideEnrolSharedPreferences(context, plainPrefs)
            .edit().clear().commit()
    }

    private fun launchEnrolmentForm(): MainActivity {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.FRAGMENT_TO_LOAD_KEY, EnrolmentFragment::class.java)
        val activity = activityRule.launchActivity(intent)
        instrumentation.waitForIdleSync()
        return activity
    }

    private fun MainActivity.setFieldText(id: Int, text: String) {
        // setText fires the field's TextWatcher, which persists the value just like real typing.
        instrumentation.runOnMainSync { findViewById<EditText>(id).setText(text) }
    }

    private fun MainActivity.clickSend() {
        instrumentation.runOnMainSync { findViewById<android.view.View>(R.id.fabEnrolSend).performClick() }
    }

    /** Polls the activity's view tree for the Material snackbar text, which shows asynchronously. */
    private fun MainActivity.awaitSnackbarText(timeoutMs: Long = 3000): String {
        val deadline = System.currentTimeMillis() + timeoutMs
        val holder = arrayOfNulls<String>(1)
        while (System.currentTimeMillis() < deadline) {
            instrumentation.runOnMainSync {
                holder[0] = window.decorView
                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    ?.text?.toString()
            }
            if (!holder[0].isNullOrEmpty()) return holder[0]!!
            Thread.sleep(50)
        }
        return holder[0] ?: ""
    }

    @Test
    fun emptyForm_listsMissingRequiredFieldsInSnackbar() {
        val activity = launchEnrolmentForm()

        activity.clickSend()
        val snackbar = activity.awaitSnackbarText()

        // A representative spread of the required fields, including the new "age" field and the two
        // consent selections, must be named in the snackbar.
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_last_name_hint)))
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_mail_hint)))
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_age_label)))
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_field_image_consent)))
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_field_address_consent)))
    }

    @Test
    fun filledFields_dropOutOfTheMissingList() {
        val activity = launchEnrolmentForm()

        activity.setFieldText(R.id.enrolNameEdit, "Mustermann")
        activity.setFieldText(R.id.enrolFirstameEdit, "Max")
        activity.setFieldText(R.id.enrolAgeEdit, "25")

        activity.clickSend()
        val snackbar = activity.awaitSnackbarText()

        // The filled fields are no longer reported; the still-empty ones still are.
        assertFalse("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_last_name_hint)))
        assertFalse("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_age_label)))
        assertTrue("snackbar was: '$snackbar'", snackbar.contains(activity.getString(R.string.enrol_mail_hint)))
    }
}
