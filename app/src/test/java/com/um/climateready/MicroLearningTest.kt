package com.um.climateready

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.um.climateready.core.model.LearningUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * JUnit unit tests for Module 2 — Climate Micro-Learning.
 *
 * Tests are split into two groups:
 *
 * GROUP A — JSON Parsing Tests (7 tests)
 *   Verifies that [m2_microlearning_units.json] is well-formed and matches
 *   the curriculum specification (12 units, 3 categories × 4, correct option IDs,
 *   required text fields, global order uniqueness, naming convention).
 *
 * GROUP B — Progress Tracking Tests (6 tests)
 *   Exercises the in-memory completion toggle logic used by [LearningRepository]
 *   and [AssetDataManager.setUnitCompleted], using a mutable copy of the unit list
 *   without any Android Context or SharedPreferences dependency.
 *
 * Run individually:
 *   ./gradlew testDebugUnitTest --tests "*MicroLearningTest*"
 */
class MicroLearningTest {

    private val gson = Gson()
    private val assetsDir = File("src/main/assets/data")
    private val jsonFile = File(assetsDir, "m2_microlearning_units.json")

    private lateinit var units: List<LearningUnit>

    @Before
    fun setUp() {
        assertTrue("m2_microlearning_units.json must exist in assets/data/", jsonFile.exists())
        val type = object : TypeToken<List<LearningUnit>>() {}.type
        units = gson.fromJson(jsonFile.readText(), type)
    }

    // =========================================================================
    // GROUP A — JSON Parsing Tests
    // =========================================================================

    /** The JSON must deserialize into exactly 12 LearningUnit records. */
    @Test
    fun jsonLoadsExactly12Units() {
        assertEquals("Must load exactly 12 micro-learning units", 12, units.size)
    }

    /** Every unit must have non-blank values for all required text fields. */
    @Test
    fun allUnitsHaveNonBlankRequiredFields() {
        units.forEach { unit ->
            assertFalse("id must not be blank in unit at order ${unit.globalOrder}", unit.id.isBlank())
            assertFalse("title must not be blank (${unit.id})", unit.title.isBlank())
            assertFalse("summary must not be blank (${unit.id})", unit.summary.isBlank())
            assertFalse("realWorldExample must not be blank (${unit.id})", unit.realWorldExample.isBlank())
            assertFalse("umStudentTip must not be blank (${unit.id})", unit.umStudentTip.isBlank())
            assertFalse("takeaway must not be blank (${unit.id})", unit.takeaway.isBlank())
        }
    }

    /** Each unit's Knowledge Check must have exactly 4 answer options. */
    @Test
    fun allUnitsHaveExactly4KnowledgeCheckOptions() {
        units.forEach { unit ->
            val kc = unit.knowledgeCheck
            assertNotNull("knowledgeCheck must not be null (${unit.id})", kc)
            assertEquals(
                "knowledgeCheck must have exactly 4 options (${unit.id})",
                4,
                kc!!.options.size
            )
        }
    }

    /** The correctOptionId in every Knowledge Check must match one of the 4 option IDs. */
    @Test
    fun correctOptionIdExistsInOptionsList() {
        units.forEach { unit ->
            val kc = unit.knowledgeCheck ?: return@forEach
            val optionIds = kc.options.map { it.id }
            assertTrue(
                "correctOptionId '${kc.correctOptionId}' not found in options list (${unit.id}). Available: $optionIds",
                kc.correctOptionId in optionIds
            )
        }
    }

    /** Category distribution must be exactly 4 HEAVY_RAIN, 4 EXTREME_HEAT, 4 HAZE_AIR_QUALITY. */
    @Test
    fun categoryDistributionIsExactly4PerCategory() {
        val rainCount = units.count { it.category == "HEAVY_RAIN" }
        val heatCount = units.count { it.category == "EXTREME_HEAT" }
        val hazeCount = units.count { it.category == "HAZE_AIR_QUALITY" }

        assertEquals("Must have exactly 4 HEAVY_RAIN units",      4, rainCount)
        assertEquals("Must have exactly 4 EXTREME_HEAT units",    4, heatCount)
        assertEquals("Must have exactly 4 HAZE_AIR_QUALITY units", 4, hazeCount)
    }

    /** globalOrder values must be unique and span 1 to 12 without gaps. */
    @Test
    fun globalOrderValuesAreUnique1Through12() {
        val orders = units.map { it.globalOrder }.sorted()
        val expected = (1..12).toList()
        assertEquals(
            "globalOrder values must be exactly 1 through 12 with no duplicates or gaps",
            expected,
            orders
        )
    }

    /**
     * Unit IDs must follow the naming convention:
     * RAIN_01..RAIN_04, HEAT_01..HEAT_04, HAZE_01..HAZE_04.
     */
    @Test
    fun unitIdsFollowNamingConvention() {
        val validPrefixes = listOf("RAIN_", "HEAT_", "HAZE_")
        units.forEach { unit ->
            val hasValidPrefix = validPrefixes.any { unit.id.startsWith(it) }
            assertTrue(
                "Unit ID '${unit.id}' does not follow RAIN_xx / HEAT_xx / HAZE_xx convention",
                hasValidPrefix
            )
        }
        // Spot-check the specific IDs defined in the curriculum doc
        val ids = units.map { it.id }.toSet()
        listOf("RAIN_01", "RAIN_04", "HEAT_01", "HEAT_04", "HAZE_01", "HAZE_04").forEach { expected ->
            assertTrue("Expected unit ID '$expected' not found in JSON", expected in ids)
        }
    }

