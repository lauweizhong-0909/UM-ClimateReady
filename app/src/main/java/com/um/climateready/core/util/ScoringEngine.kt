package com.um.climateready.core.util

import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.model.LearningUnit

data class AssessmentAnswerRecord(
    val questionId: String,
    val selectedOptionId: String,
    val correctOptionId: String,
    val isCorrect: Boolean,
    val answeredAt: Long
)

data class AssessmentAttempt(
    val id: String,
    val assessmentType: String, // "PRE" or "POST"
    val startedAt: Long,
    val completedAt: Long,
    val totalScore: Int,
    val heavyRainScore: Int,
    val extremeHeatScore: Int,
    val hazeScore: Int,
    val answers: List<AssessmentAnswerRecord>
)

data class CategoryScoreSummary(
    val category: String,
    val displayName: String,
    val score: Int,
    val maxScore: Int = ScoringEngine.QUESTIONS_PER_CATEGORY,
    val band: ScoreBand
)

data class QuestionEvaluation(
    val questionId: String,
    val title: String,
    val category: String,
    val questionText: String,
    val selectedOptionId: String?,
    val selectedOptionText: String?,
    val correctOptionId: String,
    val correctOptionText: String,
    val isCorrect: Boolean,
    val explanation: String,
    val linkedLearningUnitId: String
)

data class AssessmentResult(
    val assessmentType: String,
    val totalScore: Int,
    val maxScore: Int,
    val heavyRainScore: Int,
    val extremeHeatScore: Int,
    val hazeScore: Int,
    val totalBand: ScoreBand,
    val categorySummaries: List<CategoryScoreSummary>,
    val questionEvaluations: List<QuestionEvaluation>
)

data class RecommendationResult(
    val recommendedUnit: LearningUnit?,
    val targetCategory: String?,
    val prioritizedCategories: List<String>,
    val allUnitsCompleted: Boolean,
    val reason: String
)

data class PostTestUnlockStatus(
    val isUnlocked: Boolean,
    val completedTotal: Int,
    val requiredTotal: Int = ScoringEngine.REQUIRED_TOTAL_UNITS,
    val completedRain: Int,
    val completedHeat: Int,
    val completedHaze: Int,
    val requiredPerCategory: Int = ScoringEngine.REQUIRED_UNITS_PER_CATEGORY
)

data class AssessmentComparison(
    val preTotal: Int,
    val postTotal: Int,
    val totalDelta: Int,
    val rainPre: Int,
    val rainPost: Int,
    val rainDelta: Int,
    val heatPre: Int,
    val heatPost: Int,
    val heatDelta: Int,
    val hazePre: Int,
    val hazePost: Int,
    val hazeDelta: Int,
    val neutralInterpretation: String
)

object ScoringEngine {

    const val TYPE_PRE = "PRE"
    const val TYPE_POST = "POST"

    const val CATEGORY_HEAVY_RAIN = "HEAVY_RAIN"
    const val CATEGORY_EXTREME_HEAT = "EXTREME_HEAT"
    const val CATEGORY_HAZE = "HAZE_AIR_QUALITY"

    const val QUESTIONS_PER_CATEGORY = 3
    const val TOTAL_ASSESSMENT_QUESTIONS = 9
    const val REQUIRED_TOTAL_UNITS = 9
    const val REQUIRED_UNITS_PER_CATEGORY = 2

    val CATEGORY_PRIORITY: List<String> = listOf(
        CATEGORY_HEAVY_RAIN,
        CATEGORY_EXTREME_HEAT,
        CATEGORY_HAZE
    )

    private val PDF_FOOTER_REGEX = Regex(
        """(?m)\s*UM ClimateReady\s*[•·\-]\s*Module 1 Assessment Bank\s*\n?\s*Page \d+ of \d+\s*"""
    )
    private val TRAILING_POST_ARTIFACT_REGEX = Regex("""\s*\bPOST\s*$""")
    private val MULTI_WHITESPACE_REGEX = Regex("""\s+""")

