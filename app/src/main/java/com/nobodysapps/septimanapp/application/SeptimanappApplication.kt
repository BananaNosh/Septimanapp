package com.nobodysapps.septimanapp.application

import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDexApplication
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.dependencyInjection.ContextModule
import com.nobodysapps.septimanapp.dependencyInjection.DaggerSeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule
import com.nobodysapps.septimanapp.model.storage.*
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.util.*
import javax.inject.Inject

const val VERSION_ALREADY_RUN_ON = "run_version"
val ALLOWED_HORARIUM_LOCALES = listOf("la", "de")

class SeptimanappApplication : MultiDexApplication(), HasAndroidInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

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
    lateinit var eventInfoStorage: EventInfoStorage
    @Inject
    lateinit var storageManager: StorageManager

    override fun onCreate() {
        super.onCreate()

        val sharedPreferencesModule = SharedPreferencesModule()
        val contextModule = ContextModule(applicationContext)
        DaggerSeptimanappApplicationComponent.builder()
            .contextModule(contextModule)
            .sharedPreferencesModule(sharedPreferencesModule)
            .build().inject(this)

        checkForFirstStart()

        notificationHelper.createNotificationChannel(
            NotificationManagerCompat.IMPORTANCE_DEFAULT, true,
            getString(R.string.app_name), "App notification channel."
        )

        resetAfterSeptimana()
        setupReminder()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    private fun resetAfterSeptimana() {
        storageManager.resetAfterSeptimana()
    }

    private fun setupReminder() {
        val today =
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }
        val septimanaStartTime = eventInfoStorage.loadSeptimanaStartEndTime()?.first
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
            doOnFirstStartOfVersion()
            sharedPreferences.edit().putLong(VERSION_ALREADY_RUN_ON, appVersion).apply()
        }
    }

    private fun doOnFirstStartOfVersion() {
        Log.d(TAG, "First run")
        loadHoraria()
        loadLocations()
        storeEventInfo()
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
                    val septimanaLocation = SeptimanaLocation.fromKeyOrNull(overallLocation)
                    septimanaLocation?.let {
                        locationStorage.saveLocations(locationJson, septimanaLocation)
                    }
                }
            }
        }
    }

    private fun storeEventInfo() {
        val startTime = Calendar.getInstance()
        startTime.set(2023, 6, 29, 16, 0)
        val endTime = startTime.clone() as Calendar
        endTime.set(2023, 7, 5, 14, 0)
        eventInfoStorage.saveSeptimanaStartEndTime(startTime, endTime)
        eventInfoStorage.saveSeptimanaLocation(SeptimanaLocation.BRAUNFELS)
    }

    companion object {
        const val TAG = "SeptimanappApplication"
    }
}