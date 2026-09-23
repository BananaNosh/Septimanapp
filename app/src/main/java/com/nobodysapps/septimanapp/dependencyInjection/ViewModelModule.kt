package com.nobodysapps.septimanapp.dependencyInjection

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.nobodysapps.septimanapp.export.HorariumIcsExporter
import com.nobodysapps.septimanapp.model.storage.EnrolInformationStorage
import com.nobodysapps.septimanapp.model.storage.EventInfoStorage
import com.nobodysapps.septimanapp.model.storage.HorariumStorage
import com.nobodysapps.septimanapp.model.storage.LocationStorage
import com.nobodysapps.septimanapp.model.storage.PropositaStorage
import com.nobodysapps.septimanapp.notifications.AlarmScheduler
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import com.nobodysapps.septimanapp.utils.CalendarUtils
import com.nobodysapps.septimanapp.viewModel.EnrolmentViewModel
import com.nobodysapps.septimanapp.viewModel.HorariumViewModel
import com.nobodysapps.septimanapp.viewModel.MainViewModel
import com.nobodysapps.septimanapp.viewModel.PropositaViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider
import kotlin.reflect.KClass

@Module
class ViewModelModule {
    @Target(AnnotationTarget.FUNCTION)
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @MapKey
    internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

    @Provides
    fun provideViewModelFactory(providerMap: MutableMap<Class<out ViewModel>, Provider<ViewModel>>): ViewModelFactory {
        return ViewModelFactory(providerMap)
    }

    @Provides
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun provideMainViewModel(eventInfoStorage: EventInfoStorage, locationStorage: LocationStorage, calendarUtils: CalendarUtils): ViewModel {
        return MainViewModel(eventInfoStorage, locationStorage, calendarUtils)
    }

    @Provides
    @IntoMap
    @ViewModelKey(HorariumViewModel::class)
    fun provideHorariumViewModel(
        horariumStorage: HorariumStorage,
        sharedPreferences: SharedPreferences,
        eventInfoStorage: EventInfoStorage,
        locationStorage: LocationStorage,
        icsExporter: HorariumIcsExporter
    ): ViewModel {
        return HorariumViewModel(
            horariumStorage, sharedPreferences, eventInfoStorage, locationStorage, icsExporter
        )
    }

    @Provides
    @IntoMap
    @ViewModelKey(PropositaViewModel::class)
    fun providePropositaViewModel(propositaStorage: PropositaStorage): ViewModel {
        return PropositaViewModel(propositaStorage)
    }

    @Provides
    @IntoMap
    @ViewModelKey(EnrolmentViewModel::class)
    fun provideEnrolmentViewModel(
        enrolInformationStorage: EnrolInformationStorage,
        eventInfoStorage: EventInfoStorage,
        alarmScheduler: AlarmScheduler,
        notificationHelper: NotificationHelper
    ): ViewModel {
        return EnrolmentViewModel(
            enrolInformationStorage, eventInfoStorage, alarmScheduler, notificationHelper
        )
    }
}