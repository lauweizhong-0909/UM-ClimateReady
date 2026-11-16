package com.um.climateready

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.um.climateready.databinding.ActivityMainBinding
import com.um.climateready.feature.assessment.AssessmentFragment
import com.um.climateready.feature.learning.LearningFragment
import com.um.climateready.feature.quiz.QuizFragment
import com.um.climateready.feature.resources.ResourcesFragment
import com.um.climateready.feature.shell.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar icons to dark since our app has a light background
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        // Handle System Bar Insets (Status Bar at top, Gesture/Nav Bar at bottom)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val topPadding = maxOf(systemBars.top, displayCutout.top)

            // Top padding for content so status bar & camera notch never overlap
            binding.navHostFragment.setPadding(systemBars.left, topPadding, systemBars.right, 0)

            // Bottom padding for navigation bar so gesture bar doesn't overlap tabs
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Setup BottomNavigationView (5 Tabs: Home, Learn, AI Quiz, Explorer, Directory)
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_learning -> {
                    replaceFragment(LearningFragment())
                    true
                }
                R.id.navigation_quiz -> {
                    replaceFragment(QuizFragment())
                    true
                }
                R.id.navigation_explorer -> {
                    replaceFragment(com.um.climateready.feature.explorer.ExplorerFragment())
                    true
                }
                R.id.navigation_resources -> {
                    replaceFragment(ResourcesFragment())
                    true
                }
                else -> false
            }
        }

        // Set default fragment on first launch
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }

    fun switchTab(menuItemId: Int) {
        binding.bottomNavigation.selectedItemId = menuItemId
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
