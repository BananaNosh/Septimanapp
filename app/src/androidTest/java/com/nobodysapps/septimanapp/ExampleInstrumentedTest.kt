package com.nobodysapps.septimanapp


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class ExampleInstrumentedTest {

    @Before
    fun setUp() {
//        val app = RuntimeEnvironment.application as ExampleApplication
//        app.setLocationProvider(mockLocationProvider)
    }

    @Test
    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getTargetContext()
//        assertEquals("com.nobodysapps.septimanapp", appContext.packageName)
    }
}
