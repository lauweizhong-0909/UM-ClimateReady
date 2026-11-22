package com.um.climateready.data.repository

import android.content.Context
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.LearningUnit

/**
 * Repository for Module 2 — Climate Micro-Learning.
 *
 * Acts as the single source of truth for learning unit data and progress,
 * wrapping [AssetDataManager] to follow the MVVM + Repository pattern.
 *
 * Data is loaded from the pre-compiled JSON asset [m2_microlearning_units.json].
 * Progress (completed unit IDs) is persisted via SharedPreferences inside
 * [AssetDataManager] using the key "completed_unit_ids".
 */
class LearningRepository(context: Context) {

    private val dataManager = AssetDataManager.getInstance(context)

    /** Returns all 12 learning units with up-to-date [LearningUnit.isCompleted] state. */
    fun getLearningUnits(): List<LearningUnit> = dataManager.learningUnits

    /**
     * Returns units filtered by category.
     * @param category One of "HEAVY_RAIN", "EXTREME_HEAT", "HAZE_AIR_QUALITY", or null/"ALL"
     *                 to return all 12 units.
     */
    fun getFilteredUnits(category: String?): List<LearningUnit> {
        val all = dataManager.learningUnits
        return if (category.isNullOrBlank() || category == "ALL") {
            all
        } else {
            all.filter { it.category == category }
        }
    }

    /**
     * Toggles the completion state of a learning unit and persists the change
     * to SharedPreferences immediately.
     *
     * @param unitId   The unit ID (e.g. "RAIN_01").
     * @param completed True to mark as completed, false to undo.
     */
    fun toggleCompletion(unitId: String, completed: Boolean) {
        dataManager.setUnitCompleted(unitId, completed)
    }

    /** Returns the total number of completed units across all categories. */
    fun getCompletedCount(): Int = dataManager.getCompletedUnitsCount()

    /**
     * Returns the number of completed units for a specific category.
     * Used by the Assessment module's post-test unlock check
     * (requires >= 2 per category AND >= 9 total).
     */
    fun getCompletedCountByCategory(category: String): Int {
        val completedIds = dataManager.getCompletedUnitIds()
        return dataManager.learningUnits.count { it.category == category && it.id in completedIds }
    }

    /**
     * Returns true when the post-assessment is unlocked:
     * total completed >= 9 AND each category has >= 2 completed units.
     */
    fun isPostTestUnlocked(): Boolean {
        if (getCompletedCount() < 9) return false
        return getCompletedCountByCategory("HEAVY_RAIN") >= 2 &&
               getCompletedCountByCategory("EXTREME_HEAT") >= 2 &&
               getCompletedCountByCategory("HAZE_AIR_QUALITY") >= 2
    }
}