    /**
     * Pure deterministic scoring for Module 1 (Pre or Post assessment).
     * Each correct MCQ earns 1 point; each category scores 0-3 and total scores 0-9.
     */
    fun calculateScores(
        answers: Map<String, String>,
        questions: List<AssessmentQuestion>,
        assessmentType: String = questions.firstOrNull()?.type ?: TYPE_PRE
    ): AssessmentResult {
        var rainScore = 0
        var heatScore = 0
        var hazeScore = 0

        val evaluations = questions.map { q ->
            val rawSelected = answers[q.id]?.trim()?.uppercase()
            val normalizedCorrect = q.correctOptionId.trim().uppercase()
            val isCorrect = rawSelected != null && rawSelected == normalizedCorrect

            if (isCorrect) {
                when (q.category) {
                    CATEGORY_HEAVY_RAIN -> rainScore++
                    CATEGORY_EXTREME_HEAT -> heatScore++
                    CATEGORY_HAZE -> hazeScore++
                }
            }

            val selectedText = rawSelected?.let { id ->
                q.options.firstOrNull { it.id.equals(id, ignoreCase = true) }?.text
                    ?.let { sanitizeDisplayText(it) }
            }
            val correctText = q.options.firstOrNull { it.id.equals(normalizedCorrect, ignoreCase = true) }?.text
                ?.let { sanitizeDisplayText(it) }
                ?: ""

            QuestionEvaluation(
                questionId = q.id,
                title = sanitizeDisplayText(q.title),
                category = q.category,
                questionText = sanitizeDisplayText(q.question),
                selectedOptionId = rawSelected,
                selectedOptionText = selectedText,
                correctOptionId = normalizedCorrect,
                correctOptionText = correctText,
                isCorrect = isCorrect,
                explanation = sanitizeDisplayText(q.explanation),
                linkedLearningUnitId = q.linkedLearningUnitId
            )
        }

        val totalScore = rainScore + heatScore + hazeScore
        val summaries = listOf(
            CategoryScoreSummary(
                category = CATEGORY_HEAVY_RAIN,
                displayName = ResultColorMapper.categoryDisplayName(CATEGORY_HEAVY_RAIN),
                score = rainScore,
                maxScore = QUESTIONS_PER_CATEGORY,
                band = ResultColorMapper.mapCategoryScore(rainScore)
            ),
            CategoryScoreSummary(
                category = CATEGORY_EXTREME_HEAT,
                displayName = ResultColorMapper.categoryDisplayName(CATEGORY_EXTREME_HEAT),
                score = heatScore,
                maxScore = QUESTIONS_PER_CATEGORY,
                band = ResultColorMapper.mapCategoryScore(heatScore)
            ),
            CategoryScoreSummary(
                category = CATEGORY_HAZE,
                displayName = ResultColorMapper.categoryDisplayName(CATEGORY_HAZE),
                score = hazeScore,
                maxScore = QUESTIONS_PER_CATEGORY,
                band = ResultColorMapper.mapCategoryScore(hazeScore)
            )
        )

        return AssessmentResult(
            assessmentType = assessmentType,
            totalScore = totalScore,
            maxScore = questions.size,
            heavyRainScore = rainScore,
            extremeHeatScore = heatScore,
            hazeScore = hazeScore,
            totalBand = ResultColorMapper.mapTotalScore(totalScore),
            categorySummaries = summaries,
            questionEvaluations = evaluations
        )
    }

