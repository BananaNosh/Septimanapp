package com.nobodysapps.septimanapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.application.SeptimanappApplication

class AlarmReceiver : BroadcastReceiver() {

    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")
        if (context != null && intent != null && intent.action != null) {
            notificationHelper = (context.applicationContext as SeptimanappApplication).notificationHelper  // TODO inject
            if (intent.action!!.equals(
                    context.getString(R.string.action_notify_enrol_reminder),
                    ignoreCase = true
                )
            ) {
                notificationHelper.createNotificationEnrolReminder()
            }
        }
    }


    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }
}