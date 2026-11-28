package com.um.climateready.core.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.um.climateready.core.model.KnowledgeCheck
import com.um.climateready.core.model.LearningUnit

/**
 * Pure Kotlin helper functions for Module 2 (Micro-Learning) JSON parsing,
 * category filtering, completion state tracking, and deep-link resolution.
 *
 * Kept free of Android framework dependencies so every edge case can be
 * verified directly by JUnit tests.
 */
object LearningDataHelper {

    const val CATEGORY_ALL = "ALL"
    const val CATEGORY_HEAVY_RAIN = "HEAVY_RAIN"
    const val CATEGORY_EXTREME_HEAT = "EXTREME_HEAT"
    const val CATEGORY_HAZE = "HAZE_AIR_QUALITY"

    const val REQUIRED_TOTAL_FOR_POST_TEST = 9
    const val REQUIRED_PER_CATEGORY_FOR_POST_TEST = 2

    private val VALID_CATEGORIES = setOf(
        CATEGORY_HEAVY_RAIN,
        CATEGORY_EXTREME_HEAT,
        CATEGORY_HAZE
    )

    private val PDF_FOOTER_REGEX = Regex(
        """(?m)\s*UM ClimateReady\s*[•·\-]\s*Module 2 Micro-Learning Curriculum\s*\n?\s*Page \d+ of \d+\s*"""
    )
    private val MULTI_WHITESPACE_REGEX = Regex("""\s+""")

    private val gson = Gson()
    private val unitListType = object : TypeToken<List<LearningUnit>>() {}.type

    /**
     * Safely parses a JSON array of [LearningUnit] objects.
     * Returns an empty list on null, blank, malformed, or non-array JSON,
     * and drops incomplete records that lack an ID, title, or valid category.
     */
    fun parseLearningUnitsJson(rawJson: String?): List<LearningUnit> {
        val trimmed = rawJson?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed == "null") return emptyList()

