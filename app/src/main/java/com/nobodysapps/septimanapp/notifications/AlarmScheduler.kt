package com.nobodysapps.septimanapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import java.util.*
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context) {

    /**
     * Schedules a single alarm
     */
    fun scheduleAlarm(alarmTime: Calendar, alarmIntent: PendingIntent?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, alarmTime.timeInMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent)
    }
}