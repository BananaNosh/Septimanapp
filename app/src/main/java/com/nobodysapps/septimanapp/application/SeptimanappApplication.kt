package com.nobodysapps.septimanapp.application

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.dependencyInjection.ContextModule
import com.nobodysapps.septimanapp.dependencyInjection.DaggerSeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import com.nobodysapps.septimanapp.model.storage.TimeStorage
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import com.testfairy.TestFairy
import java.util.*
import javax.inject.Inject

const val VERSION_ALREADY_RUN_ON = "run_version"
val ALLOWED_HORARIUM_LOCALES = listOf("la", "de")

class SeptimanappApplication : Application() {
    lateinit var component: SeptimanappApplicationComponent

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var notificationHelper: NotificationHelper
    @Inject
    lateinit var alarmScheduler: AlarmScheduler
    @Inject
    lateinit var horariumStorage: HorariumStorage
    @Inject
    lateinit var locationStorage: LocationStorage
    @Inject
    lateinit var timeStorage: TimeStorage

    override fun onCreate() {
        super.onCreate()

        TestFairy.begin(this, "SDK-wiO2TKkT")

        val sharedPreferencesModule = SharedPreferencesModule()
        val contextModule = ContextModule(applicationContext)
        component = DaggerSeptimanappApplicationComponent.builder()
            .contextModule(contextModule)
            .sharedPreferencesModule(sharedPreferencesModule)
            .build()

        component.inject(this)

        checkForFirstStart()

        notificationHelper.createNotificationChannel(
            NotificationManagerCompat.IMPORTANCE_DEFAULT, true,
            getString(R.string.app_name), "App notification channel."
        )

        setupReminder()

        // TODO reset Enrol state after septimana
    }

    private fun setupReminder() {
        val today =
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }
        val septimanaStartTime = timeStorage.loadSeptimanaStartEndTime()?.first
        septimanaStartTime?.let { startTime ->
            val reminderTimes = NotificationHelper.ENROL_REMINDER_TIMES
            reminderTimes.forEachIndexed { i, reminderTime ->
                val reminderDate = (startTime.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, -reminderTime.second)
                    add(Calendar.MONTH, -reminderTime.first)
                }
                if (reminderDate.after(today) || i == reminderTimes.size - 1) { // if in past only send one reminder
                    alarmScheduler.scheduleAlarm(
                        reminderDate, notificationHelper.pendingIntentForEnrolReminder()
                    )
                }
            }
        }
    }

    private fun checkForFirstStart() {
        @Suppress("DEPRECATION")
        val appVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
        Log.d("SeptimanappApplication", "version $appVersion")
        // true if first time run for current app version
        val isFirstRunForVersion =
            sharedPreferences.getLong(VERSION_ALREADY_RUN_ON, 0) != appVersion
        if (isFirstRunForVersion) {
            sharedPreferences.edit().putLong(VERSION_ALREADY_RUN_ON, appVersion).apply()
            doOnFirstStartOfVersion()
        }
    }

    private fun doOnFirstStartOfVersion() {
        Log.d(TAG, "First run")
        loadHoraria()
        loadLocations()
        loadSeptimanaStartEndTime()
    }

    private fun loadHoraria() {
        val fileList = assets.list("")
        fileList?.forEach {
            if (it.startsWith("horarium_") && it.endsWith(".json")) {
                val yearAndLocaleString = it.substringAfter("_").substringBefore(".")
                val yearString = yearAndLocaleString.substringBefore("_")
                val locale = yearAndLocaleString.substringAfter("_")
                if (locale in ALLOWED_HORARIUM_LOCALES) {
                    val year: Int = try {
                        yearString.toInt()
                    } catch (e: NumberFormatException) {
                        Log.d(TAG, "Wrong filename $it")
                        0
                    }
                    assets.open(it).bufferedReader().use { reader ->
                        val horariumJson = reader.readText()
                        horariumStorage.saveHorarium(horariumJson, year, locale)
                    }
                }
            }
        }
    }

    private fun loadLocations() {
        val fileList = assets.list("")
        fileList?.forEach {
            if (it.startsWith("locations") && it.endsWith(".json")) {
                val overallLocation = it.substringAfter("_").substringBefore(".")
                Log.d(TAG, "overallLocation is $overallLocation")
                assets.open(it).bufferedReader().use { reader ->
                    val locationJson = reader.readText()
                    locationStorage.saveLocations(locationJson, overallLocation)
                }
            }
        }
    }

    private fun loadSeptimanaStartEndTime() {
        val startTime = Calendar.getInstance()
        startTime.set(2020, 6, 25, 16, 30)
        val endTime = startTime.clone() as Calendar
        endTime.set(2020, 7, 1, 14, 0)
        timeStorage.saveSeptimanaStartEndTime(startTime, endTime)
    }

    companion object {
        const val TAG = "SeptimanappApplication"
    }
}