    /**
     * Recommends the first incomplete learning unit in the lowest-scoring category.
     * Tie-breaking priority: HEAVY_RAIN > EXTREME_HEAT > HAZE_AIR_QUALITY.
     * If a lowest category has no incomplete units left, falls through deterministically
     * to the next lowest/tied category that has an incomplete unit.
     */
    fun recommendLearningUnit(
        result: AssessmentResult,
        learningUnits: List<LearningUnit>,
        completedUnitIds: Set<String>
    ): RecommendationResult {
        val scoreByCategory = mapOf(
            CATEGORY_HEAVY_RAIN to result.heavyRainScore,
            CATEGORY_EXTREME_HEAT to result.extremeHeatScore,
            CATEGORY_HAZE to result.hazeScore
        )

        val prioritizedCategories = CATEGORY_PRIORITY.sortedWith(
            compareBy<String> { scoreByCategory[it] ?: 0 }
                .thenBy { CATEGORY_PRIORITY.indexOf(it) }
        )

        for (category in prioritizedCategories) {
            val nextUnit = learningUnits
                .filter { it.category == category && !completedUnitIds.contains(it.id) }
                .sortedWith(compareBy<LearningUnit> { it.categoryOrder }.thenBy { it.globalOrder })
                .firstOrNull()

            if (nextUnit != null) {
                val catScore = scoreByCategory[category] ?: 0
                val catName = ResultColorMapper.categoryDisplayName(category)
                val band = ResultColorMapper.mapCategoryScore(catScore)
                val reason = if (catScore < QUESTIONS_PER_CATEGORY) {
                    "Recommended based on your $catName score ($catScore/3 — ${band.label})."
                } else {
                    "You scored well across all categories. Continue building depth with $catName."
                }
                return RecommendationResult(
                    recommendedUnit = nextUnit,
                    targetCategory = category,
                    prioritizedCategories = prioritizedCategories,
                    allUnitsCompleted = false,
                    reason = reason
                )
            }
        }

        return RecommendationResult(
            recommendedUnit = null,
            targetCategory = null,
            prioritizedCategories = prioritizedCategories,
            allUnitsCompleted = learningUnits.isNotEmpty(),
            reason = "All 12 micro-learning units are complete! Your Post-Assessment is ready."
        )
    }

    /**
     * Evaluates the deterministic Post-Test unlock condition from persisted completion records:
     * total completed units >= 9 AND >= 2 completed in each of the 3 categories.
     */
    fun evaluatePostTestUnlock(
        learningUnits: List<LearningUnit>,
        completedUnitIds: Set<String>
    ): PostTestUnlockStatus {
        val validCompletedUnits = learningUnits.filter { completedUnitIds.contains(it.id) }
        val completedTotal = validCompletedUnits.size
        val completedRain = validCompletedUnits.count { it.category == CATEGORY_HEAVY_RAIN }
        val completedHeat = validCompletedUnits.count { it.category == CATEGORY_EXTREME_HEAT }
        val completedHaze = validCompletedUnits.count { it.category == CATEGORY_HAZE }

        val unlocked = completedTotal >= REQUIRED_TOTAL_UNITS &&
            completedRain >= REQUIRED_UNITS_PER_CATEGORY &&
            completedHeat >= REQUIRED_UNITS_PER_CATEGORY &&
            completedHaze >= REQUIRED_UNITS_PER_CATEGORY

        return PostTestUnlockStatus(
            isUnlocked = unlocked,
            completedTotal = completedTotal,
            requiredTotal = REQUIRED_TOTAL_UNITS,
            completedRain = completedRain,
            completedHeat = completedHeat,
            completedHaze = completedHaze,
            requiredPerCategory = REQUIRED_UNITS_PER_CATEGORY
        )
    }

    fun isPostTestUnlocked(
        learningUnits: List<LearningUnit>,
        completedUnitIds: Set<String>
    ): Boolean {
        return evaluatePostTestUnlock(learningUnits, completedUnitIds).isUnlocked
    }

