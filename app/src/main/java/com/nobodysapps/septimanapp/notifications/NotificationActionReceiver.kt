package com.nobodysapps.septimanapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.application.SeptimanappApplication
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment

/**
 * Receiver for handling broadcasts from notification actions
 */
class NotificationActionReceiver : BroadcastReceiver() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action != null) {
            val septimanappApplication = context.applicationContext as SeptimanappApplication
            notificationHelper = septimanappApplication.notificationHelper  // TODO inject
            sharedPreferences = septimanappApplication.sharedPreferences // TODO inject

            @Suppress("CascadeIf")
            if (intent.action!!.equals(context.getString(R.string.action_mark_already_enrolled), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                sharedPreferences.edit().putInt(EnrolmentFragment.ENROLLED_STATE_KEY, EnrolmentFragment.ENROLLED_STATE_ENROLLED).apply()
                Log.d(TAG, "ALREADY_ENROLLED clicked")
                // TODO maybe delete notifications
            } else if (intent.action!!.equals(context.getString(R.string.action_remind_later), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                Log.d(TAG, "LATER clicked")
            } else if (intent.action!!.equals(context.getString(R.string.action_do_not_ask_again), ignoreCase = true)) {
                notificationHelper.cancelNotification(NotificationHelper.ENROL_REMINDER_NOTIFICATION_ID)
                sharedPreferences.edit().putInt(EnrolmentFragment.ENROLLED_STATE_KEY, EnrolmentFragment.ENROLLED_STATE_NOT_ASK_AGAIN).apply()
                Log.d(TAG, "Not ask again clicked")
                // TODO maybe delete notifications
            }
        }
    }


    companion object {
        private val TAG = NotificationActionReceiver::class.java.simpleName
    }

}