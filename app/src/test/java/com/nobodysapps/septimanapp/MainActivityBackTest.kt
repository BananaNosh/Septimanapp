package com.nobodysapps.septimanapp

import android.os.Looper.getMainLooper
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.nobodysapps.septimanapp.activity.MainActivity
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.fakes.RoboMenuItem
import java.time.Duration

/**
 * Since targetSdk 36 back gestures bypass onBackPressed() and go through the
 * OnBackPressedDispatcher, so these tests drive back through the dispatcher.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityBackTest {
    private lateinit var activity: MainActivity
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(getMainLooper()).idle()
        drawerLayout = activity.findViewById(R.id.drawer_layout)
        navView = activity.findViewById(R.id.nav_view)
    }

    @Test
    fun backClosesTheOpenDrawerInsteadOfFinishing() {
        drawerLayout.openDrawer(GravityCompat.START, false)
        shadowOf(getMainLooper()).idle()
        assertTrue(drawerLayout.isDrawerOpen(GravityCompat.START))

        pressBack()
        settleDrawerAnimation()

        assertFalse(drawerLayout.isDrawerOpen(GravityCompat.START))
        assertFalse(activity.isFinishing)
    }

    @Test
    fun backPopsToThePreviousScreenAndRestoresTheCheckedDrawerItem() {
        activity.onNavigationItemSelected(RoboMenuItem(R.id.nav_map))
        shadowOf(getMainLooper()).idle()
        assertTrue(shownFragment() is MapFragment)
        assertEquals(R.id.nav_map, navView.checkedItem?.itemId)

        pressBack()

        assertTrue(shownFragment() is HorariumFragment)
        assertEquals(R.id.nav_horarium, navView.checkedItem?.itemId)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun backOnTheFirstScreenWithClosedDrawerFinishes() {
        pressBack()

        assertTrue(activity.isFinishing)
    }

    private fun pressBack() {
        activity.onBackPressedDispatcher.onBackPressed()
        shadowOf(getMainLooper()).idle()
    }

    /**
     * closeDrawer() animates, and the animation only advances in DrawerLayout.computeScroll(),
     * which Robolectric never calls because nothing is drawn. Step it frame by frame instead.
     */
    private fun settleDrawerAnimation() {
        repeat(100) {
            shadowOf(getMainLooper()).idleFor(Duration.ofMillis(16))
            drawerLayout.computeScroll()
        }
    }

    private fun shownFragment() = activity.supportFragmentManager.findFragmentById(R.id.fragment_layout)
}
