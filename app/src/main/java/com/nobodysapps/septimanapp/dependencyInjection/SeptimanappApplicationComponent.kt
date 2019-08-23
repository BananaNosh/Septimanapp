package com.nobodysapps.septimanapp.dependencyInjection

import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SharedPreferencesModule::class])
interface SeptimanappApplicationComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(horariumFragment: HorariumFragment)
}