package com.um.climateready.feature.assessment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.um.climateready.R
import com.um.climateready.core.util.AssessmentResult
import com.um.climateready.core.util.CategoryScoreSummary
import com.um.climateready.core.util.QuestionEvaluation
import com.um.climateready.core.util.RecommendationResult
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.databinding.FragmentAssessmentResultBinding
import com.um.climateready.databinding.ItemAssessmentReviewBinding

class AssessmentResultFragment : Fragment() {

    private var _binding: FragmentAssessmentResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssessmentViewModel by activityViewModels()
    private var isReviewExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackToHub.setOnClickListener {
            viewModel.returnToHub()
        }

        binding.btnOpenComparison.setOnClickListener {
            viewModel.openComparison()
        }

        binding.btnShareResult.setOnClickListener {
            val summary = viewModel.buildShareTextForActiveResult() ?: return@setOnClickListener
            (activity as? AssessmentActivity)?.shareAnonymousSummary(
                summaryText = summary,
                chooserTitle = "Share Assessment Result"
            )
        }

        binding.btnToggleReview.setOnClickListener {
            isReviewExpanded = !isReviewExpanded
            binding.layoutReviewList.visibility = if (isReviewExpanded) View.VISIBLE else View.GONE
            binding.btnToggleReview.text = if (isReviewExpanded) "Hide Review" else "Show Review"
        }

        viewModel.activeResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                bindResult(result)
            }
        }

        viewModel.activeRecommendation.observe(viewLifecycleOwner) { recommendation ->
            if (recommendation != null) {
                bindRecommendation(recommendation)
            }
        }

        viewModel.comparison.observe(viewLifecycleOwner) { comparison ->
            binding.btnOpenComparison.visibility = if (comparison != null) View.VISIBLE else View.GONE
        }
    }

    private fun bindResult(result: AssessmentResult) {
        binding.tvResultTypeLabel.text = if (result.assessmentType == ScoringEngine.TYPE_POST) {
            "POST-ASSESSMENT SUMMARY"
        } else {
            "PRE-ASSESSMENT SUMMARY"
        }

        binding.tvTotalScore.text = "${result.totalScore} / ${result.maxScore}"
        binding.tvTotalBandBadge.text = result.totalBand.label
        applyPillColors(
            binding.tvTotalBandBadge,
            result.totalBand.textColorRes,
            result.totalBand.containerColorRes
        )

        val summaryByCategory = result.categorySummaries.associateBy { it.category }

        summaryByCategory[ScoringEngine.CATEGORY_HEAVY_RAIN]?.let {
            bindCategoryCard(it, binding.tvRainScore, binding.tvRainBand, binding.progressRain, binding.tvRainGuidance)
        }
        summaryByCategory[ScoringEngine.CATEGORY_EXTREME_HEAT]?.let {
            bindCategoryCard(it, binding.tvHeatScore, binding.tvHeatBand, binding.progressHeat, binding.tvHeatGuidance)
        }
        summaryByCategory[ScoringEngine.CATEGORY_HAZE]?.let {
            bindCategoryCard(it, binding.tvHazeScore, binding.tvHazeBand, binding.progressHaze, binding.tvHazeGuidance)
        }

        populateQuestionReview(result.questionEvaluations)
    }

    private fun bindCategoryCard(
        summary: CategoryScoreSummary,
        scoreView: TextView,
        bandView: TextView,
        progressBar: LinearProgressIndicator,
        guidanceView: TextView
    ) {
        val ctx = requireContext()
        val indicatorColor = ContextCompat.getColor(ctx, summary.band.textColorRes)
        val trackColor = ContextCompat.getColor(ctx, summary.band.containerColorRes)

        scoreView.text = "${summary.score} / ${summary.maxScore}"
        bandView.text = summary.band.label
        applyPillColors(bandView, summary.band.textColorRes, summary.band.containerColorRes)

        progressBar.max = summary.maxScore
        progressBar.progress = summary.score
        progressBar.setIndicatorColor(indicatorColor)
        progressBar.trackColor = trackColor

        guidanceView.text = summary.band.guidance
    }

    private fun bindRecommendation(rec: RecommendationResult) {
        val unit = rec.recommendedUnit
        if (unit != null) {
            binding.tvRecommendedUnitTitle.text = unit.title
            binding.tvRecommendedUnitMeta.text =
                "${ResultColorMapper.categoryDisplayName(unit.category)} • Unit ${unit.id}"
            binding.tvRecommendedReason.text = rec.reason
            binding.btnStartLesson.text = "Start Lesson (${unit.id})"
            binding.btnStartLesson.setOnClickListener {
                (activity as? AssessmentActivity)?.navigateToMicroLearning(unit.id)
            }
        } else {
            val unlockStatus = viewModel.unlockStatus.value
            val postUnlocked = unlockStatus?.isUnlocked == true
            binding.tvRecommendedUnitTitle.text = if (rec.allUnitsCompleted) {
                "All 12 Micro-Learning Units Completed"
            } else {
                "Explore Micro-Learning Curriculum"
            }
            binding.tvRecommendedUnitMeta.text = if (postUnlocked) {
                "Post-Assessment Unlocked"
            } else {
                "Continue learning to unlock Post-Assessment"
            }
            binding.tvRecommendedReason.text = rec.reason
            if (postUnlocked && viewModel.activeType.value == ScoringEngine.TYPE_PRE) {
                binding.btnStartLesson.text = "Take Post-Assessment"
                binding.btnStartLesson.setOnClickListener {
                    viewModel.startAssessment(ScoringEngine.TYPE_POST)
                }
            } else {
                binding.btnStartLesson.text = "Open Micro-Learning"
                binding.btnStartLesson.setOnClickListener {
                    (activity as? AssessmentActivity)?.navigateToMicroLearning()
                }
            }
        }
    }

    private fun populateQuestionReview(evaluations: List<QuestionEvaluation>) {
        binding.layoutReviewList.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        evaluations.forEachIndexed { idx, eval ->
            val itemBinding = ItemAssessmentReviewBinding.inflate(inflater, binding.layoutReviewList, false)
            val shortCat = ResultColorMapper.categoryShortName(eval.category)
            itemBinding.tvReviewHeader.text = "Q${idx + 1} • $shortCat"
            itemBinding.tvReviewQuestion.text = eval.questionText

            if (eval.isCorrect) {
                itemBinding.tvReviewStatus.text = "Correct"
                applyPillColors(itemBinding.tvReviewStatus, R.color.score_green, R.color.score_green_container)
            } else {
                itemBinding.tvReviewStatus.text = "Incorrect"
                applyPillColors(itemBinding.tvReviewStatus, R.color.score_red, R.color.score_red_container)
            }

            val selectedDisplay = if (eval.selectedOptionId != null) {
                "${eval.selectedOptionId}) ${eval.selectedOptionText.orEmpty()}"
            } else {
                "Not answered"
            }
            itemBinding.tvReviewYourAnswer.text = "Your answer: $selectedDisplay"
            itemBinding.tvReviewCorrectAnswer.text =
                "Correct answer: ${eval.correctOptionId}) ${eval.correctOptionText}"
            itemBinding.tvReviewExplanation.text = eval.explanation
            itemBinding.tvReviewLinkedUnit.text = "Linked Micro-Learning Unit: ${eval.linkedLearningUnitId}"

            binding.layoutReviewList.addView(itemBinding.root)
        }
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
