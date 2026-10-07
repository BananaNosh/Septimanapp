package com.nobodysapps.septimanapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nobodysapps.septimanapp.BuildConfig
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.databinding.ActivityMainBinding // Import View Binding class
import com.nobodysapps.septimanapp.databinding.NavHeaderMainBinding
import com.nobodysapps.septimanapp.databinding.ViewImpressumBinding
import com.nobodysapps.septimanapp.fragments.EnrolmentFragment
import com.nobodysapps.septimanapp.fragments.HorariumFragment
import com.nobodysapps.septimanapp.fragments.MapFragment
import com.nobodysapps.septimanapp.fragments.PropositaFragment
import com.nobodysapps.septimanapp.view.CountDownView
import com.nobodysapps.septimanapp.viewModel.MainViewModel
import com.nobodysapps.septimanapp.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainActivity : SeptimanappActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding // Declare binding variable

    // Whether the toolbar content (nav icon / title) is inset clear of the cutout and side
    // nav bar. Only the enrolment screen does so; map and horarium let it bleed to the edge.
    private var insetToolbarContentHorizontally = false

    // Since targetSdk 36 a back gesture no longer calls onBackPressed(); back goes through
    // the OnBackPressedDispatcher instead. This callback closes the open drawer and is only
    // enabled while the drawer is open. Popping the fragment back stack is handled by the
    // FragmentManager's own callback, and with neither enabled the activity finishes.
    private val closeDrawerOnBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflate the layout
        setContentView(binding.root) // Set the content view using the binding's root
        setSupportActionBar(binding.appBarMain.toolbar) // Access toolbar via binding

        // Android 15 (targetSdk 35) forces edge-to-edge. We intentionally do NOT use
        // fitsSystemWindows on the DrawerLayout: its legacy behaviour consumes the
        // system-window insets and turns them into content margins, which both adds a
        // spurious side margin and stops fragments from bleeding to the edge. Instead the
        // insets propagate untouched, and each view pads itself.
        //
        // We pad the AppBarLayout (not the fixed-height toolbar, whose content would
        // otherwise be clipped); its ?attr/colorPrimary background fills the full bounds
        // incl. padding, so the bar always bleeds full-width while the toolbar sits below
        // the status bar (top padding). Horizontal padding — which keeps the nav icon /
        // title clear of the landscape cutout and side nav bar — is only applied on the
        // enrolment screen, whose form is inset to match; map and horarium bleed fully, so
        // the nav icon sits flush at the edge like their content (see
        // updateToolbarInsetsForFragment). The drawer's NavigationView keeps
        // fitsSystemWindows so its header still clears the status bar.
        val appBarLayout = binding.appBarMain.appBarLayout
        val initialAppBarLeft = appBarLayout.paddingLeft
        val initialAppBarRight = appBarLayout.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(
                top = bars.top,
                left = initialAppBarLeft + if (insetToolbarContentHorizontally) bars.left else 0,
                right = initialAppBarRight + if (insetToolbarContentHorizontally) bars.right else 0
            )
            windowInsets
        }
        // goToFragment() / back press change the visible fragment via the back stack;
        // keep the toolbar inset and the checked drawer item in sync with whichever
        // fragment is now shown.
        supportFragmentManager.addOnBackStackChangedListener {
            val shownFragment = supportFragmentManager.findFragmentById(R.id.fragment_layout)
            updateToolbarInsetsForFragment(shownFragment is EnrolmentFragment)
            navItemForFragment(shownFragment)?.let { binding.navView.setCheckedItem(it) }
        }

        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        if (savedInstanceState == null) {
            replaceFragment(HorariumFragment::class.java)
        }

        val fragmentToGo: Class<*>? = intent.getSerializableExtra(FRAGMENT_TO_LOAD_KEY) as Class<*>?
        if (fragmentToGo != null) {
            replaceFragment(fragmentToGo)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout // Access drawer_layout via binding
        val navView: NavigationView = binding.navView // Access nav_view via binding
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.appBarMain.toolbar, // Access toolbar via binding
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        setupDrawerListenerForCountDown(drawerLayout)
        toggle.syncState()

        // Registered after the FragmentManager's callback, so it takes precedence: back
        // first closes the drawer before it pops the back stack.
        onBackPressedDispatcher.addCallback(this, closeDrawerOnBack)
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                closeDrawerOnBack.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                closeDrawerOnBack.isEnabled = false
            }
        })

        navView.setNavigationItemSelectedListener(this)

        // For views inside nav_header_main, you'll need to get the header view first
        val headerView = navView.getHeaderView(0)
        val navHeaderBinding = NavHeaderMainBinding.bind(headerView)
        val impressumView = binding.impressumView
        impressumView.privacyTV.movementMethod = LinkMovementMethod.getInstance()


        if (BuildConfig.DEBUG) {
            impressumView.debugTV.visibility = View.VISIBLE
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // A drawer left open before a configuration change is restored without the
        // listener's onDrawerOpened, so sync the callback with the restored state.
        closeDrawerOnBack.isEnabled = binding.drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    private fun setupDrawerListenerForCountDown(drawerLayout: DrawerLayout) {
        // Access views in nav_header_main through its binding
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderBinding = NavHeaderMainBinding.bind(headerView)

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (!navHeaderBinding.countDownTV.started) { // Use navHeaderBinding
                    val septimanaStartTime = viewModel.septimanaStartTime
                    if (septimanaStartTime == null) {
                        navHeaderBinding.countDownTV.visibility = View.GONE
                        navHeaderBinding.countDownSubTV.visibility = View.GONE
                    } else {
                        navHeaderBinding.countDownTV.visibility = View.VISIBLE
                        navHeaderBinding.countDownSubTV.visibility = View.VISIBLE
                        navHeaderBinding.countDownTV.setEndTime(septimanaStartTime, object : CountDownView.Listener {
                            override fun onFinished() {
                                navHeaderBinding.countDownTV.visibility = View.GONE
                                navHeaderBinding.countDownSubTV.visibility = View.GONE
                            }

                        })
                        navHeaderBinding.countDownTV.startTimer()
                    }
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                navHeaderBinding.countDownTV.stopTimer() // Use navHeaderBinding
            }

            override fun onDrawerOpened(drawerView: View) {}

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.isChecked) {
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Use binding
            return true
        }
        val fragmentClass = NAV_ITEM_FRAGMENTS[item.itemId]
        if (fragmentClass == null || !goToFragment(fragmentClass)) return false
        binding.drawerLayout.closeDrawer(GravityCompat.START) // Use binding
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
                    binding.appBarMain.contentMainInclude.mainLayout, // Use binding.contentMain if content_main is included with that ID
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

    private fun replaceFragment(fragmentToGo: Class<*>) {
        // These transactions don't go through the back stack, so update the inset directly.
        updateToolbarInsetsForFragment(fragmentToGo == EnrolmentFragment::class.java)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, fragmentToGo.newInstance() as Fragment).commit() // Adjust path to fragment_layout
    }

    private fun updateToolbarInsetsForFragment(isEnrolment: Boolean) {
        insetToolbarContentHorizontally = isEnrolment
        ViewCompat.requestApplyInsets(binding.appBarMain.appBarLayout)
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
            .replace(R.id.fragment_layout, fragment) // Adjust path to fragment_layout
            .addToBackStack(null)
            .commit()
        return true
    }

    companion object {
        const val TAG = "MainActivity"
        const val FRAGMENT_TO_LOAD_KEY = "fragmentToLoad"
        const val SNACKBAR_ROUTE_DELAY: Long = 1000

        private val NAV_ITEM_FRAGMENTS = mapOf(
            R.id.nav_horarium to HorariumFragment::class.java,
            R.id.nav_proposita to PropositaFragment::class.java,
            R.id.nav_map to MapFragment::class.java,
            R.id.nav_enrol to EnrolmentFragment::class.java
        )

        private fun navItemForFragment(fragment: Fragment?): Int? =
            NAV_ITEM_FRAGMENTS.entries.firstOrNull { it.value == fragment?.javaClass }?.key
    }
}
