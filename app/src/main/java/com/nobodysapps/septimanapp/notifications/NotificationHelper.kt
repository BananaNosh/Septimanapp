package com.nobodysapps.septimanapp.notifications

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(private val context: Context) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    /**
     * Sets up the notification channels for API 26+.
     * Note: This uses package name + channel name to create unique channelId's.
     *
     * @param importance  importance level for the notificaiton channel
     * @param showBadge   whether the channel should have a notification badge
     * @param name        name for the notification channel
     * @param description description for the notification channel
     */
    fun createNotificationChannel(
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = channelIdForName(name)
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun channelIdForName(name: String) = "${context.packageName}-$name"


    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    /**
     * Creates a notification to remind to enrol
     */
    fun createNotificationEnrolReminder() {

//        // create a group notification
//        val groupBuilder = buildGroupNotification(context, reminderData)

        val notificationBuilder = buildNotificationEnrolReminder()

        // add an action to set "already enrolled" to the notification
        val alreadyEnrolledIntent = createPendingIntent(
            context.getString(R.string.action_mark_already_enrolled),
            NotificationActionReceiver::class.java
        )
        val remindLaterIntent = createPendingIntent(
            context.getString(R.string.action_remind_later),
            NotificationActionReceiver::class.java
        )
        val doNotAskAgainIntent = createPendingIntent(
            context.getString(R.string.action_do_not_ask_again),
            NotificationActionReceiver::class.java
        )
        notificationBuilder
            .addAction(R.drawable.ic_clear, "Nicht fragen", doNotAskAgainIntent)
            .addAction(R.drawable.ic_check, "Schon passiert", alreadyEnrolledIntent)
            .addAction(R.drawable.ic_clock, "Später", remindLaterIntent)

        // call notify for both the group and the pet notification
//        notificationManager.notify(reminderData.type.ordinal, groupBuilder.build())
        notificationManager.notify(ENROL_REMINDER_NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * Builds and returns the NotificationCompat.Builder for the enrol reminder notification
     */
    private fun buildNotificationEnrolReminder(): NotificationCompat.Builder {
        val channelId = channelIdForName(context.getString(R.string.app_name))

        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.mipmap.ic_assignment)
            setContentTitle(context.getString(R.string.notification_enrol_title))
            setAutoCancel(true)
//            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText(context.getString(R.string.notification_enrol_text))


            // Launches the app to open the MainActivity with EnrolFragment
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    createEnrolFragmentIntent(),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            setContentIntent(pendingIntent)
        }
    }

    /**
     * Creates a notification to remind to continue enrollment
     */
    fun createNotificationContinueEnrolReminder() {

//        // create a group notification
//        val groupBuilder = buildGroupNotification(context, reminderData)

        val notificationBuilder = buildNotificationContinueEnrolReminder()

        // call notify for both the group and the pet notification
//        notificationManager.notify(reminderData.type.ordinal, groupBuilder.build())
        notificationManager.notify(
            CONTINUE_ENROL_REMINDER_NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    /**
     * Builds and returns the NotificationCompat.Builder for the enrol reminder notification
     */
    private fun buildNotificationContinueEnrolReminder(): NotificationCompat.Builder {
        val channelId = channelIdForName(context.getString(R.string.app_name))

        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.mipmap.ic_assignment)
            setContentTitle(context.getString(R.string.notification_enrol_title))
            setAutoCancel(true)
//            setLargeIcon(BitmapFactory.decodeResource(context.resources, drawable))
            setContentText(context.getString(R.string.notification_continue_enrol_text))

            // Launches the app to open the MainActivity with EnrolFragment
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    createEnrolFragmentIntent(),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            setContentIntent(pendingIntent)
        }
    }

    private fun createEnrolFragmentIntent() = Intent(context, MainActivity::class.java).apply {
        flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // TODO check flags
        putExtra(MainActivity.FRAGMENT_TO_LOAD_KEY, EnrolmentFragment::class.java)
    }

    fun pendingIntentForEnrolReminder(): PendingIntent? {
        return createPendingIntent(
            context.getString(R.string.action_notify_enrol_reminder),
            AlarmReceiver::class.java
        )
    }

    private fun createPendingIntent(action: String, receiverClass: Class<*>): PendingIntent? {
        val intent = Intent(context.applicationContext, receiverClass).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val ENROL_REMINDER_NOTIFICATION_ID = 1
        const val CONTINUE_ENROL_REMINDER_NOTIFICATION_ID = 2

        val ENROL_REMINDER_DATES = listOf(Pair(6, 1), Pair(7, 1), Pair(8, 1))
    }
}