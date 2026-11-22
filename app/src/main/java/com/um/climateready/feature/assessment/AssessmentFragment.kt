package com.um.climateready.feature.assessment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.um.climateready.R
import com.um.climateready.core.util.AssessmentAttempt
import com.um.climateready.core.util.PostTestUnlockStatus
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.databinding.FragmentAssessmentBinding

class AssessmentFragment : Fragment() {

    private var _binding: FragmentAssessmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLaunchQuiz.setOnClickListener {
            if (activity is AssessmentActivity) {
                if (!viewModel.startAssessment(ScoringEngine.TYPE_PRE)) {
                    Toast.makeText(requireContext(), "No pre-assessment questions found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                startActivity(
                    Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_START_PRE)
                    }
                )
            }
        }

        binding.btnViewPreResults.setOnClickListener {
            viewModel.openSavedResult(ScoringEngine.TYPE_PRE)
        }

        binding.btnLaunchPostQuiz.setOnClickListener {
            if (activity is AssessmentActivity) {
                if (!viewModel.startAssessment(ScoringEngine.TYPE_POST)) {
                    Toast.makeText(
                        requireContext(),
                        "Complete at least 9 units (min. 2 per category) to unlock the Post-Assessment.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                startActivity(
                    Intent(requireContext(), AssessmentActivity::class.java).apply {
                        putExtra(AssessmentActivity.EXTRA_INITIAL_MODE, AssessmentActivity.MODE_START_POST)
                    }
                )
            }
        }

        binding.btnViewPostResults.setOnClickListener {
            viewModel.openSavedResult(ScoringEngine.TYPE_POST)
        }

        binding.btnGoToLearning.setOnClickListener {
            (activity as? AssessmentActivity)?.navigateToMicroLearning()
        }

        binding.btnViewComparison.setOnClickListener {
            viewModel.openComparison()
        }

        viewModel.preAttempt.observe(viewLifecycleOwner) { pre ->
            bindPreAssessmentCard(pre)
            updateComparisonVisibility()
        }

        viewModel.postAttempt.observe(viewLifecycleOwner) { post ->
            bindPostAssessmentStatus(post, viewModel.unlockStatus.value)
            updateComparisonVisibility()
        }

        viewModel.unlockStatus.observe(viewLifecycleOwner) { status ->
            bindPostAssessmentStatus(viewModel.postAttempt.value, status)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHubState()
    }

    private fun bindPreAssessmentCard(pre: AssessmentAttempt?) {
        if (pre == null) {
            binding.tvPreStatusBadge.text = "Not Taken"
            applyPillColors(binding.tvPreStatusBadge, R.color.score_yellow, R.color.score_yellow_container)
            binding.layoutPreCategoryChips.visibility = View.GONE
            binding.btnViewPreResults.visibility = View.GONE
            binding.btnLaunchQuiz.text = "Start Pre-Assessment"
            return
        }

        val totalBand = ResultColorMapper.mapTotalScore(pre.totalScore)
        binding.tvPreStatusBadge.text = "Score: ${pre.totalScore} / 9"
        applyPillColors(binding.tvPreStatusBadge, totalBand.textColorRes, totalBand.containerColorRes)

        binding.layoutPreCategoryChips.visibility = View.VISIBLE
        bindMiniCategoryChip(binding.tvPreRainChip, "Rain", pre.heavyRainScore)
        bindMiniCategoryChip(binding.tvPreHeatChip, "Heat", pre.extremeHeatScore)
        bindMiniCategoryChip(binding.tvPreHazeChip, "Haze", pre.hazeScore)

        binding.btnViewPreResults.visibility = View.VISIBLE
        binding.btnLaunchQuiz.text = "Retake Pre-Assessment"
    }

    private fun bindMiniCategoryChip(view: TextView, label: String, score: Int) {
        val band = ResultColorMapper.mapCategoryScore(score)
        view.text = "$label: $score/3"
        applyPillColors(view, band.textColorRes, band.containerColorRes)
    }

    private fun bindPostAssessmentStatus(post: AssessmentAttempt?, status: PostTestUnlockStatus?) {
        val unlock = status ?: return

        binding.tvReqTotal.text = formatRequirement(
            "Total Micro-Learning Units",
            unlock.completedTotal,
            unlock.requiredTotal
        )
        binding.tvReqRain.text = formatRequirement(
            "Heavy Rain & Flooding",
            unlock.completedRain,
            unlock.requiredPerCategory
        )
        binding.tvReqHeat.text = formatRequirement(
            "Extreme Heat",
            unlock.completedHeat,
            unlock.requiredPerCategory
        )
        binding.tvReqHaze.text = formatRequirement(
            "Haze & Air Quality",
            unlock.completedHaze,
            unlock.requiredPerCategory
        )

        setRequirementColor(binding.tvReqTotal, unlock.completedTotal >= unlock.requiredTotal)
        setRequirementColor(binding.tvReqRain, unlock.completedRain >= unlock.requiredPerCategory)
        setRequirementColor(binding.tvReqHeat, unlock.completedHeat >= unlock.requiredPerCategory)
        setRequirementColor(binding.tvReqHaze, unlock.completedHaze >= unlock.requiredPerCategory)

        if (unlock.isUnlocked) {
            binding.btnLaunchPostQuiz.isEnabled = true
            binding.btnLaunchPostQuiz.alpha = 1f
            binding.btnGoToLearning.visibility = View.GONE

            if (post != null) {
                val band = ResultColorMapper.mapTotalScore(post.totalScore)
                binding.tvPostStatusBadge.text = "Score: ${post.totalScore} / 9"
                applyPillColors(binding.tvPostStatusBadge, band.textColorRes, band.containerColorRes)
                binding.btnViewPostResults.visibility = View.VISIBLE
                binding.btnLaunchPostQuiz.text = "Retake Post-Assessment"
            } else {
                binding.tvPostStatusBadge.text = "Unlocked"
                applyPillColors(binding.tvPostStatusBadge, R.color.score_green, R.color.score_green_container)
                binding.btnViewPostResults.visibility = View.GONE
                binding.btnLaunchPostQuiz.text = "Start Post-Assessment"
            }
        } else {
            binding.tvPostStatusBadge.text = "Locked"
            applyPillColors(binding.tvPostStatusBadge, R.color.score_red, R.color.score_red_container)
            binding.btnLaunchPostQuiz.isEnabled = false
            binding.btnLaunchPostQuiz.alpha = 0.5f
            binding.btnLaunchPostQuiz.text = "Locked (Complete Units First)"
            binding.btnViewPostResults.visibility = View.GONE
            binding.btnGoToLearning.visibility = View.VISIBLE
        }
    }

    private fun updateComparisonVisibility() {
        val hasBoth = viewModel.preAttempt.value != null && viewModel.postAttempt.value != null
        binding.btnViewComparison.visibility = if (hasBoth) View.VISIBLE else View.GONE
    }

    private fun formatRequirement(label: String, current: Int, required: Int): String {
        val prefix = if (current >= required) "✓" else "•"
        return "$prefix $label: $current / $required required"
    }

    private fun setRequirementColor(textView: TextView, met: Boolean) {
        val colorRes = if (met) R.color.score_green else R.color.text_secondary
        textView.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun applyPillColors(view: TextView, textColorRes: Int, bgColorRes: Int) {
        val ctx = requireContext()
        view.setTextColor(ContextCompat.getColor(ctx, textColorRes))
        ViewCompat.setBackgroundTintList(
            view,
            ColorStateList.valueOf(ContextCompat.getColor(ctx, bgColorRes))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
