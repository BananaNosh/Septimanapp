package com.nobodysapps.septimanapp.dependencyInjection

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class SharedPreferencesModule {
    @Provides
    @SeptimanappApplicationScope
    @Inject
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    }
}