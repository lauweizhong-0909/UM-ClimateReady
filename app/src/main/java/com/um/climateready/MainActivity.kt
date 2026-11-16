package com.um.climateready

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_assessment -> {
                    replaceFragment(AssessmentFragment())
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
