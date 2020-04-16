package com.nobodysapps.septimanapp.dependencyInjection

import android.content.Context
import dagger.Module
import dagger.Provides
import java.lang.annotation.RetentionPolicy
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.BINARY)
annotation class SeptimanappApplicationScope

@Module
abstract class SeptimanappApplicationModule {
//    @ContributesAndroidInjector
}

@Module
class ContextModule(private val context: Context) {
    @Provides
    @SeptimanappApplicationScope
    fun provideContext() = context
}