    /**
     * Compares Pre-Assessment and Post-Assessment attempts with a neutral, factual interpretation.
     * Deliberately avoids claiming that a score change proves long-term behaviour change.
     */
    fun comparePreAndPost(
        pre: AssessmentAttempt,
        post: AssessmentAttempt
    ): AssessmentComparison {
        val totalDelta = post.totalScore - pre.totalScore
        val rainDelta = post.heavyRainScore - pre.heavyRainScore
        val heatDelta = post.extremeHeatScore - pre.extremeHeatScore
        val hazeDelta = post.hazeScore - pre.hazeScore

        val interpretation = when {
            totalDelta > 0 -> {
                val pointsLabel = if (totalDelta == 1) "1 point" else "$totalDelta points"
                "Your post-assessment score is $pointsLabel higher than your initial pre-assessment (${pre.totalScore}/9 to ${post.totalScore}/9). This reflects improved recall on the assessed climate preparedness items after completing micro-learning units."
            }
            totalDelta == 0 -> {
                "Your post-assessment total score (${post.totalScore}/9) is the same as your initial pre-assessment (${pre.totalScore}/9). Check the category breakdown below to see any topic-level changes."
            }
            else -> {
                val diff = -totalDelta
                val pointsLabel = if (diff == 1) "1 point" else "$diff points"
                "Your post-assessment score is $pointsLabel lower than your initial pre-assessment (${pre.totalScore}/9 to ${post.totalScore}/9). Revisiting the micro-learning units for lower-scoring categories can help reinforce key preparedness actions."
            }
        }

        return AssessmentComparison(
            preTotal = pre.totalScore,
            postTotal = post.totalScore,
            totalDelta = totalDelta,
            rainPre = pre.heavyRainScore,
            rainPost = post.heavyRainScore,
            rainDelta = rainDelta,
            heatPre = pre.extremeHeatScore,
            heatPost = post.extremeHeatScore,
            heatDelta = heatDelta,
            hazePre = pre.hazeScore,
            hazePost = post.hazeScore,
            hazeDelta = hazeDelta,
            neutralInterpretation = interpretation
        )
    }

    fun formatDelta(delta: Int): String {
        return when {
            delta > 0 -> "+$delta"
            else -> delta.toString()
        }
    }

    /**
     * Builds a safe, anonymous shareable summary for Android sharing.
     * Strictly excludes personal identifiers (email, name, student/matric ID, device ID).
     */
    fun buildShareableSummary(
        preAttempt: AssessmentAttempt,
        postAttempt: AssessmentAttempt? = null
    ): String {
        return if (postAttempt != null) {
            val comparison = comparePreAndPost(preAttempt, postAttempt)
            buildString {
                appendLine("UM ClimateReady — Climate Awareness Assessment Comparison (SDG 13.3)")
                appendLine("Total Score: ${comparison.preTotal}/9 -> ${comparison.postTotal}/9 (${formatDelta(comparison.totalDelta)})")
                appendLine("- Heavy Rain & Flooding: ${comparison.rainPre}/3 -> ${comparison.rainPost}/3 (${formatDelta(comparison.rainDelta)})")
                appendLine("- Extreme Heat: ${comparison.heatPre}/3 -> ${comparison.heatPost}/3 (${formatDelta(comparison.heatDelta)})")
                appendLine("- Haze & Air Quality: ${comparison.hazePre}/3 -> ${comparison.hazePost}/3 (${formatDelta(comparison.hazeDelta)})")
                append("Shared anonymously from UM ClimateReady.")
            }
        } else {
            val typeTitle = if (preAttempt.assessmentType == TYPE_POST) "Post-Assessment" else "Pre-Assessment"
            val rainBand = ResultColorMapper.mapCategoryScore(preAttempt.heavyRainScore).label
            val heatBand = ResultColorMapper.mapCategoryScore(preAttempt.extremeHeatScore).label
            val hazeBand = ResultColorMapper.mapCategoryScore(preAttempt.hazeScore).label
            buildString {
                appendLine("UM ClimateReady — $typeTitle Result (SDG 13.3)")
                appendLine("Total Score: ${preAttempt.totalScore}/9")
                appendLine("- Heavy Rain & Flooding: ${preAttempt.heavyRainScore}/3 ($rainBand)")
                appendLine("- Extreme Heat: ${preAttempt.extremeHeatScore}/3 ($heatBand)")
                appendLine("- Haze & Air Quality: ${preAttempt.hazeScore}/3 ($hazeBand)")
                append("Shared anonymously from UM ClimateReady.")
            }
        }
    }

    /**
     * Normalizes seed JSON text by removing extracted PDF footer artifacts and collapsing
     * single line-wrap breaks while keeping sentences cleanly readable.
     */
    fun sanitizeDisplayText(raw: String): String {
        val withoutFooter = PDF_FOOTER_REGEX.replace(raw, " ")
        val withoutTrailingPost = TRAILING_POST_ARTIFACT_REGEX.replace(withoutFooter, "")
        return MULTI_WHITESPACE_REGEX.replace(withoutTrailingPost.trim(), " ")
    }
}
