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
import com.um.climateready.R
import com.um.climateready.core.util.AssessmentComparison
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.databinding.FragmentAssessmentComparisonBinding

class AssessmentComparisonFragment : Fragment() {

    private var _binding: FragmentAssessmentComparisonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnComparisonBack.setOnClickListener {
            viewModel.returnToHub()
        }

        binding.btnShareComparison.setOnClickListener {
            val summary = viewModel.buildShareTextForComparison() ?: return@setOnClickListener
            (activity as? AssessmentActivity)?.shareAnonymousSummary(
                summaryText = summary,
                chooserTitle = "Share Pre vs Post Comparison"
            )
        }

        viewModel.comparison.observe(viewLifecycleOwner) { comparison ->
            if (comparison != null) {
                bindComparison(comparison)
            }
        }
    }

    private fun bindComparison(c: AssessmentComparison) {
        binding.tvComparePreTotal.text = "${c.preTotal} / 9"
        binding.tvComparePostTotal.text = "${c.postTotal} / 9"
        bindDeltaPill(binding.tvCompareTotalDelta, c.totalDelta)
        binding.tvCompareInterpretation.text = c.neutralInterpretation

        bindCategoryComparison(
            preScore = c.rainPre,
            postScore = c.rainPost,
            delta = c.rainDelta,
            preView = binding.tvCompareRainPre,
            postView = binding.tvCompareRainPost,
            deltaView = binding.tvCompareRainDelta
        )

        bindCategoryComparison(
            preScore = c.heatPre,
            postScore = c.heatPost,
            delta = c.heatDelta,
            preView = binding.tvCompareHeatPre,
            postView = binding.tvCompareHeatPost,
            deltaView = binding.tvCompareHeatDelta
        )

        bindCategoryComparison(
            preScore = c.hazePre,
            postScore = c.hazePost,
            delta = c.hazeDelta,
            preView = binding.tvCompareHazePre,
            postView = binding.tvCompareHazePost,
            deltaView = binding.tvCompareHazeDelta
        )
    }

    private fun bindCategoryComparison(
        preScore: Int,
        postScore: Int,
        delta: Int,
        preView: TextView,
        postView: TextView,
        deltaView: TextView
    ) {
        val preBand = ResultColorMapper.mapCategoryScore(preScore)
        val postBand = ResultColorMapper.mapCategoryScore(postScore)

        preView.text = "Pre: $preScore/3 (${preBand.label})"
        applyPillColors(preView, preBand.textColorRes, preBand.containerColorRes)

        postView.text = "Post: $postScore/3 (${postBand.label})"
        applyPillColors(postView, postBand.textColorRes, postBand.containerColorRes)

        bindDeltaPill(deltaView, delta)
    }

    private fun bindDeltaPill(view: TextView, delta: Int) {
        view.text = ScoringEngine.formatDelta(delta)
        when {
            delta > 0 -> applyPillColors(view, R.color.score_green, R.color.score_green_container)
            delta == 0 -> applyPillColors(view, R.color.score_yellow, R.color.score_yellow_container)
            else -> applyPillColors(view, R.color.score_red, R.color.score_red_container)
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
