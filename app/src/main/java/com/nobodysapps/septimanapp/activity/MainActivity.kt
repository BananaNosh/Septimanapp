package com.nobodysapps.septimanapp.activity


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import com.nobodysapps.septimanapp.model.storage.TimeStorage
import com.nobodysapps.septimanapp.view.CountDownView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import javax.inject.Inject


class MainActivity : SeptimanappActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var timeStorage: TimeStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        AndroidInjection.inject(this)

        if (savedInstanceState == null) {
            replaceFragment(HorariumFragment::class.java)
        }

        val fragmentToGo: Class<*>? = intent.getSerializableExtra(FRAGMENT_TO_LOAD_KEY) as Class<*>?
        if (fragmentToGo != null) {
            replaceFragment(fragmentToGo)
        }

        val drawerLayout: DrawerLayout = drawer_layout
        val navView: NavigationView = nav_view
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        setupDrawerListenerForCountDown(drawerLayout)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    private fun setupDrawerListenerForCountDown(drawerLayout: DrawerLayout) {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (!countDownTV.started) {
                    val (septimanaStartTime, _) = timeStorage.loadSeptimanaStartEndTime() ?: Pair(
                        null, null)
                    if (septimanaStartTime == null || septimanaStartTime.before(Calendar.getInstance())) {
                        countDownTV.visibility = View.GONE
                        countDownSubTV.visibility = View.GONE
                    } else {
                        countDownTV.visibility = View.VISIBLE
                        countDownSubTV.visibility = View.VISIBLE
                        countDownTV.setEndTime(septimanaStartTime, object: CountDownView.Listener{
                            override fun onFinished() {
                                countDownTV.visibility = View.GONE
                                countDownSubTV.visibility = View.GONE
                            }

                        })
                        countDownTV.startTimer()
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                countDownTV.stopTimer()
            }

            override fun onDrawerOpened(drawerView: View) {}

        })
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount > 0 -> {
                val prevFragment =
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1)
                prevFragment.name?.let {
                    nav_view.setCheckedItem(it.toInt())
                }
                supportFragmentManager.popBackStack()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.isChecked) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return true
        }
        // Handle navigation view item clicks here.
        var fragmentClass: Class<*>? = null
        when (item.itemId) {
            R.id.nav_horarium -> {
                fragmentClass = HorariumFragment::class.java
            }
            R.id.nav_map -> {
                fragmentClass = MapFragment::class.java
            }
            R.id.nav_enrol -> {
                fragmentClass = EnrolmentFragment::class.java
            }
        }
        if (fragmentClass == null || !goToFragment(fragmentClass)) return false
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragmentToGo: Class<*>) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, fragmentToGo.newInstance() as Fragment).commit()
    }

    private fun goToFragment(fragmentClass: Class<*>): Boolean {
        var fragment: Fragment? = null
        try {
            fragment = (fragmentClass.newInstance() as Fragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (fragment == null) return false
        val currentNavItemId = nav_view.checkedItem?.itemId
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_layout, fragment)
            .addToBackStack(currentNavItemId?.toString())
            .commit()
        return true
    }

    companion object {
        const val TAG = "MainActivity"

        const val FRAGMENT_TO_LOAD_KEY = "fragmentToLoad"
    }
}
