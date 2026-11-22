package com.um.climateready.feature.assessment

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
import com.google.android.material.card.MaterialCardView
import com.um.climateready.R
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.databinding.FragmentAssessmentQuizBinding

class AssessmentQuizFragment : Fragment() {

    private var _binding: FragmentAssessmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssessmentViewModel by activityViewModels()
    private var lastRenderedIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssessmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardOptA.setOnClickListener { onOptionSelected("A") }
        binding.cardOptB.setOnClickListener { onOptionSelected("B") }
        binding.cardOptC.setOnClickListener { onOptionSelected("C") }
        binding.cardOptD.setOnClickListener { onOptionSelected("D") }

        binding.btnPrevQuestion.setOnClickListener {
            viewModel.goToPreviousQuestion()
        }

        binding.btnNextQuestion.setOnClickListener {
            val selected = viewModel.getSelectedOptionForCurrentQuestion()
            if (selected == null) {
                Toast.makeText(requireContext(), "Please select an answer before continuing.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (viewModel.isLastQuestion()) {
                viewModel.submitAssessment()
            } else {
                viewModel.goToNextQuestion()
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) { index ->
            renderCurrentQuestion(index, animate = lastRenderedIndex != -1 && lastRenderedIndex != index)
            lastRenderedIndex = index
        }

        viewModel.answers.observe(viewLifecycleOwner) {
            updateOptionSelectionUI(viewModel.getSelectedOptionForCurrentQuestion())
        }
    }

    private fun onOptionSelected(optionId: String) {
        val question = getCurrentQuestion() ?: return
        viewModel.selectOption(question.id, optionId)
    }

    private fun getCurrentQuestion(): AssessmentQuestion? {
        val questions = viewModel.questions.value.orEmpty()
        val index = viewModel.currentIndex.value ?: 0
        return questions.getOrNull(index)
    }

    private fun renderCurrentQuestion(index: Int, animate: Boolean) {
        val questions = viewModel.questions.value.orEmpty()
        val question = questions.getOrNull(index) ?: return

        if (animate) {
            binding.layoutQuestionContainer.alpha = 0f
            binding.layoutQuestionContainer.animate().alpha(1f).setDuration(180L).start()
        }

        binding.scrollQuizContent.scrollTo(0, 0)

        binding.tvCategoryTag.text = ResultColorMapper.categoryDisplayName(question.category)
        binding.tvQuestionProgress.text = "Question ${index + 1} of ${questions.size}"
        binding.progressAssessment.max = questions.size
        binding.progressAssessment.progress = index + 1

        binding.tvQuestionTitle.text = ScoringEngine.sanitizeDisplayText(question.title)
        binding.tvQuestionBody.text = ScoringEngine.sanitizeDisplayText(question.question)

        val optionsById = question.options.associateBy { it.id.uppercase() }
        binding.tvOptA.text = ScoringEngine.sanitizeDisplayText(optionsById["A"]?.text ?: "")
        binding.tvOptB.text = ScoringEngine.sanitizeDisplayText(optionsById["B"]?.text ?: "")
        binding.tvOptC.text = ScoringEngine.sanitizeDisplayText(optionsById["C"]?.text ?: "")
        binding.tvOptD.text = ScoringEngine.sanitizeDisplayText(optionsById["D"]?.text ?: "")

        binding.btnPrevQuestion.isEnabled = index > 0
        binding.btnPrevQuestion.alpha = if (index > 0) 1f else 0.45f
        binding.btnNextQuestion.text = if (index == questions.lastIndex) {
            "Submit Assessment"
        } else {
            "Next Question"
        }

        updateOptionSelectionUI(viewModel.answers.value?.get(question.id))
    }

    private fun updateOptionSelectionUI(selectedOptionId: String?) {
        val options = listOf(
            Triple("A", binding.cardOptA, binding.tvBadgeA),
            Triple("B", binding.cardOptB, binding.tvBadgeB),
            Triple("C", binding.cardOptC, binding.tvBadgeC),
            Triple("D", binding.cardOptD, binding.tvBadgeD)
        )

        val ctx = requireContext()
        val selectedStroke = ContextCompat.getColor(ctx, R.color.climate_green_primary)
        val selectedBg = ContextCompat.getColor(ctx, R.color.climate_green_container)
        val defaultStroke = ContextCompat.getColor(ctx, R.color.divider_line)
        val defaultBg = ContextCompat.getColor(ctx, R.color.card_surface)
        val badgeDefaultBg = ContextCompat.getColor(ctx, R.color.bg_light)
        val white = ContextCompat.getColor(ctx, R.color.white)
        val textPrimary = ContextCompat.getColor(ctx, R.color.text_primary)

        for ((id, card, badge) in options) {
            val isSelected = id.equals(selectedOptionId, ignoreCase = true)
            applyOptionCardStyle(
                card = card,
                badge = badge,
                isSelected = isSelected,
                selectedStroke = selectedStroke,
                selectedBg = selectedBg,
                defaultStroke = defaultStroke,
                defaultBg = defaultBg,
                badgeDefaultBg = badgeDefaultBg,
                white = white,
                textPrimary = textPrimary
            )
        }
    }

    private fun applyOptionCardStyle(
        card: MaterialCardView,
        badge: TextView,
        isSelected: Boolean,
        selectedStroke: Int,
        selectedBg: Int,
        defaultStroke: Int,
        defaultBg: Int,
        badgeDefaultBg: Int,
        white: Int,
        textPrimary: Int
    ) {
        if (isSelected) {
            card.strokeColor = selectedStroke
            card.strokeWidth = dpToPx(2)
            card.setCardBackgroundColor(selectedBg)
            badge.setTextColor(white)
            ViewCompat.setBackgroundTintList(badge, ColorStateList.valueOf(selectedStroke))
        } else {
            card.strokeColor = defaultStroke
            card.strokeWidth = dpToPx(1)
            card.setCardBackgroundColor(defaultBg)
            badge.setTextColor(textPrimary)
            ViewCompat.setBackgroundTintList(badge, ColorStateList.valueOf(badgeDefaultBg))
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
