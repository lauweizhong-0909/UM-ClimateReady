package com.um.climateready.feature.learning

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.um.climateready.R
import com.um.climateready.core.model.KnowledgeCheck
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.LearningDataHelper
import com.um.climateready.data.repository.LearningRepository
import com.um.climateready.databinding.BottomSheetLessonDetailBinding

/**
 * Full-detail BottomSheetDialogFragment for a single Micro-Learning unit.
 *
 * Displays lesson content, interactive CCNA-style Knowledge Check (persisting the
 * selected option across screen rotation), Key Takeaway banner, and "Mark as Completed"
 * toggle persisted via [LearningRepository]. Emits a Fragment Result upon dismissal so
 * [LearningFragment] refreshes reliably even after configuration changes.
 */
class LessonDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLessonDetailBinding? = null
    private val binding get() = _binding!!

    private var selectedOptionId: String? = null

    /** Optional legacy callback retained for direct programmatic callers. */
    var onDismissCallback: (() -> Unit)? = null

    companion object {
        const val REQUEST_KEY_LESSON_UPDATED = "request_key_lesson_updated"
        private const val ARG_UNIT_ID = "unit_id"
        private const val STATE_SELECTED_OPTION_ID = "state_selected_option_id"

        fun newInstance(unit: LearningUnit): LessonDetailBottomSheet {
            return LessonDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_UNIT_ID, unit.id)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLessonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedOptionId = savedInstanceState?.getString(STATE_SELECTED_OPTION_ID)

        val unitId = arguments?.getString(ARG_UNIT_ID)
        val repository = LearningRepository(requireContext())
        val unit = repository.getUnitById(unitId)
        if (unit == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.learning_error_unit_not_found),
                Toast.LENGTH_SHORT
            ).show()
            dismissAllowingStateLoss()
            return
        }

        bindLessonContent(unit)
        bindKnowledgeCheck(unit.knowledgeCheck)
        bindCompleteButton(unit, repository)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedOptionId?.let { outState.putString(STATE_SELECTED_OPTION_ID, it) }
    }

    private fun bindLessonContent(unit: LearningUnit) {
        with(binding) {
            tvDetailCategoryBadge.text = categoryLabel(unit.category)
            tvDetailTitle.text = unit.title
            tvDetailSummary.text = unit.summary
            tvDetailRealWorldExample.text = unit.realWorldExample
            tvDetailUmTip.text = unit.umStudentTip
            tvDetailTakeaway.text = unit.takeaway
        }
    }

    private fun bindKnowledgeCheck(kc: KnowledgeCheck?) {
        if (kc == null || kc.options.isEmpty()) {
            binding.cardKnowledgeCheck.visibility = View.GONE
            return
        }

        binding.cardKnowledgeCheck.visibility = View.VISIBLE
        with(binding) {
            tvKcQuestion.text = kc.question

            val optionButtons = listOf(btnOptionA, btnOptionB, btnOptionC, btnOptionD)
            optionButtons.forEachIndexed { index, btn ->
                val option = kc.options.getOrNull(index)
                if (option == null) {
                    btn.visibility = View.GONE
                } else {
                    btn.visibility = View.VISIBLE
                    btn.isEnabled = true
                    btn.text = getString(R.string.learning_option_format, option.id, option.text)
                    btn.tag = option.id
                    btn.setOnClickListener { clickedBtn ->
                        val chosenId = clickedBtn.tag as? String ?: return@setOnClickListener
                        onOptionSelected(chosenId, kc, optionButtons)
                    }
                }
            }

            // Restore prior answer state after screen rotation
            val restoredId = selectedOptionId
            if (!restoredId.isNullOrBlank()) {
                onOptionSelected(restoredId, kc, optionButtons)
            }
        }
    }

    private fun onOptionSelected(
        selectedId: String,
        kc: KnowledgeCheck,
        buttons: List<MaterialButton>
    ) {
        selectedOptionId = selectedId
        val ctx = requireContext()
        val correctGreen = ContextCompat.getColor(ctx, R.color.learning_answer_correct_bg)
        val incorrectRed = ContextCompat.getColor(ctx, R.color.learning_answer_wrong_bg)
        val white = ContextCompat.getColor(ctx, R.color.white)

        buttons.filter { it.isVisible }.forEach { btn ->
            btn.isEnabled = false

            val optionId = btn.tag as? String
            when {
                optionId == kc.correctOptionId -> {
                    btn.backgroundTintList = ColorStateList.valueOf(correctGreen)
                    btn.setTextColor(white)
                    btn.strokeWidth = 0
                }
                optionId == selectedId -> {
                    btn.backgroundTintList = ColorStateList.valueOf(incorrectRed)
                    btn.setTextColor(white)
                    btn.strokeWidth = 0
                }
            }
        }

        binding.cardExplanation.visibility = View.VISIBLE
        binding.tvKcExplanation.text = kc.explanation
    }

    private fun bindCompleteButton(unit: LearningUnit, repository: LearningRepository) {
        updateCompleteButtonState(unit.isCompleted)

        binding.btnMarkComplete.setOnClickListener {
            val newState = !unit.isCompleted
            repository.toggleCompletion(unit.id, newState)
            unit.isCompleted = newState
            updateCompleteButtonState(newState)

            val message = if (newState) {
                getString(R.string.learning_toast_completed, unit.title)
            } else {
                getString(R.string.learning_toast_incomplete, unit.title)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCompleteButtonState(isCompleted: Boolean) {
        val ctx = requireContext()
        with(binding.btnMarkComplete) {
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            if (isCompleted) {
                text = getString(R.string.learning_btn_completed_undo)
                backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.learning_complete_bg)
            } else {
                text = getString(R.string.learning_btn_mark_complete)
                backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.learning_incomplete_bg)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult(REQUEST_KEY_LESSON_UPDATED, Bundle.EMPTY)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun categoryLabel(category: String): String = when (category) {
        LearningDataHelper.CATEGORY_HEAVY_RAIN -> getString(R.string.learning_category_rain)
        LearningDataHelper.CATEGORY_EXTREME_HEAT -> getString(R.string.learning_category_heat)
        LearningDataHelper.CATEGORY_HAZE -> getString(R.string.learning_category_haze)
        else -> category
    }
}
