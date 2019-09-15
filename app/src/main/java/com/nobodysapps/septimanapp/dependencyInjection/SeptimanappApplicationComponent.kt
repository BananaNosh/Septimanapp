package com.nobodysapps.septimanapp.dependencyInjection

import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.application.SeptimanappApplication
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, SharedPreferencesModule::class])
@SeptimanappApplicationScope
interface SeptimanappApplicationComponent {
    fun inject(application: SeptimanappApplication)
    fun inject(mainActivity: MainActivity)
    fun inject(horariumFragment: HorariumFragment)
}