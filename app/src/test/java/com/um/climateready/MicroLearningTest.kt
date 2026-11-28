package com.um.climateready

import androidx.lifecycle.SavedStateHandle
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.LearningDataHelper
import com.um.climateready.feature.learning.LearningViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * JUnit unit tests for Module 2 — Climate Micro-Learning.
 *
 * Covers:
 * - GROUP A: Seed JSON schema, curriculum completeness, and PDF-artifact cleanliness
 * - GROUP B: Progress tracking, duplicate toggle idempotency, and post-test unlock boundaries
 * - GROUP C: Corrupt/empty JSON handling, filter state persistence, and deep-link resolution
 *
 * Run individually:
 *   ./gradlew testDebugUnitTest --tests "*MicroLearningTest*"
 */
class MicroLearningTest {

    private val assetsDir = File("src/main/assets/data")
    private val jsonFile = File(assetsDir, "m2_microlearning_units.json")

    private lateinit var units: List<LearningUnit>

    @Before
    fun setUp() {
        assertTrue("m2_microlearning_units.json must exist in assets/data/", jsonFile.exists())
        units = LearningDataHelper.parseLearningUnitsJson(jsonFile.readText())
    }

    // =========================================================================
    // GROUP A — JSON Parsing & Curriculum Integrity Tests
    // =========================================================================

    @Test
    fun jsonLoadsExactly12Units() {
        assertEquals("Must load exactly 12 micro-learning units", 12, units.size)
    }

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

    @Test
    fun seedCurriculumContainsNoPdfPageFooterArtifacts() {
        val rawText = jsonFile.readText()
        assertFalse(
            "Seed JSON should not contain leftover PDF footer markers",
            rawText.contains("Module 2 Micro-Learning Curriculum")
        )
        units.forEach { unit ->
            val fields = listOf(
                unit.title,
                unit.summary,
                unit.realWorldExample,
                unit.umStudentTip,
                unit.takeaway,
                unit.knowledgeCheck?.explanation.orEmpty()
            )
            fields.forEach { text ->
                assertFalse("Field in ${unit.id} must not contain 'Page ' footer", Regex("""Page \d+ of \d+""").containsMatchIn(text))
            }
        }
    }

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

    @Test
    fun categoryDistributionIsExactly4PerCategory() {
        val rainCount = units.count { it.category == LearningDataHelper.CATEGORY_HEAVY_RAIN }
        val heatCount = units.count { it.category == LearningDataHelper.CATEGORY_EXTREME_HEAT }
        val hazeCount = units.count { it.category == LearningDataHelper.CATEGORY_HAZE }

        assertEquals("Must have exactly 4 HEAVY_RAIN units", 4, rainCount)
        assertEquals("Must have exactly 4 EXTREME_HEAT units", 4, heatCount)
        assertEquals("Must have exactly 4 HAZE_AIR_QUALITY units", 4, hazeCount)
    }

    @Test
    fun globalOrderValuesAreUnique1Through12() {
        val orders = units.map { it.globalOrder }.sorted()
        assertEquals((1..12).toList(), orders)
    }

    @Test
    fun unitIdsFollowNamingConvention() {
        val validPrefixes = listOf("RAIN_", "HEAT_", "HAZE_")
        units.forEach { unit ->
            assertTrue(
                "Unit ID '${unit.id}' does not follow RAIN_xx / HEAT_xx / HAZE_xx convention",
                validPrefixes.any { unit.id.startsWith(it) }
            )
        }
        val ids = units.map { it.id }.toSet()
        listOf("RAIN_01", "RAIN_04", "HEAT_01", "HEAT_04", "HAZE_01", "HAZE_04").forEach { expected ->
            assertTrue("Expected unit ID '$expected' not found in JSON", expected in ids)
        }
    }

    // =========================================================================
    // GROUP B — Progress Tracking & Duplicate Toggle Tests
    // =========================================================================

    private fun freshUnits(): MutableList<LearningUnit> =
        units.map { it.copy(isCompleted = false) }.toMutableList()

