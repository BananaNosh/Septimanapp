package com.nobodysapps.septimanapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import java.util.*
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context) {

    /**
     * Schedules a single alarm
     */
    fun scheduleAlarm(alarmTime: Calendar, alarmIntent: PendingIntent?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, alarmTime.timeInMillis, alarmIntent)
    }

    fun scheduleAlarmIfNotInPast(alarmTime: Calendar, alarmIntent: PendingIntent?) {
        if (alarmTime.after(Calendar.getInstance())) {
            scheduleAlarm(alarmTime, alarmIntent)
        }
    }
}