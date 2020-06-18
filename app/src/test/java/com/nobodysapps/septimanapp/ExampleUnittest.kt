package com.nobodysapps.septimanapp

import android.os.Looper.getMainLooper
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.nobodysapps.septimanapp.activity.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem

@RunWith(RobolectricTestRunner::class)
//@Config(constants=BuildConfig::class)
@Config(sdk = [28])
class ExampleUnitTest {
    lateinit var activity: MainActivity
    @Before
    fun init(){
//        mainActivity = Robolectric.setupActivity(MainActivity::class.java)
//        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun clickingLoginButton_shouldStartSecondActivity(){
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            activity = it
            shadowOf(getMainLooper()).idle()
            activity.onNavigationItemSelected(RoboMenuItem(R.id.nav_enrol))
        }

//        mainActivity.onNavigationItemSelected(RoboMenuItem(R.id.nav_enrol))

//
//        val intent: Intent =showActivity.nextStartedActivity
//        val shadowIntent:ShadowIntent=shadowOf(intent)
//        assertNotNull(mainActivity.supportFragmentManager.findFragmentById(R.layout.fragment_enrolment))
//        assertThat(shadowIntent.intentClass.name, equalTo(MainActivity::class.java.getName()))
    }

//    @Test
//    fun checkTextViewString_presentOrNot(){
//        val textView=mainActivity.findViewById<TextView>(R.id.textView)
//        val stringValue=textView.text.toString()
//        assertThat(stringValue,equalTo("Hello World!"))
//    }

}