package com.um.climateready.feature.learning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.databinding.ItemLessonCardBinding

/**
 * RecyclerView adapter for Module 2 — Climate Micro-Learning unit cards.
 *
 * Each card displays:
 * - Unit order number (globalOrder)
 * - Category badge (Heavy Rain / Extreme Heat / Haze & AQI)
 * - Unit title
 * - Truncated summary snippet
 * - Completion checkmark (shown when isCompleted == true)
 *
 * Follows the same ViewHolder / updateData() pattern as [ResourceAdapter].
 */
class LessonAdapter(
    private var units: List<LearningUnit>,
    private val onUnitClick: (LearningUnit) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    inner class LessonViewHolder(
        val binding: ItemLessonCardBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val unit = units[position]
        with(holder.binding) {
            // Unit number badge (zero-padded, e.g. "01", "12")
            tvUnitNumber.text = unit.globalOrder.toString().padStart(2, '0')

            // Category label
            tvCategoryBadge.text = categoryLabel(unit.category)

            // Title
            tvUnitTitle.text = unit.title

            // Summary snippet — first 100 characters followed by ellipsis
            val snippet = if (unit.summary.length > 100) {
                unit.summary.take(100).trimEnd() + "…"
            } else {
                unit.summary
            }
            tvUnitSummarySnippet.text = snippet

            // Completion indicator (right-side tick icon)
            ivCompletedRight.visibility = if (unit.isCompleted) View.VISIBLE else View.GONE

            // Click listener opens the detail bottom sheet
            root.setOnClickListener { onUnitClick(unit) }
        }
    }

    override fun getItemCount(): Int = units.size

    /** Replaces the displayed unit list (e.g. after filtering or completion toggle). */
    fun updateData(newUnits: List<LearningUnit>) {
        units = newUnits
        notifyDataSetChanged()
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun categoryLabel(category: String): String = when (category) {
        "HEAVY_RAIN"      -> "🌧 Heavy Rain"
        "EXTREME_HEAT"    -> "☀ Extreme Heat"
        "HAZE_AIR_QUALITY" -> "🌫 Haze & AQI"
        else              -> category
    }
}
