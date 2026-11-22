package com.um.climateready.feature.assessment

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.um.climateready.MainActivity
import com.um.climateready.R
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.databinding.ActivityAssessmentBinding

class AssessmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.assessmentRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val topPadding = maxOf(systemBars.top, displayCutout.top)
            binding.toolbarAssessment.setPadding(0, topPadding, 0, 0)
            binding.assessmentFragmentContainer.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        binding.toolbarAssessment.setNavigationOnClickListener {
            handleBackNavigation()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        })

        if (savedInstanceState == null) {
            when (intent.getStringExtra(EXTRA_INITIAL_MODE)) {
                MODE_START_PRE -> viewModel.startAssessment(ScoringEngine.TYPE_PRE)
                MODE_START_POST -> viewModel.startAssessment(ScoringEngine.TYPE_POST)
                MODE_COMPARISON -> {
                    if (!viewModel.openComparison()) {
                        viewModel.returnToHub()
                    }
                }
                else -> viewModel.returnToHub()
            }
        }

        viewModel.currentScreen.observe(this) { screen ->
            renderScreen(screen)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentScreen.value == AssessmentScreen.HUB) {
            viewModel.refreshHubState()
        }
    }

    private fun renderScreen(screen: AssessmentScreen) {
        val (tag, title, fragmentFactory) = when (screen) {
            AssessmentScreen.HUB -> Triple(
                TAG_HUB,
                "Climate Awareness Assessment",
                { AssessmentFragment() }
            )
            AssessmentScreen.QUIZ -> {
                val quizTitle = if (viewModel.activeType.value == ScoringEngine.TYPE_POST) {
                    "Post-Assessment (9 MCQs)"
                } else {
                    "Pre-Assessment (9 MCQs)"
                }
                Triple(TAG_QUIZ, quizTitle, { AssessmentQuizFragment() })
            }
            AssessmentScreen.RESULT -> {
                val resultTitle = if (viewModel.activeType.value == ScoringEngine.TYPE_POST) {
                    "Post-Assessment Results"
                } else {
                    "Pre-Assessment Results"
                }
                Triple(TAG_RESULT, resultTitle, { AssessmentResultFragment() })
            }
            AssessmentScreen.COMPARISON -> Triple(
                TAG_COMPARISON,
                "Pre vs Post Comparison",
                { AssessmentComparisonFragment() }
            )
        }

        binding.toolbarAssessment.title = title

        val existing = supportFragmentManager.findFragmentById(R.id.assessment_fragment_container)
        if (existing != null && existing.tag == tag) {
            return
        }

        val nextFragment: Fragment = fragmentFactory()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.assessment_fragment_container, nextFragment, tag)
            .commit()
    }

    private fun handleBackNavigation() {
        when (viewModel.currentScreen.value) {
            AssessmentScreen.QUIZ -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Leave Assessment?")
                    .setMessage("Your current progress in this 9-question attempt has not been submitted yet. Return to the assessment hub?")
                    .setNegativeButton("Continue Quiz", null)
                    .setPositiveButton("Leave") { _, _ ->
                        viewModel.returnToHub()
                    }
                    .show()
            }
            AssessmentScreen.RESULT, AssessmentScreen.COMPARISON -> {
                viewModel.returnToHub()
            }
            else -> finish()
        }
    }

    fun navigateToMicroLearning(recommendedUnitId: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(MainActivity.EXTRA_TARGET_TAB, R.id.navigation_learning)
            if (recommendedUnitId != null) {
                putExtra(MainActivity.EXTRA_RECOMMENDED_UNIT_ID, recommendedUnitId)
            }
        }
        startActivity(intent)
        finish()
    }

    fun shareAnonymousSummary(summaryText: String, chooserTitle: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "UM ClimateReady Assessment Summary")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        startActivity(Intent.createChooser(sendIntent, chooserTitle))
    }

    companion object {
        const val EXTRA_INITIAL_MODE = "extra_assessment_initial_mode"
        const val MODE_HUB = "HUB"
        const val MODE_START_PRE = "START_PRE"
        const val MODE_START_POST = "START_POST"
        const val MODE_COMPARISON = "COMPARISON"

        private const val TAG_HUB = "assessment_hub"
        private const val TAG_QUIZ = "assessment_quiz"
        private const val TAG_RESULT = "assessment_result"
        private const val TAG_COMPARISON = "assessment_comparison"
    }
}
