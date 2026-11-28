package com.um.climateready.feature.shell

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.um.climateready.MainActivity
import com.um.climateready.R
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoreBand
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.data.repository.AssessmentRepository
import com.um.climateready.data.repository.LearningRepository
import com.um.climateready.databinding.FragmentHomeBinding
import com.um.climateready.feature.assessment.AssessmentActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var assessmentRepository: AssessmentRepository
    private lateinit var learningRepository: LearningRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        assessmentRepository = AssessmentRepository(requireContext())
        learningRepository = LearningRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Navigation Shortcuts
        binding.btnViewLessons.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_learning)
        }

        binding.cardActionQuiz.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_quiz)
        }

        binding.cardActionExplorer.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_explorer)
        }

        binding.cardActionResources.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_resources)
        }

        binding.cardEnvStatus.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_explorer)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardUI()
    }

    private fun updateDashboardUI() {
        if (_binding == null) return
        // --- 1. Learning Progress Card ---
        val totalCount = learningRepository.getTotalUnitCount().let { if (it > 0) it else 12 }
        val completedCount = learningRepository.getCompletedCount().coerceAtMost(totalCount)
        binding.progressLearning.max = totalCount
        binding.progressLearning.progress = completedCount
        binding.tvLearningProgressText.text = getString(
            R.string.home_learning_progress_format,
            completedCount,
            totalCount
        )
        binding.btnViewLessons.text = when {
            completedCount <= 0 -> getString(R.string.home_btn_start_learning)
            completedCount >= totalCount -> getString(R.string.home_btn_review_lessons)
            else -> getString(R.string.home_btn_resume_learning)
        }

        // --- 2. Assessment Status & Post-Test Unlock ---
        val preAttempt = assessmentRepository.getLatestPreAttempt()
        val postAttempt = assessmentRepository.getLatestPostAttempt()
        val unlockStatus = assessmentRepository.getPostTestUnlockStatus()

        when {
            // Case A: Both Pre and Post Assessments are Completed
            preAttempt != null && postAttempt != null -> {
                val score = postAttempt.totalScore
                binding.tvAssessmentScoreBadge.text = "Post: $score / 9"
                applyScoreBadgeColor(score)

                binding.tvAssessmentDesc.text =
                    "Pre: ${preAttempt.totalScore}/9  •  Post: ${postAttempt.totalScore}/9. Review your climate literacy growth and knowledge comparison."
                binding.tvAssessmentUnlockHint.visibility = View.GONE
                binding.btnTakeAssessment.text = "View Comparison & Certificate"
                binding.btnTakeAssessment.setOnClickListener {
                    val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_COMPARISON)
                    }
                    startActivity(intent)
                }
            }

            // Case B: Pre-Assessment Completed & Post-Assessment is UNLOCKED
            preAttempt != null && unlockStatus.isUnlocked -> {
                val score = preAttempt.totalScore
                binding.tvAssessmentScoreBadge.text = "Pre: $score / 9"
                applyScoreBadgeColor(score)

                binding.tvAssessmentDesc.text =
                    "Baseline established (${score}/9). You have completed sufficient micro-learning units to take the final Post-Assessment!"
                binding.tvAssessmentUnlockHint.visibility = View.VISIBLE
                binding.tvAssessmentUnlockHint.text = "🎉 Post-Assessment is unlocked! Ready to test your progress?"
                binding.btnTakeAssessment.text = "Take Post-Assessment"
                binding.btnTakeAssessment.setOnClickListener {
                    val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_START_POST)
                    }
                    startActivity(intent)
                }
            }

            // Case C: Pre-Assessment Completed, but Post-Assessment is LOCKED
            preAttempt != null -> {
                val score = preAttempt.totalScore
                binding.tvAssessmentScoreBadge.text = "Pre: $score / 9"
                applyScoreBadgeColor(score)

                binding.tvAssessmentDesc.text =
                    "Baseline score: ${score}/9. Study micro-learning modules to unlock the final post-assessment verification."
                binding.tvAssessmentUnlockHint.visibility = View.VISIBLE
                val needed = maxOf(0, unlockStatus.requiredTotal - unlockStatus.completedTotal)
                binding.tvAssessmentUnlockHint.text =
                    "Unlock requirement: complete $needed more learning units (>=2 per climate topic)."
                binding.btnTakeAssessment.text = "Retake Assessment"
                binding.btnTakeAssessment.setOnClickListener {
                    val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_START_PRE)
                    }
                    startActivity(intent)
                }
            }

            // Case D: Not Assessed Yet
            else -> {
                binding.tvAssessmentScoreBadge.text = getString(R.string.home_assessment_not_assessed)
                binding.tvAssessmentScoreBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                binding.tvAssessmentScoreBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.divider_line)
                )

                binding.tvAssessmentDesc.text = getString(R.string.home_assessment_card_desc)
                binding.tvAssessmentUnlockHint.visibility = View.GONE
                binding.btnTakeAssessment.text = getString(R.string.home_btn_start_assessment)
                binding.btnTakeAssessment.setOnClickListener {
                    val intent = Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_START_PRE)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun applyScoreBadgeColor(score: Int) {
        val band = ResultColorMapper.mapTotalScore(score)
        val context = requireContext()

        binding.tvAssessmentScoreBadge.setTextColor(ContextCompat.getColor(context, band.textColorRes))
        binding.tvAssessmentScoreBadge.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, band.containerColorRes)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
