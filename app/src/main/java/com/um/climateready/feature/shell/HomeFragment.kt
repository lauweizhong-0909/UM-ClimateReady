package com.um.climateready.feature.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.um.climateready.MainActivity
import com.um.climateready.R
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.databinding.FragmentHomeBinding

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
        val dataManager = AssetDataManager.getInstance(requireContext())

        // 1. Update Learning Progress
        val completedCount = dataManager.getCompletedUnitsCount()
        binding.progressLearning.progress = completedCount
        binding.tvLearningProgressText.text = "$completedCount of 12 units completed"

        // 2. Update Assessment Score Badge
        if (dataManager.hasCompletedPreAssessment()) {
            val score = dataManager.getPreAssessmentTotalScore()
            binding.tvAssessmentScoreBadge.text = "Score: $score / 9"
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
        }

        // 3. Quick Action Buttons
        binding.btnTakeAssessment.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_assessment)
        }

        binding.btnViewLessons.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.navigation_learning)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