    @Test
    fun filteredUnitsReturnsAll12WhenCategoryIsAll() {
        val all = freshUnits()
        assertEquals(12, LearningDataHelper.filterUnits(all, LearningDataHelper.CATEGORY_ALL).size)
        assertEquals(12, LearningDataHelper.filterUnits(all, null).size)
        assertEquals(12, LearningDataHelper.filterUnits(all, "   ").size)
        assertEquals(12, LearningDataHelper.filterUnits(all, "UNKNOWN_CATEGORY").size)
    }

    @Test
    fun filteredUnitsReturnsOnly4HeavyRainUnits() {
        val rainOnly = LearningDataHelper.filterUnits(freshUnits(), LearningDataHelper.CATEGORY_HEAVY_RAIN)
        assertEquals(4, rainOnly.size)
        rainOnly.forEach { assertEquals(LearningDataHelper.CATEGORY_HEAVY_RAIN, it.category) }
    }

    @Test
    fun completedCountStartsAtZeroForFreshList() {
        val all = freshUnits()
        assertEquals(0, LearningDataHelper.getCompletedCount(all, emptySet()))
    }

    @Test
    fun completedCountIncrementsCorrectlyAfterMarkingComplete() {
        val all = freshUnits()
        var completed = emptySet<String>()
        listOf("RAIN_01", "HEAT_01", "HAZE_01").forEach { id ->
            completed = LearningDataHelper.toggleUnitCompletion(all, completed, id, true)
        }
        assertEquals(3, LearningDataHelper.getCompletedCount(all, completed))
        assertEquals(3, all.count { it.isCompleted })
    }

