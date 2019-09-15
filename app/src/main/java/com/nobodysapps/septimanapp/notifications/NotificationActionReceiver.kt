package com.nobodysapps.septimanapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nobodysapps.septimanapp.R

/**
 * Receiver for handling broadcasts from notification actions
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null && intent != null && intent.action != null) {

            if (intent.action!!.equals(context.getString(R.string.action_mark_already_enrolled), ignoreCase = true)) {
                // TODO maybe delete notifications
                // TODO mark as enrolled
            }
        }
    }

}