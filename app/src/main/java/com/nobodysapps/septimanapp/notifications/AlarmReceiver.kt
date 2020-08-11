package com.nobodysapps.septimanapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Called on alarm, creates and shows the notification
 */
class AlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var enrolInformationStorage: EnrolInformationStorage
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")
        if (context != null && intent != null && intent.action != null) {
            val enrolState = enrolInformationStorage.loadEnrolState()
            if (intent.action!!.equals(
                    context.getString(R.string.action_notify_enrol_reminder),
                    ignoreCase = true
                )
            ) {
                Log.d(TAG, "enrollState: $enrolState")
                if (enrolState == EnrolInformationStorage.ENROLLED_STATE_REMIND) {
                    notificationHelper.createNotificationEnrolReminder()    // sends EnrolReminder
                } else if (enrolState == EnrolInformationStorage.ENROLLED_STATE_IN_PROGRESS) { // TODO check if should be removed
                    notificationHelper.createNotificationContinueEnrolReminder()    // sends ContinueEnrolReminder because enrollment allready started
                }
            } else if (intent.action!!.equals(
                    context.getString(R.string.action_notify_continue_enrol_reminder),
                    ignoreCase = true
                )
            ) {
                if (enrolState == EnrolInformationStorage.ENROLLED_STATE_IN_PROGRESS) {
                    notificationHelper.createNotificationContinueEnrolReminder()   // sends ContinueEnrolReminder
                }
            }
        }
    }


    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }
}