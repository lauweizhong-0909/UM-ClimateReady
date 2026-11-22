package com.um.climateready.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.util.AssessmentAnswerRecord
import com.um.climateready.core.util.AssessmentAttempt
import com.um.climateready.core.util.AssessmentResult
import com.um.climateready.core.util.PostTestUnlockStatus
import com.um.climateready.core.util.ScoringEngine
import java.util.UUID

class AssessmentRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataManager = AssetDataManager.getInstance(appContext)
    private val gson = Gson()
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getQuestions(assessmentType: String): List<AssessmentQuestion> {
        return dataManager.assessmentQuestions.filter {
            it.type.equals(assessmentType, ignoreCase = true)
        }
    }

    fun getLearningUnits(): List<LearningUnit> {
        return dataManager.learningUnits
    }

    fun getCompletedUnitIds(): Set<String> {
        return dataManager.getCompletedUnitIds()
    }

    fun getPostTestUnlockStatus(): PostTestUnlockStatus {
        return ScoringEngine.evaluatePostTestUnlock(
            learningUnits = getLearningUnits(),
            completedUnitIds = getCompletedUnitIds()
        )
    }

    fun saveAttempt(
        assessmentType: String,
        startedAt: Long,
        completedAt: Long,
        result: AssessmentResult
    ): AssessmentAttempt {
        val answerRecords = result.questionEvaluations.map { eval ->
            AssessmentAnswerRecord(
                questionId = eval.questionId,
                selectedOptionId = eval.selectedOptionId ?: "",
                correctOptionId = eval.correctOptionId,
                isCorrect = eval.isCorrect,
                answeredAt = completedAt
            )
        }

        val attempt = AssessmentAttempt(
            id = UUID.randomUUID().toString(),
            assessmentType = assessmentType,
            startedAt = startedAt,
            completedAt = completedAt,
            totalScore = result.totalScore,
            heavyRainScore = result.heavyRainScore,
            extremeHeatScore = result.extremeHeatScore,
            hazeScore = result.hazeScore,
            answers = answerRecords
        )

        val key = if (assessmentType == ScoringEngine.TYPE_POST) KEY_LATEST_POST else KEY_LATEST_PRE
        prefs.edit().putString(key, gson.toJson(attempt)).apply()

        // Keep HomeFragment's shared summary in sync when Pre-Assessment is saved
        if (assessmentType == ScoringEngine.TYPE_PRE) {
            dataManager.savePreAssessmentScore(
                totalScore = attempt.totalScore,
                rainScore = attempt.heavyRainScore,
                heatScore = attempt.extremeHeatScore,
                hazeScore = attempt.hazeScore
            )
        }

        return attempt
    }

    fun getLatestPreAttempt(): AssessmentAttempt? {
        return readAttempt(KEY_LATEST_PRE)
    }

    fun getLatestPostAttempt(): AssessmentAttempt? {
        return readAttempt(KEY_LATEST_POST)
    }

    fun rebuildResultFromAttempt(attempt: AssessmentAttempt): AssessmentResult {
        val questions = getQuestions(attempt.assessmentType)
        val answerMap = attempt.answers.associate { it.questionId to it.selectedOptionId }
        return ScoringEngine.calculateScores(
            answers = answerMap,
            questions = questions,
            assessmentType = attempt.assessmentType
        )
    }

    private fun readAttempt(key: String): AssessmentAttempt? {
        val raw = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(raw, AssessmentAttempt::class.java)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "um_climate_assessment_prefs"
        private const val KEY_LATEST_PRE = "latest_pre_attempt_json"
        private const val KEY_LATEST_POST = "latest_post_attempt_json"
    }
}
