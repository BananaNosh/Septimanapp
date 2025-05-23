package com.nobodysapps.septimanapp.activity


import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.multidex.BuildConfig
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import com.nobodysapps.septimanapp.view.CountDownView
import com.nobodysapps.septimanapp.viewModel.MainViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.drawer_layout
import kotlinx.android.synthetic.main.activity_main.nav_view
import kotlinx.android.synthetic.main.app_bar_main.toolbar
import kotlinx.android.synthetic.main.content_main.main_layout
import kotlinx.android.synthetic.main.nav_header_main.countDownSubTV
import kotlinx.android.synthetic.main.nav_header_main.countDownTV
import kotlinx.android.synthetic.main.view_impressum.view.debugTV
import kotlinx.android.synthetic.main.view_impressum.view.privacyTV
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : SeptimanappActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

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

        navView.privacyTV.movementMethod = LinkMovementMethod.getInstance()

        if (BuildConfig.DEBUG) {
            navView.debugTV.visibility = View.VISIBLE
        }
    }

    private fun setupDrawerListenerForCountDown(drawerLayout: DrawerLayout) {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (!countDownTV.started) {
                    val septimanaStartTime = viewModel.septimanaStartTime
                    if (septimanaStartTime == null) {
                        countDownTV.visibility = View.GONE
                        countDownSubTV.visibility = View.GONE
                    } else {
                        countDownTV.visibility = View.VISIBLE
                        countDownSubTV.visibility = View.VISIBLE
                        countDownTV.setEndTime(septimanaStartTime, object : CountDownView.Listener {
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

    override fun onResume() {
        super.onResume()
        if (viewModel.shouldShowRouteHint) {
            showRouteToLocationSnackbar()
        }
    }

    private fun showRouteToLocationSnackbar() {
        lifecycleScope.launch {
            delay(SNACKBAR_ROUTE_DELAY)
            runOnUiThread {
                val snackbar = Snackbar.make(
                    main_layout,
                    getString(R.string.snackbar_to_septimana),
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar
                    .setAction(R.string.ok) {
                        val gmmIntentUri = viewModel.septimanaLocationUri
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.resolveActivity(packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }.show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {  // TODO replace deprecated method
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

        const val SNACKBAR_ROUTE_DELAY: Long = 1000
    }
}
