package com.nobodysapps.septimanapp.dependencyInjection

import android.content.Context
import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import com.nobodysapps.septimanapp.notifications.AlarmReceiver
import com.nobodysapps.septimanapp.notifications.NotificationActionReceiver
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.BINARY)
annotation class SeptimanappApplicationScope

@Module
class ContextModule(private val context: Context) {
    @Provides
    @SeptimanappApplicationScope
    fun provideContext() = context
}

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivityInjector(): MainActivity
}

@Module
abstract class HorariumFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeHorariumFragmentInjector(): HorariumFragment
}

@Module
abstract class EnrolFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeEnrolmentFragmentInjector(): EnrolmentFragment
}

@Module
abstract class MapFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeMapFragmentInjector(): MapFragment
}

@Module
abstract class BroadcastReceiverModule {
    @ContributesAndroidInjector
    abstract fun contributeAlarmReceiverInjector(): AlarmReceiver

    @ContributesAndroidInjector
    abstract fun contributeNotificationActionReceiverInjector(): NotificationActionReceiver
}

