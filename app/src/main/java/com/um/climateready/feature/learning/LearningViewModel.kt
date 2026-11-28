package com.um.climateready.feature.learning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.um.climateready.core.util.LearningDataHelper

/**
 * ViewModel for Module 2 — Climate Micro-Learning.
 *
 * Persists the active category filter and deep-link consumption state in
 * [SavedStateHandle] across configuration changes (e.g. screen rotation)
 * and process recreation.
 */
class LearningViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val selectedCategory: String
        get() = LearningDataHelper.normalizeCategory(
            savedStateHandle.get<String>(KEY_SELECTED_CATEGORY)
        )

    fun setSelectedCategory(category: String?) {
        savedStateHandle[KEY_SELECTED_CATEGORY] = LearningDataHelper.normalizeCategory(category)
    }

    /**
     * Consumes a deep-linked [unitId] at most once for this ViewModel instance,
     * preventing accidental re-opening of the detail sheet on rotation.
     */
    fun consumeDeepLinkUnitId(unitId: String?): String? {
        val candidate = unitId?.trim().orEmpty()
        if (candidate.isEmpty()) return null
        val alreadyHandled = savedStateHandle.get<String>(KEY_HANDLED_DEEP_LINK_ID)
        if (alreadyHandled.equals(candidate, ignoreCase = true)) {
            return null
        }
        savedStateHandle[KEY_HANDLED_DEEP_LINK_ID] = candidate
        return candidate
    }

    fun resetDeepLinkHistory() {
        savedStateHandle.remove<String>(KEY_HANDLED_DEEP_LINK_ID)
    }

    companion object {
        const val KEY_SELECTED_CATEGORY = "selected_category"
        const val KEY_HANDLED_DEEP_LINK_ID = "handled_deep_link_id"
    }
}
