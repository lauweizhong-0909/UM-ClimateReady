package com.um.climateready.feature.learning

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.um.climateready.R
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.KnowledgeCheck
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.databinding.BottomSheetLessonDetailBinding

/**
 * Full-detail BottomSheetDialogFragment for a single Micro-Learning unit.
 *
 * Displays:
 * - Full lesson summary, real-world example, UM student tip
 * - Interactive CCNA-style Knowledge Check with instant green/red colour feedback
 * - Key Takeaway banner
 * - "Mark as Completed" / "Undo" toggle persisted via [AssetDataManager]
 *
 * Usage:
 *   LessonDetailBottomSheet.newInstance(unit).show(childFragmentManager, "lesson_detail")
 *
 * The parent fragment should call [setOnDismissListener] (or override [onDismiss]) to
 * refresh the RecyclerView adapter and progress header after the sheet closes.
 */
class LessonDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLessonDetailBinding? = null
    private val binding get() = _binding!!

    // Callback so LearningFragment can refresh UI when the sheet is dismissed
    var onDismissCallback: (() -> Unit)? = null

    companion object {
        private const val ARG_UNIT_ID = "unit_id"

        fun newInstance(unit: LearningUnit): LessonDetailBottomSheet {
            val fragment = LessonDetailBottomSheet()
            fragment.arguments = Bundle().apply {
                putString(ARG_UNIT_ID, unit.id)
            }
            return fragment
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

        val unitId = arguments?.getString(ARG_UNIT_ID) ?: return
        val dataManager = AssetDataManager.getInstance(requireContext())
        val unit = dataManager.learningUnits.find { it.id == unitId } ?: return

        bindLessonContent(unit)
        bindKnowledgeCheck(unit.knowledgeCheck)
        bindCompleteButton(unit, dataManager)
    }

    // ---------------------------------------------------------------------------
    // Binding helpers
    // ---------------------------------------------------------------------------

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
        if (kc == null) {
            binding.cardKnowledgeCheck.visibility = View.GONE
            return
        }

        with(binding) {
            tvKcQuestion.text = kc.question

            // Map the 4 options (A, B, C, D) to the 4 buttons
            val optionButtons = listOf(btnOptionA, btnOptionB, btnOptionC, btnOptionD)
            kc.options.forEachIndexed { index, option ->
                if (index < optionButtons.size) {
                    val btn = optionButtons[index]
                    btn.text = "${option.id}. ${option.text}"
                    btn.tag = option.id      // store option ID in tag for click handler

                    btn.setOnClickListener { clickedBtn ->
                        val selectedId = clickedBtn.tag as String
                        onOptionSelected(selectedId, kc, optionButtons)
                    }
                }
            }
        }
    }

    /**
     * Called when the student taps a knowledge-check option.
     * Immediately highlights the tapped button (green if correct, red if wrong),
     * always highlights the correct button green, disables all buttons, and
     * reveals the explanation.
     */
    private fun onOptionSelected(
        selectedId: String,
        kc: KnowledgeCheck,
        buttons: List<com.google.android.material.button.MaterialButton>
    ) {
        val ctx = requireContext()
        val correctGreen = ContextCompat.getColor(ctx, R.color.score_green)
        val incorrectRed = ContextCompat.getColor(ctx, R.color.score_red)
        val white = ContextCompat.getColor(ctx, R.color.white)

        buttons.forEach { btn ->
            btn.isEnabled = false   // lock all buttons — no re-answering

            val optionId = btn.tag as String
            when {
                optionId == kc.correctOptionId -> {
                    // Always highlight correct answer green
                    btn.backgroundTintList = ColorStateList.valueOf(correctGreen)
                    btn.setTextColor(white)
                }
                optionId == selectedId -> {
                    // Wrong answer tapped — highlight red
                    btn.backgroundTintList = ColorStateList.valueOf(incorrectRed)
                    btn.setTextColor(white)
                }
                // Other buttons fade visually (keep default tint, just disabled)
            }
        }

        // Reveal explanation
        binding.cardExplanation.visibility = View.VISIBLE
        binding.tvKcExplanation.text = kc.explanation
    }

    private fun bindCompleteButton(unit: LearningUnit, dataManager: AssetDataManager) {
        updateCompleteButtonState(unit.isCompleted)

        binding.btnMarkComplete.setOnClickListener {
            val newState = !unit.isCompleted
            dataManager.setUnitCompleted(unit.id, newState)
            unit.isCompleted = newState
            updateCompleteButtonState(newState)

            val message = if (newState) {
                "✓ \"${unit.title}\" marked as completed!"
            } else {
                "\"${unit.title}\" marked as incomplete."
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCompleteButtonState(isCompleted: Boolean) {
        with(binding.btnMarkComplete) {
            if (isCompleted) {
                text = "✓  Completed — Undo"
                val grey = ContextCompat.getColorStateList(requireContext(), R.color.score_green)
                backgroundTintList = grey
            } else {
                text = "Mark as Completed"
                backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(), com.google.android.material.R.color.m3_button_background_color_selector
                )
            }
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun categoryLabel(category: String): String = when (category) {
        "HEAVY_RAIN"       -> "🌧 Heavy Rain"
        "EXTREME_HEAT"     -> "☀ Extreme Heat"
        "HAZE_AIR_QUALITY" -> "🌫 Haze & AQI"
        else               -> category
    }
}
