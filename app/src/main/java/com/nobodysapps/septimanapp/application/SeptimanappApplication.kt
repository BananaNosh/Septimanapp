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
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import java.lang.NumberFormatException
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

    override fun onCreate() {
        super.onCreate()
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

        alarmScheduler.scheduleAlarm(Calendar.getInstance().apply { set(Calendar.MINUTE, 41) },
            notificationHelper.pendingIntentForEnrolReminder())
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
    }

    private fun loadHoraria() {
        val fileList = assets.list("")
        fileList?.forEach {
            if (it.startsWith("horarium_") && it.endsWith(".json")) {
                val yearAndLocaleString = it.substringAfter("_").substringBefore(".")
                val yearString = yearAndLocaleString.substringBefore("_")
                val locale = yearAndLocaleString.substringAfter("_")
                if (locale in ALLOWED_HORARIUM_LOCALES) {
                    Log.d(TAG, "locale is $locale")
                    var year: Int
                    try {
                        year = yearString.toInt()
                    } catch (e: NumberFormatException) {
                        Log.d(TAG, "Wrong filename $it")
                        year = 0
                    }
                    assets.open(it).bufferedReader().use {
                        val horariumJson = it.readText()
                        sharedPreferences.edit().putString(
                            HorariumStorage.keyFromYearAndLocale(year, locale),
                            horariumJson
                        ).apply()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "SeptimanappApplication"
    }
}