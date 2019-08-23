package com.nobodysapps.septimanapp.application

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.nobodysapps.septimanapp.dependencyInjection.DaggerSeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import java.lang.NumberFormatException
import java.util.*

const val VERSION_ALREADY_RUN_ON = "run_version"

class SeptimanappApplication : Application() {
    lateinit var component: SeptimanappApplicationComponent

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        val sharedPreferencesModule = SharedPreferencesModule(applicationContext)
        component = DaggerSeptimanappApplicationComponent.builder()
            .sharedPreferencesModule(sharedPreferencesModule)
            .build()

        sharedPreferences = sharedPreferencesModule.provideSharedPreferences()
        val appVersion = packageManager.getPackageInfo(packageName, 0).versionName
        Log.d("SeptimanappApplication", "version $appVersion")
        // true if first time run for current app version
        val isFirstRunForVersion = sharedPreferences.getString(VERSION_ALREADY_RUN_ON, "") == appVersion
        if (true || isFirstRunForVersion) { //TODO
            sharedPreferences.edit().putString(VERSION_ALREADY_RUN_ON, appVersion).apply()
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
                val yearString = it.substringAfter("_").substringBefore(".")
                var year: Int
                try {
                    year = yearString.toInt()
                } catch (e: NumberFormatException) {
                    Log.d(TAG, "Wrong filename $it")
                    year = 0
                }
                assets.open(it).bufferedReader().use {
                    val horariumJson = it.readText()
                    val horariumDate = Calendar.getInstance()
                    horariumDate.set(year, 1, 1)
                    sharedPreferences.edit().putString(HorariumStorage.keyFromStartDate(horariumDate), horariumJson).apply()
                }
            }
        }
    }

    companion object {
        const val TAG = "SeptimanappApplication"
    }
}