        return try {
            val root = JsonParser.parseString(trimmed)
            if (!root.isJsonArray) return emptyList()

            val parsed: List<LearningUnit>? = gson.fromJson(root, unitListType)
            parsed.orEmpty()
                .mapNotNull { unit -> sanitizeUnitOrNull(unit) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun sanitizeUnitOrNull(unit: LearningUnit?): LearningUnit? {
        if (unit == null) return null
        val id = unit.id.orEmpty().trim()
        val category = unit.category.orEmpty().trim()
        val title = sanitizeDisplayText(unit.title)
        if (id.isEmpty() || title.isEmpty() || category !in VALID_CATEGORIES) {
            return null
        }

        val sanitizedKnowledgeCheck = unit.knowledgeCheck?.let { kc ->
            val q = sanitizeDisplayText(kc.question)
            val options = kc.options.orEmpty()
                .filter { !it.id.isNullOrBlank() && !it.text.isNullOrBlank() }
                .map { it.copy(id = it.id.trim(), text = sanitizeDisplayText(it.text)) }
            if (q.isEmpty() || options.isEmpty()) {
                null
            } else {
                KnowledgeCheck(
                    question = q,
                    options = options,
                    correctOptionId = kc.correctOptionId.orEmpty().trim(),
                    explanation = sanitizeDisplayText(kc.explanation)
                )
            }
        }

        return LearningUnit(
            id = id,
            globalOrder = unit.globalOrder,
            categoryOrder = unit.categoryOrder,
            category = category,
            title = title,
            summary = sanitizeDisplayText(unit.summary),
            realWorldExample = sanitizeDisplayText(unit.realWorldExample),
            umStudentTip = sanitizeDisplayText(unit.umStudentTip),
            knowledgeCheck = sanitizedKnowledgeCheck,
            takeaway = sanitizeDisplayText(unit.takeaway),
            isCompleted = false
        )
    }

    /**
     * Strips leftover PDF page-footer strings and normalizes whitespace.
     */
    fun sanitizeDisplayText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val withoutFooter = PDF_FOOTER_REGEX.replace(raw, " ")
        return MULTI_WHITESPACE_REGEX.replace(withoutFooter.trim(), " ")
    }

    /**
     * Normalizes a category filter string; null, blank, or unknown values map to [CATEGORY_ALL].
     */
    fun normalizeCategory(category: String?): String {
        val candidate = category?.trim().orEmpty()
        return if (candidate in VALID_CATEGORIES) candidate else CATEGORY_ALL
    }

    /**
     * Filters [units] by [category], defaulting to all units when [category] is null, blank, or "ALL".
     */
    fun filterUnits(units: List<LearningUnit>, category: String?): List<LearningUnit> {
        val normalized = normalizeCategory(category)
        return if (normalized == CATEGORY_ALL) {
            units
        } else {
            units.filter { it.category == normalized }
        }
    }

    /**
     * Filters persisted IDs so only IDs corresponding to actual loaded units are counted.
     */
    fun validCompletedIds(units: List<LearningUnit>, rawCompletedIds: Set<String>): Set<String> {
        if (units.isEmpty() || rawCompletedIds.isEmpty()) return emptySet()
        val validIds = units.map { it.id }.toSet()
        return rawCompletedIds.filterTo(LinkedHashSet()) { it in validIds }
    }

    /**
     * Updates [LearningUnit.isCompleted] flags in-place from [completedIds] and returns valid IDs.
     */
    fun applyCompletionState(units: List<LearningUnit>, completedIds: Set<String>): Set<String> {
        val validCompleted = validCompletedIds(units, completedIds)
        units.forEach { it.isCompleted = it.id in validCompleted }
        return validCompleted
    }

    /**
     * Toggles [unitId] in [completedIds] idempotently. Ignores blank or unknown unit IDs.
     */
    fun toggleUnitCompletion(
        units: List<LearningUnit>,
        completedIds: Set<String>,
        unitId: String,
        completed: Boolean
    ): Set<String> {
        val validCompleted = validCompletedIds(units, completedIds).toMutableSet()
        val trimmedId = unitId.trim()
        val exists = units.any { it.id == trimmedId }
        if (!exists) return validCompleted

        if (completed) {
            validCompleted.add(trimmedId)
        } else {
            validCompleted.remove(trimmedId)
        }
        units.find { it.id == trimmedId }?.isCompleted = completed
        return validCompleted
    }

    /**
     * Counts completed units across all valid loaded units.
     */
    fun getCompletedCount(units: List<LearningUnit>, completedIds: Set<String>): Int {
        return validCompletedIds(units, completedIds).size
    }

    /**
     * Counts completed units within a specific [category].
     */
    fun getCompletedCountByCategory(
        units: List<LearningUnit>,
        completedIds: Set<String>,
        category: String
    ): Int {
        val validCompleted = validCompletedIds(units, completedIds)
        return units.count { it.category == category && it.id in validCompleted }
    }

    /**
     * Evaluates Module 1 post-test unlock criteria:
     * >= 9 completed units overall AND >= 2 in each of the 3 climate categories.
     */
    fun isPostTestUnlocked(units: List<LearningUnit>, completedIds: Set<String>): Boolean {
        if (getCompletedCount(units, completedIds) < REQUIRED_TOTAL_FOR_POST_TEST) return false
        return getCompletedCountByCategory(units, completedIds, CATEGORY_HEAVY_RAIN) >= REQUIRED_PER_CATEGORY_FOR_POST_TEST &&
            getCompletedCountByCategory(units, completedIds, CATEGORY_EXTREME_HEAT) >= REQUIRED_PER_CATEGORY_FOR_POST_TEST &&
            getCompletedCountByCategory(units, completedIds, CATEGORY_HAZE) >= REQUIRED_PER_CATEGORY_FOR_POST_TEST
    }

    /**
     * Finds a unit by ID (case-insensitive trim match) for deep-linking from Assessment/Quiz.
     */
    fun resolveUnitById(units: List<LearningUnit>, unitId: String?): LearningUnit? {
        val normalized = unitId?.trim().orEmpty()
        if (normalized.isEmpty()) return null
        return units.firstOrNull { it.id.equals(normalized, ignoreCase = true) }
    }
}
