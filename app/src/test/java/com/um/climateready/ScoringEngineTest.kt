package com.um.climateready

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.model.Option
import com.um.climateready.core.util.AssessmentAttempt
import com.um.climateready.core.util.ResultColorMapper
import com.um.climateready.core.util.ScoreBand
import com.um.climateready.core.util.ScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ScoringEngineTest {

    private val gson = Gson()
    private val assetsDir = File("src/main/assets/data")

    private fun loadSeedQuestions(type: String): List<AssessmentQuestion> {
        val file = File(assetsDir, "m1_assessment_questions.json")
        val listType = object : TypeToken<List<AssessmentQuestion>>() {}.type
        val all: List<AssessmentQuestion> = gson.fromJson(file.readText(), listType)
        return all.filter { it.type == type }
    }

    private fun loadSeedUnits(): List<LearningUnit> {
        val file = File(assetsDir, "m2_microlearning_units.json")
        val listType = object : TypeToken<List<LearningUnit>>() {}.type
        return gson.fromJson(file.readText(), listType)
    }

    private fun sampleQuestion(id: String, category: String, correct: String) = AssessmentQuestion(
        id = id,
        type = "PRE",
        category = category,
        title = "Title $id",
        question = "Question $id?",
        options = listOf(
            Option("A", "Opt A"),
            Option("B", "Opt B"),
            Option("C", "Opt C"),
            Option("D", "Opt D")
        ),
        correctOptionId = correct,
        explanation = "Explanation $id",
        linkedLearningUnitId = "${category.take(4)}_01"
    )

    private fun wrongOption(correct: String): String = if (correct == "A") "B" else "A"

    @Test
    fun exhaustiveAll512Combinations_scoresAndColorBandsAreExact() {
        val preQuestions = loadSeedQuestions("PRE")
        assertEquals(9, preQuestions.size)

        val rainQuestions = preQuestions.filter { it.category == ScoringEngine.CATEGORY_HEAVY_RAIN }
        val heatQuestions = preQuestions.filter { it.category == ScoringEngine.CATEGORY_EXTREME_HEAT }
        val hazeQuestions = preQuestions.filter { it.category == ScoringEngine.CATEGORY_HAZE }
        assertEquals(3, rainQuestions.size)
        assertEquals(3, heatQuestions.size)
        assertEquals(3, hazeQuestions.size)

        val ordered = rainQuestions + heatQuestions + hazeQuestions

        for (mask in 0 until 512) {
            val answers = mutableMapOf<String, String>()
            var expectedRain = 0
            var expectedHeat = 0
            var expectedHaze = 0

            for (bit in 0 until 9) {
                val q = ordered[bit]
                val isCorrect = ((mask shr bit) and 1) == 1
                answers[q.id] = if (isCorrect) q.correctOptionId else wrongOption(q.correctOptionId)
                if (isCorrect) {
                    when (bit) {
                        in 0..2 -> expectedRain++
                        in 3..5 -> expectedHeat++
                        in 6..8 -> expectedHaze++
                    }
                }
            }

            val result = ScoringEngine.calculateScores(answers, ordered, ScoringEngine.TYPE_PRE)

            assertEquals("Mask $mask rain score", expectedRain, result.heavyRainScore)
            assertEquals("Mask $mask heat score", expectedHeat, result.extremeHeatScore)
            assertEquals("Mask $mask haze score", expectedHaze, result.hazeScore)
            assertEquals("Mask $mask total score", expectedRain + expectedHeat + expectedHaze, result.totalScore)

            val byCat = result.categorySummaries.associateBy { it.category }
            assertEquals(expectedBand(expectedRain), byCat[ScoringEngine.CATEGORY_HEAVY_RAIN]?.band)
            assertEquals(expectedBand(expectedHeat), byCat[ScoringEngine.CATEGORY_EXTREME_HEAT]?.band)
            assertEquals(expectedBand(expectedHaze), byCat[ScoringEngine.CATEGORY_HAZE]?.band)
        }
    }

    private fun expectedBand(categoryScore: Int): ScoreBand {
        return when (categoryScore) {
            0, 1 -> ScoreBand.RED
            2 -> ScoreBand.YELLOW
            3 -> ScoreBand.GREEN
            else -> error("Unexpected score $categoryScore")
        }
    }

    @Test
    fun colorBandBoundaries_matchSpecification() {
        assertEquals(ScoreBand.RED, ResultColorMapper.mapCategoryScore(0))
        assertEquals(ScoreBand.RED, ResultColorMapper.mapCategoryScore(1))
        assertEquals(ScoreBand.YELLOW, ResultColorMapper.mapCategoryScore(2))
        assertEquals(ScoreBand.GREEN, ResultColorMapper.mapCategoryScore(3))

        assertEquals("Needs Urgent Review", ScoreBand.RED.label)
        assertEquals("Basic Understanding", ScoreBand.YELLOW.label)
        assertEquals("Sound Awareness", ScoreBand.GREEN.label)
    }

    @Test
    fun calculateScores_handlesAllCorrectAllWrongUnansweredAndCaseInsensitivity() {
        val postQuestions = loadSeedQuestions("POST")
        assertEquals(9, postQuestions.size)

        val allCorrectLowercase = postQuestions.associate { it.id to it.correctOptionId.lowercase() }
        val maxResult = ScoringEngine.calculateScores(allCorrectLowercase, postQuestions, ScoringEngine.TYPE_POST)
        assertEquals(9, maxResult.totalScore)
        assertEquals(3, maxResult.heavyRainScore)
        assertEquals(3, maxResult.extremeHeatScore)
        assertEquals(3, maxResult.hazeScore)
        assertEquals(ScoreBand.GREEN, maxResult.totalBand)

        val emptyResult = ScoringEngine.calculateScores(emptyMap(), postQuestions, ScoringEngine.TYPE_POST)
        assertEquals(0, emptyResult.totalScore)
        assertEquals(0, emptyResult.heavyRainScore)
        assertEquals(0, emptyResult.extremeHeatScore)
        assertEquals(0, emptyResult.hazeScore)
        assertEquals(ScoreBand.RED, emptyResult.totalBand)
    }

    @Test
    fun recommendation_selectsFirstIncompleteUnitInLowestCategory() {
        val units = loadSeedUnits()
        val questions = loadSeedQuestions("PRE")

        // Rain: 3/3, Heat: 1/3, Haze: 2/3 -> Lowest is EXTREME_HEAT
        val answers = questions.associate { q ->
            val pickCorrect = when (q.category) {
                ScoringEngine.CATEGORY_HEAVY_RAIN -> true
                ScoringEngine.CATEGORY_EXTREME_HEAT -> q.id == "PRE_HEAT_01"
                ScoringEngine.CATEGORY_HAZE -> q.id != "PRE_HAZE_03"
                else -> false
            }
            q.id to if (pickCorrect) q.correctOptionId else wrongOption(q.correctOptionId)
        }

        val result = ScoringEngine.calculateScores(answers, questions)
        val rec1 = ScoringEngine.recommendLearningUnit(result, units, emptySet())
        assertEquals("HEAT_01", rec1.recommendedUnit?.id)
        assertFalse(rec1.allUnitsCompleted)

        // When HEAT_01 is already completed, recommend HEAT_02
        val rec2 = ScoringEngine.recommendLearningUnit(result, units, setOf("HEAT_01"))
        assertEquals("HEAT_02", rec2.recommendedUnit?.id)
    }

    @Test
    fun recommendation_tieBreakingFollowsRainThenHeatThenHaze() {
        val units = loadSeedUnits()
        val questions = loadSeedQuestions("PRE")

        // All 0 -> tie across all 3 -> HEAVY_RAIN wins -> RAIN_01
        val allWrong = ScoringEngine.calculateScores(emptyMap(), questions)
        val recRain = ScoringEngine.recommendLearningUnit(allWrong, units, emptySet())
        assertEquals("RAIN_01", recRain.recommendedUnit?.id)

        // Rain = 3, Heat = 1, Haze = 1 -> tie between Heat & Haze -> EXTREME_HEAT wins -> HEAT_01
        val heatHazeTieAnswers = questions.associate { q ->
            val correct = when (q.category) {
                ScoringEngine.CATEGORY_HEAVY_RAIN -> true
                ScoringEngine.CATEGORY_EXTREME_HEAT -> q.id == "PRE_HEAT_01"
                ScoringEngine.CATEGORY_HAZE -> q.id == "PRE_HAZE_01"
                else -> false
            }
            q.id to if (correct) q.correctOptionId else wrongOption(q.correctOptionId)
        }
        val heatHazeTie = ScoringEngine.calculateScores(heatHazeTieAnswers, questions)
        val recHeat = ScoringEngine.recommendLearningUnit(heatHazeTie, units, emptySet())
        assertEquals("HEAT_01", recHeat.recommendedUnit?.id)

        // If lowest category (HEAVY_RAIN) is completely done, fall through to EXTREME_HEAT
        val allRainDone = setOf("RAIN_01", "RAIN_02", "RAIN_03", "RAIN_04")
        val recFallback = ScoringEngine.recommendLearningUnit(allWrong, units, allRainDone)
        assertEquals("HEAT_01", recFallback.recommendedUnit?.id)

        // If all 12 units are completed, recommendedUnit is null and allUnitsCompleted is true
        val allUnitIds = units.map { it.id }.toSet()
        val recComplete = ScoringEngine.recommendLearningUnit(allWrong, units, allUnitIds)
        assertNull(recComplete.recommendedUnit)
        assertTrue(recComplete.allUnitsCompleted)
    }

    @Test
    fun postTestUnlock_enforcesBothTotalAndPerCategoryThresholds() {
        val units = loadSeedUnits()

        // 0 units -> locked
        assertFalse(ScoringEngine.isPostTestUnlocked(units, emptySet()))

        // 8 units total (3 Rain, 3 Heat, 2 Haze) -> locked because total < 9
        val eightUnits = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03",
            "HEAT_01", "HEAT_02", "HEAT_03",
            "HAZE_01", "HAZE_02"
        )
        assertFalse(ScoringEngine.isPostTestUnlocked(units, eightUnits))

        // 9 units total (4 Rain, 4 Heat, 1 Haze) -> locked because Haze < 2
        val nineImbalanced = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03", "RAIN_04",
            "HEAT_01", "HEAT_02", "HEAT_03", "HEAT_04",
            "HAZE_01"
        )
        assertFalse(ScoringEngine.isPostTestUnlocked(units, nineImbalanced))

        // 9 units total (4 Rain, 3 Heat, 2 Haze) -> UNLOCKED
        val nineBalanced = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03", "RAIN_04",
            "HEAT_01", "HEAT_02", "HEAT_03",
            "HAZE_01", "HAZE_02"
        )
        val statusUnlocked = ScoringEngine.evaluatePostTestUnlock(units, nineBalanced)
        assertTrue(statusUnlocked.isUnlocked)
        assertEquals(9, statusUnlocked.completedTotal)
        assertEquals(4, statusUnlocked.completedRain)
        assertEquals(3, statusUnlocked.completedHeat)
        assertEquals(2, statusUnlocked.completedHaze)

        // 9 units (3+3+3) -> UNLOCKED
        val threeEach = setOf(
            "RAIN_01", "RAIN_02", "RAIN_03",
            "HEAT_01", "HEAT_02", "HEAT_03",
            "HAZE_01", "HAZE_02", "HAZE_03"
        )
        assertTrue(ScoringEngine.isPostTestUnlocked(units, threeEach))

        // Bogus unit IDs do not falsely unlock
        val bogusPlusEight = eightUnits + setOf("INVALID_01", "FAKE_02")
        assertFalse(ScoringEngine.isPostTestUnlocked(units, bogusPlusEight))
    }

    @Test
    fun comparisonAndShareableSummary_computeExactDeltasAndExcludePersonalData() {
        val pre = AssessmentAttempt(
            id = "attempt-pre-secret-id",
            assessmentType = ScoringEngine.TYPE_PRE,
            startedAt = 1000L,
            completedAt = 2000L,
            totalScore = 4,
            heavyRainScore = 1,
            extremeHeatScore = 2,
            hazeScore = 1,
            answers = emptyList()
        )
        val post = AssessmentAttempt(
            id = "attempt-post-secret-id",
            assessmentType = ScoringEngine.TYPE_POST,
            startedAt = 3000L,
            completedAt = 4000L,
            totalScore = 8,
            heavyRainScore = 3,
            extremeHeatScore = 2,
            hazeScore = 3,
            answers = emptyList()
        )

        val comparison = ScoringEngine.comparePreAndPost(pre, post)
        assertEquals(4, comparison.totalDelta)
        assertEquals(2, comparison.rainDelta)
        assertEquals(0, comparison.heatDelta)
        assertEquals(2, comparison.hazeDelta)
        assertFalse(
            "Neutral interpretation must not overclaim long-term behaviour change",
            comparison.neutralInterpretation.contains("behaviour change", ignoreCase = true)
        )

        val sharedComparison = ScoringEngine.buildShareableSummary(pre, post)
        assertTrue(sharedComparison.contains("4/9 -> 8/9 (+4)"))
        assertTrue(sharedComparison.contains("Heavy Rain & Flooding: 1/3 -> 3/3 (+2)"))
        assertFalse("Must not leak attempt ID", sharedComparison.contains("attempt-pre-secret-id"))
        assertFalse("Must not contain email symbol", sharedComparison.contains("@"))
        assertFalse("Must not contain matric label", sharedComparison.contains("matric", ignoreCase = true))
        assertFalse("Must not contain student ID label", sharedComparison.contains("student id", ignoreCase = true))

        val sharedSingle = ScoringEngine.buildShareableSummary(pre)
        assertTrue(sharedSingle.contains("Total Score: 4/9"))
        assertTrue(sharedSingle.contains("Needs Urgent Review"))
        assertFalse(sharedSingle.contains("@"))
    }

    @Test
    fun sanitizeDisplayText_stripsPdfPageFootersFromSeedQuestions() {
        val allQuestions = loadSeedQuestions("PRE") + loadSeedQuestions("POST")
        assertEquals(18, allQuestions.size)

        allQuestions.forEach { q ->
            val cleanExplanation = ScoringEngine.sanitizeDisplayText(q.explanation)
            assertNotNull(cleanExplanation)
            assertTrue(cleanExplanation.isNotBlank())
            assertFalse("Footer leaked in ${q.id}", cleanExplanation.contains("Page 1 of 5"))
            assertFalse("Footer leaked in ${q.id}", cleanExplanation.contains("Page 2 of 5"))
            assertFalse("Footer leaked in ${q.id}", cleanExplanation.contains("Page 3 of 5"))
            assertFalse("Footer leaked in ${q.id}", cleanExplanation.contains("Page 4 of 5"))
            assertFalse("Trailing POST leaked in ${q.id}", cleanExplanation.endsWith(" POST"))
        }
    }
}
