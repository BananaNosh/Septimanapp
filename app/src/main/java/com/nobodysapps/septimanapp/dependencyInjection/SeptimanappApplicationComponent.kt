package com.nobodysapps.septimanapp.dependencyInjection

import com.nobodysapps.septimanapp.application.SeptimanappApplication
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    ViewModelModule::class,
    MainActivityModule::class,
    HorariumFragmentModule::class,
    EnrolFragmentModule::class,
    MapFragmentModule::class,
    BroadcastReceiverModule::class,
    ContextModule::class,
    SharedPreferencesModule::class])
@SeptimanappApplicationScope
interface SeptimanappApplicationComponent {
    fun inject(application: SeptimanappApplication)
}