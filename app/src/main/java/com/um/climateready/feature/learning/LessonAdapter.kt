package com.um.climateready.feature.learning

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.um.climateready.R
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.LearningDataHelper
import com.um.climateready.databinding.ItemLessonCardBinding
import java.util.Locale

/**
 * RecyclerView adapter for Module 2 — Climate Micro-Learning unit cards.
 *
 * Uses [DiffUtil] over immutable snapshots so completion checkmark toggles and
 * category filter transitions animate cleanly without full-list rebinds.
 */
class LessonAdapter(
    initialUnits: List<LearningUnit>,
    private val onUnitClick: (LearningUnit) -> Unit
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    private var units: List<LearningUnit> = initialUnits.map { it.copy() }

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
        val ctx = holder.itemView.context
        with(holder.binding) {
            tvUnitNumber.text = String.format(Locale.getDefault(), "%02d", unit.globalOrder)
            tvCategoryBadge.text = categoryLabel(ctx, unit.category)
            tvUnitTitle.text = unit.title

            val snippet = if (unit.summary.length > 100) {
                unit.summary.take(100).trimEnd() + "…"
            } else {
                unit.summary
            }
            tvUnitSummarySnippet.text = snippet
            ivCompletedRight.visibility = if (unit.isCompleted) View.VISIBLE else View.GONE

            root.setOnClickListener { onUnitClick(unit) }
        }
    }

    override fun getItemCount(): Int = units.size

    /** Replaces the displayed unit list via [DiffUtil] using copied snapshots. */
    fun updateData(newUnits: List<LearningUnit>) {
        val newSnapshot = newUnits.map { it.copy() }
        val diffResult = DiffUtil.calculateDiff(LessonDiffCallback(units, newSnapshot))
        units = newSnapshot
        diffResult.dispatchUpdatesTo(this)
    }

    fun findPositionByUnitId(unitId: String): Int {
        return units.indexOfFirst { it.id.equals(unitId, ignoreCase = true) }
    }

    private fun categoryLabel(context: Context, category: String): String = when (category) {
        LearningDataHelper.CATEGORY_HEAVY_RAIN -> context.getString(R.string.learning_category_rain)
        LearningDataHelper.CATEGORY_EXTREME_HEAT -> context.getString(R.string.learning_category_heat)
        LearningDataHelper.CATEGORY_HAZE -> context.getString(R.string.learning_category_haze)
        else -> category
    }

    private class LessonDiffCallback(
        private val oldList: List<LearningUnit>,
        private val newList: List<LearningUnit>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
