package com.um.climateready.data.repository

import android.content.Context
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.LearningDataHelper

/**
 * Repository for Module 2 — Climate Micro-Learning.
 *
 * Acts as the single source of truth for learning unit data and progress,
 * wrapping [AssetDataManager] and delegating pure filtering/completion logic
 * to [LearningDataHelper] to follow the MVVM + Repository pattern.
 */
class LearningRepository(context: Context) {

    private val dataManager = AssetDataManager.getInstance(context)

    /** Returns all loaded learning units with up-to-date [LearningUnit.isCompleted] state. */
    fun getLearningUnits(): List<LearningUnit> = dataManager.learningUnits

    /** Returns total number of loaded units (12 in the standard curriculum). */
    fun getTotalUnitCount(): Int = dataManager.learningUnits.size

    /**
     * Returns units filtered by category.
     * Invalid, null, or blank categories safely resolve to all units.
     */
    fun getFilteredUnits(category: String?): List<LearningUnit> {
        return LearningDataHelper.filterUnits(dataManager.learningUnits, category)
    }

    /** Finds a unit by ID (case-insensitive) for deep-linking or detail display. */
    fun getUnitById(unitId: String?): LearningUnit? {
        return LearningDataHelper.resolveUnitById(dataManager.learningUnits, unitId)
    }

    /**
     * Toggles the completion state of a learning unit and persists the change
     * to SharedPreferences immediately.
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
        return LearningDataHelper.getCompletedCountByCategory(
            units = dataManager.learningUnits,
            completedIds = dataManager.getCompletedUnitIds(),
            category = category
        )
    }

    /**
     * Returns true when the post-assessment is unlocked:
     * total completed >= 9 AND each category has >= 2 completed units.
     */
    fun isPostTestUnlocked(): Boolean {
        return LearningDataHelper.isPostTestUnlocked(
            units = dataManager.learningUnits,
            completedIds = dataManager.getCompletedUnitIds()
        )
    }
}
