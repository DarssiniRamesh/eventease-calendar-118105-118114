package com.example.calendareventfrontend

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.example.calendareventfrontend.databinding.ActivityMainBinding
import com.example.calendareventfrontend.model.CalendarViewMode

// PUBLIC_INTERFACE
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    /**
     * Entry point for the main activity.
     * Sets up the navigation drawer and shows the default (month) calendar view.
     */
    // PUBLIC_INTERFACE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        setupNavigationDrawer()

        // Show month view by default
        if (savedInstanceState == null) {
            replaceFragment(CalendarFragment.newInstance(CalendarViewMode.MONTH))
        }

        binding.fab.setOnClickListener {
            val calendarFragment = supportFragmentManager.findFragmentById(R.id.content_frame)
            val dialog = EventDialogFragment.newInstance(null)
            if (calendarFragment is CalendarFragment) {
                dialog.setEventChangedListener(object : EventDialogFragment.EventChangedListener {
                    override fun onEventChanged() {
                        calendarFragment.refreshEvents()
                    }
                })
                dialog.show(calendarFragment.childFragmentManager, "EventDialogFragment")
            } else {
                dialog.show(supportFragmentManager, "EventDialogFragment")
            }
        }
    }

    /**
     * Sets up the navigation drawer, configuring item selection listener.
     */
    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: android.view.View) {}
            override fun onDrawerClosed(drawerView: android.view.View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    /**
     * Handles navigation drawer item selection for switching between views and search.
     */
    // PUBLIC_INTERFACE
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_month -> {
                replaceFragment(CalendarFragment.newInstance(CalendarViewMode.MONTH))
            }
            R.id.menu_week -> {
                replaceFragment(CalendarFragment.newInstance(CalendarViewMode.WEEK))
            }
            R.id.menu_day -> {
                replaceFragment(CalendarFragment.newInstance(CalendarViewMode.DAY))
            }
            R.id.menu_search -> {
                replaceFragment(SearchFragment())
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
