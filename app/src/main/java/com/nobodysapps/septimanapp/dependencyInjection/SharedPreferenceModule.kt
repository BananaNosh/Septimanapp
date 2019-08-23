package com.nobodysapps.septimanapp.dependencyInjection

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule(private val context: Context) {
    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    }
}