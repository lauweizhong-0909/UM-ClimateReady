package com.um.climateready

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.um.climateready.databinding.ActivityMainBinding
import com.um.climateready.feature.explorer.ExplorerFragment
import com.um.climateready.feature.learning.LearningFragment
import com.um.climateready.feature.quiz.QuizFragment
import com.um.climateready.feature.resources.ResourcesFragment
import com.um.climateready.feature.shell.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pendingRecommendedUnitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
            isAppearanceLightNavigationBars = !isNightMode
        }

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
                    val unitId = pendingRecommendedUnitId
                    pendingRecommendedUnitId = null
                    replaceFragment(LearningFragment.newInstance(unitId))
                    true
                }
                R.id.navigation_quiz -> {
                    replaceFragment(QuizFragment())
                    true
                }
                R.id.navigation_explorer -> {
                    replaceFragment(ExplorerFragment())
                    true
                }
                R.id.navigation_resources -> {
                    replaceFragment(ResourcesFragment())
                    true
                }
                else -> false
            }
        }

        // Avoid recreating the active tab on re-tap; forward deep-link if Learning is already active
        binding.bottomNavigation.setOnItemReselectedListener { menuItem ->
            if (menuItem.itemId == R.id.navigation_learning) {
                val unitId = pendingRecommendedUnitId
                if (!unitId.isNullOrBlank()) {
                    pendingRecommendedUnitId = null
                    val current = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? LearningFragment
                    current?.openRecommendedUnit(unitId, forceResetHistory = true)
                }
            }
        }

        // Set default fragment on first launch
        if (savedInstanceState == null) {
            pendingRecommendedUnitId = consumeRecommendedUnitExtra(intent)
            val targetTab = intent.getIntExtra(EXTRA_TARGET_TAB, R.id.navigation_home)
            binding.bottomNavigation.selectedItemId = targetTab
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRecommendedUnitId = consumeRecommendedUnitExtra(intent)
        val targetTab = intent.getIntExtra(EXTRA_TARGET_TAB, -1)
        if (targetTab != -1) {
            if (binding.bottomNavigation.selectedItemId == targetTab && targetTab == R.id.navigation_learning) {
                val unitId = pendingRecommendedUnitId
                pendingRecommendedUnitId = null
                val current = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? LearningFragment
                current?.openRecommendedUnit(unitId, forceResetHistory = true)
            } else {
                switchTab(targetTab)
            }
        }
    }

    fun switchTab(menuItemId: Int) {
        binding.bottomNavigation.selectedItemId = menuItemId
    }

    private fun consumeRecommendedUnitExtra(intent: Intent?): String? {
        val unitId = intent?.getStringExtra(EXTRA_RECOMMENDED_UNIT_ID)
        intent?.removeExtra(EXTRA_RECOMMENDED_UNIT_ID)
        return unitId
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    companion object {
        const val EXTRA_TARGET_TAB = "extra_target_tab"
        const val EXTRA_RECOMMENDED_UNIT_ID = "extra_recommended_unit_id"
    }
}