    @Test
    fun duplicateCompletionTogglesAreIdempotentAndIgnoreUnknownIds() {
        val all = freshUnits()
        var completed = setOf("UNKNOWN_99", "")

        // Unknown/blank IDs must be ignored
        assertEquals(0, LearningDataHelper.getCompletedCount(all, completed))

        // Duplicate mark-complete on RAIN_01 must count as 1
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "RAIN_01", true)
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "RAIN_01", true)
        assertEquals(1, LearningDataHelper.getCompletedCount(all, completed))
        assertTrue(all.first { it.id == "RAIN_01" }.isCompleted)

        // Toggle off -> off again (duplicate false) -> back on
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "RAIN_01", false)
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "RAIN_01", false)
        assertEquals(0, LearningDataHelper.getCompletedCount(all, completed))
        assertFalse(all.first { it.id == "RAIN_01" }.isCompleted)

        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "RAIN_01", true)
        assertEquals(1, LearningDataHelper.getCompletedCount(all, completed))
        assertTrue(all.first { it.id == "RAIN_01" }.isCompleted)

        // Attempting to toggle non-existent ID must not alter completed set
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "NON_EXISTENT", true)
        assertEquals(setOf("RAIN_01"), completed)
    }

    @Test
    fun postTestUnlockRequires9TotalAnd2PerCategory() {
        val all = freshUnits()
        var completed = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03", "RAIN_04",
            "HEAT_01", "HEAT_02",
            "HAZE_01", "HAZE_02"
        )
        // 8 total (4 RAIN, 2 HEAT, 2 HAZE) -> locked
        assertFalse("8 completed must NOT unlock post-test", LearningDataHelper.isPostTestUnlocked(all, completed))

        // 9 total (4 RAIN, 3 HEAT, 2 HAZE) -> unlocked
        completed = LearningDataHelper.toggleUnitCompletion(all, completed, "HEAT_03", true)
        assertTrue("9 completed with >=2 per category MUST unlock post-test", LearningDataHelper.isPostTestUnlocked(all, completed))

        // 9 total with only 1 HAZE (4 RAIN, 4 HEAT, 1 HAZE) -> locked
        val imbalanced = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03", "RAIN_04",
            "HEAT_01", "HEAT_02", "HEAT_03", "HEAT_04",
            "HAZE_01"
        )
        assertFalse("9 completed with <2 in HAZE must NOT unlock post-test", LearningDataHelper.isPostTestUnlocked(all, imbalanced))
    }

    // =========================================================================
    // GROUP C — Corrupt/Empty JSON, Filter State Persistence & Deep-Link Tests
    // =========================================================================

    @Test
    fun parseLearningUnitsHandlesEmptyAndCorruptJsonSafely() {
        assertTrue(LearningDataHelper.parseLearningUnitsJson(null).isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("").isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("   \n\t  ").isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("null").isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("[]").isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("{}").isEmpty())
        assertTrue(LearningDataHelper.parseLearningUnitsJson("{malformed json [").isEmpty())
    }

    @Test
    fun parseLearningUnitsDropsIncompleteRecordsWhileKeepingValidOnes() {
        val mixedJson = """
            [
              {
                "id": "",
                "globalOrder": 1,
                "categoryOrder": 1,
                "category": "HEAVY_RAIN",
                "title": "Missing ID"
              },
              {
                "id": "BAD_CAT",
                "globalOrder": 2,
                "categoryOrder": 1,
                "category": "EARTHQUAKE",
                "title": "Invalid Category"
              },
              {
                "id": "RAIN_01",
                "globalOrder": 1,
                "categoryOrder": 1,
                "category": "HEAVY_RAIN",
                "title": "Understanding Heavy Rainfall",
                "summary": "Valid summary",
                "realWorldExample": "Valid example",
                "umStudentTip": "Valid tip",
                "takeaway": "Valid takeaway"
              }
            ]
        """.trimIndent()

        val parsed = LearningDataHelper.parseLearningUnitsJson(mixedJson)
        assertEquals(1, parsed.size)
        assertEquals("RAIN_01", parsed.first().id)
        assertNull(parsed.first().knowledgeCheck)
    }

    @Test
    fun learningViewModelPersistsFilterStateAcrossRecreation() {
        val handle = SavedStateHandle()
        val vmBeforeRotation = LearningViewModel(handle)

        assertEquals(LearningDataHelper.CATEGORY_ALL, vmBeforeRotation.selectedCategory)

        vmBeforeRotation.setSelectedCategory(LearningDataHelper.CATEGORY_EXTREME_HEAT)
        assertEquals(LearningDataHelper.CATEGORY_EXTREME_HEAT, vmBeforeRotation.selectedCategory)

        // Simulate recreation with the same SavedStateHandle state
        val vmAfterRotation = LearningViewModel(handle)
        assertEquals(LearningDataHelper.CATEGORY_EXTREME_HEAT, vmAfterRotation.selectedCategory)

        // Invalid category normalizes cleanly back to ALL
        vmAfterRotation.setSelectedCategory("INVALID")
        assertEquals(LearningDataHelper.CATEGORY_ALL, vmAfterRotation.selectedCategory)
    }

    @Test
    fun learningViewModelConsumesDeepLinkOnlyOnceAndResolvesValidUnits() {
        val handle = SavedStateHandle()
        val viewModel = LearningViewModel(handle)

        // First consumption succeeds; second (e.g. after rotation) returns null
        assertEquals("HEAT_02", viewModel.consumeDeepLinkUnitId("HEAT_02"))
        assertNull(viewModel.consumeDeepLinkUnitId("HEAT_02"))

        // Explicit reset allows a subsequent deep-link from onNewIntent
        viewModel.resetDeepLinkHistory()
        assertEquals("HAZE_01", viewModel.consumeDeepLinkUnitId("HAZE_01"))

        // Unit resolution helper handles trimming/casing and missing IDs safely
        val resolved = LearningDataHelper.resolveUnitById(units, " rain_03 ")
        assertNotNull(resolved)
        assertEquals("RAIN_03", resolved?.id)
        assertNull(LearningDataHelper.resolveUnitById(units, "DOES_NOT_EXIST"))
        assertNull(LearningDataHelper.resolveUnitById(units, ""))
    }
}
