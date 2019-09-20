package com.nobodysapps.septimanapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.application.SeptimanappApplication
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")
        if (context != null && intent != null && intent.action != null) {
            val septimanappApplication = context.applicationContext as SeptimanappApplication
            notificationHelper = septimanappApplication.notificationHelper  // TODO inject
            sharedPreferences = septimanappApplication.sharedPreferences // TODO inject
            if (intent.action!!.equals(
                    context.getString(R.string.action_notify_enrol_reminder),
                    ignoreCase = true
                )
            ) {
                val enrolState = sharedPreferences.getInt(
                    EnrolmentFragment.ENROLLED_STATE_KEY,
                    EnrolmentFragment.ENROLLED_STATE_REMIND
                )
                if (enrolState == EnrolmentFragment.ENROLLED_STATE_REMIND) {
                    notificationHelper.createNotificationEnrolReminder()
                }
            }
        }
    }


    companion object {
        private val TAG = AlarmReceiver::class.java.simpleName
    }
}