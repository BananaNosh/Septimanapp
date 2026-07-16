package com.nobodysapps.septimanapp.dependencyInjection

import javax.inject.Qualifier

/**
 * Qualifies the encrypted [android.content.SharedPreferences] instance that holds the
 * enrolment data (personal information), as opposed to the default plaintext preferences.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EnrolPrefs
