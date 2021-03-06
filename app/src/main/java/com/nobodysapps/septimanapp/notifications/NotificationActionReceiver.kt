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
 * Receiver for handling broadcasts from notification actions
 */
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var enrolInformationStorage: EnrolInformationStorage

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)
        Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")
        if (context != null && intent != null && intent.action != null) {
            @Suppress("CascadeIf")
            if (intent.action!!.equals(context.getString(R.string.action_mark_already_enrolled), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                enrolInformationStorage.saveEnrolState(EnrolInformationStorage.ENROLLED_STATE_ENROLLED)
                Log.d(TAG, "ALREADY_ENROLLED clicked")
            } else if (intent.action!!.equals(context.getString(R.string.action_remind_later), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                Log.d(TAG, "LATER clicked")
            } else if (intent.action!!.equals(context.getString(R.string.action_do_not_ask_again), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                enrolInformationStorage.saveEnrolState(EnrolInformationStorage.ENROLLED_STATE_NOT_ASK_AGAIN)
                Log.d(TAG, "Not ask again clicked")
            }
        }
    }


    companion object {
        private val TAG = NotificationActionReceiver::class.java.simpleName
    }

}