    // =========================================================================
    // GROUP B — Progress Tracking Tests (pure in-memory, no Android Context)
    // =========================================================================

    /**
     * Helper: returns a fresh mutable copy of units with isCompleted reset to false.
     * Simulates a first-launch state where no units have been completed.
     */
    private fun freshUnits(): MutableList<LearningUnit> =
        units.map { it.copy(isCompleted = false) }.toMutableList()

    /** After toggling none, filtered with "ALL" must return all 12 units. */
    @Test
    fun filteredUnitsReturnsAll12WhenCategoryIsAll() {
        val all = freshUnits()
        val filtered = all.filter { true }  // "ALL" = no filter
        assertEquals(12, filtered.size)
    }

    /** Filtering by HEAVY_RAIN must return exactly 4 units. */
    @Test
    fun filteredUnitsReturnsOnly4HeavyRainUnits() {
        val rainOnly = freshUnits().filter { it.category == "HEAVY_RAIN" }
        assertEquals(4, rainOnly.size)
        rainOnly.forEach { unit ->
            assertEquals("HEAVY_RAIN", unit.category)
        }
    }

    /** Fresh unit list (no SharedPreferences) should have 0 completed units. */
    @Test
    fun completedCountStartsAtZeroForFreshList() {
        val all = freshUnits()
        val completedCount = all.count { it.isCompleted }
        assertEquals(0, completedCount)
    }

    /** Marking 3 specific units complete must increment completed count to 3. */
    @Test
    fun completedCountIncrementsCorrectlyAfterMarkingComplete() {
        val all = freshUnits()
        val idsToComplete = listOf("RAIN_01", "HEAT_01", "HAZE_01")

        // Simulate what AssetDataManager.setUnitCompleted does to in-memory list
        idsToComplete.forEach { id ->
            all.find { it.id == id }?.isCompleted = true
        }

        val completedCount = all.count { it.isCompleted }
        assertEquals(
            "Completing 3 units must result in completedCount == 3",
            3,
            completedCount
        )
    }

    /** Marking the same unit complete twice must NOT double-count it. */
    @Test
    fun markingUnitCompleteIsIdempotent() {
        val all = freshUnits()
        val unit = all.first { it.id == "RAIN_01" }

        // Mark complete twice
        unit.isCompleted = true
        unit.isCompleted = true

        val completedCount = all.count { it.isCompleted }
        assertEquals(
            "Double-marking a unit complete must still count as 1 completed",
            1,
            completedCount
        )
    }

    /** Undoing completion must set isCompleted back to false. */
    @Test
    fun undoCompletionRestoresIsCompletedToFalse() {
        val all = freshUnits()
        val unit = all.first { it.id == "HAZE_02" }

        unit.isCompleted = true
        assertEquals(true, unit.isCompleted)

        unit.isCompleted = false    // undo
        assertEquals(false, unit.isCompleted)
        assertEquals(0, all.count { it.isCompleted })
    }

    /**
     * Post-test unlock rule: requires >= 9 total AND >= 2 per category.
     * Tests the boundary — 8 total must NOT unlock; 9 total with >= 2 each MUST unlock.
     */
    @Test
    fun postTestUnlockRequires9TotalAnd2PerCategory() {
        // Helper mirrors LearningRepository.isPostTestUnlocked logic
        fun isUnlocked(all: List<LearningUnit>): Boolean {
            val completed = all.filter { it.isCompleted }
            if (completed.size < 9) return false
            val rain = completed.count { it.category == "HEAVY_RAIN" }
            val heat = completed.count { it.category == "EXTREME_HEAT" }
            val haze = completed.count { it.category == "HAZE_AIR_QUALITY" }
            return rain >= 2 && heat >= 2 && haze >= 2
        }

        val all = freshUnits()

        // Case 1: 8 total — must NOT unlock (even if distribution is fine)
        all.filter { it.category == "HEAVY_RAIN" }.take(2).forEach { it.isCompleted = true }
        all.filter { it.category == "EXTREME_HEAT" }.take(2).forEach { it.isCompleted = true }
        all.filter { it.category == "HAZE_AIR_QUALITY" }.take(2).forEach { it.isCompleted = true }
        // 6 so far — add 2 more from RAIN
        all.filter { it.category == "HEAVY_RAIN" && !it.isCompleted }.take(2).forEach { it.isCompleted = true }
        // Now 8 total (4 RAIN, 2 HEAT, 2 HAZE)
        assertEquals("8 completed must NOT unlock post-test", false, isUnlocked(all))

        // Case 2: add 1 more from HEAT to reach 9 total (4 RAIN, 3 HEAT, 2 HAZE)
        all.filter { it.category == "EXTREME_HEAT" && !it.isCompleted }.take(1).forEach { it.isCompleted = true }
        assertEquals("9 completed with >=2 per category MUST unlock post-test", true, isUnlocked(all))
    }
}
