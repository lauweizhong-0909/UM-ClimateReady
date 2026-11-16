package com.um.climateready.feature.shell

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.um.climateready.MainActivity
import com.um.climateready.R
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.databinding.FragmentHomeBinding
import com.um.climateready.feature.assessment.AssessmentActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Button Listeners
        binding.btnTakeAssessment.setOnClickListener {
            val intent = Intent(requireContext(), AssessmentActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewLessons.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_learning)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardUI()
    }

    private fun updateDashboardUI() {
        if (_binding == null) return
        val dataManager = AssetDataManager.getInstance(requireContext())

        // Update Learning Progress
        val completedCount = dataManager.getCompletedUnitsCount()
        binding.progressLearning.progress = completedCount
        binding.tvLearningProgressText.text = "$completedCount of 12 units completed"

        // Update Assessment Card & Score Badge
        if (dataManager.hasCompletedPreAssessment()) {
            val score = dataManager.getPreAssessmentTotalScore()
            binding.tvAssessmentScoreBadge.text = "Score: $score / 9"
            binding.btnTakeAssessment.text = "Retake Assessment"

            when {
                score <= 3 -> {
                    binding.tvAssessmentScoreBadge.setTextColor(requireContext().getColor(R.color.score_red))
                    binding.tvAssessmentScoreBadge.setBackgroundColor(requireContext().getColor(R.color.score_red_container))
                }
                score <= 6 -> {
                    binding.tvAssessmentScoreBadge.setTextColor(requireContext().getColor(R.color.score_yellow))
                    binding.tvAssessmentScoreBadge.setBackgroundColor(requireContext().getColor(R.color.score_yellow_container))
                }
                else -> {
                    binding.tvAssessmentScoreBadge.setTextColor(requireContext().getColor(R.color.score_green))
                    binding.tvAssessmentScoreBadge.setBackgroundColor(requireContext().getColor(R.color.score_green_container))
                }
            }
        } else {
            binding.tvAssessmentScoreBadge.text = "Not Assessed"
            binding.btnTakeAssessment.text = "Start Assessment"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
