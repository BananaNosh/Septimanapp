package com.nobodysapps.septimanapp.application

import android.app.Application
import com.nobodysapps.septimanapp.dependencyInjection.DaggerSeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SeptimanappApplicationComponent
import com.nobodysapps.septimanapp.dependencyInjection.SharedPreferencesModule

class SeptimanappApplication : Application() {
    lateinit var component: SeptimanappApplicationComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerSeptimanappApplicationComponent.builder()
            .sharedPreferencesModule(SharedPreferencesModule(applicationContext))
            .build()
    }
}