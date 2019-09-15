package com.nobodysapps.septimanapp.activity


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import com.nobodysapps.septimanapp.notifications.NotificationHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import javax.inject.Inject


class MainActivity : SeptimanappActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        getSeptimanappApplication().component.inject(this)

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
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
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
        // Handle navigation view item clicks here.
        var fragmentClass: Class<*> = HorariumFragment::class.java
        when (item.itemId) {
            R.id.nav_horarium -> {
                fragmentClass = HorariumFragment::class.java
            }
            R.id.nav_gallery -> {
                Toast.makeText(this, "Hallo ich bin gallery", Toast.LENGTH_LONG).show()
            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
            R.id.nav_map -> {
                fragmentClass = MapFragment::class.java
            }
            R.id.nav_enrol -> {
                fragmentClass = EnrolmentFragment::class.java
            }
        }
        if (!goToFragment(fragmentClass)) return false
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
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                android.R.anim.slide_in_left, android.R.anim.slide_out_right
            )
            .replace(R.id.fragment_layout, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    companion object {
        const val TAG = "MainActivity"

        const val FRAGMENT_TO_LOAD_KEY = "fragmentToLoad"
    }
}
