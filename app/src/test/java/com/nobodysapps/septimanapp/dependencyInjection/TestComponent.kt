package com.nobodysapps.septimanapp.dependencyInjection

import com.nobodysapps.septimanapp.JsonConverterTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component
interface TestComponent {
    fun inject(jsonConverterTest: JsonConverterTest)
}