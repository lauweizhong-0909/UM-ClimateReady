package com.um.climateready.core.database

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.model.ClimateResource
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.model.ScenarioQuestion
import java.io.InputStreamReader

class AssetDataManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()
    private val prefs: SharedPreferences = appContext.getSharedPreferences("um_climate_prefs", Context.MODE_PRIVATE)

    val assessmentQuestions: List<AssessmentQuestion> by lazy {
        loadJsonFromAssets("data/m1_assessment_questions.json", object : TypeToken<List<AssessmentQuestion>>() {}.type)
    }

    val learningUnits: List<LearningUnit> by lazy {
        val list: List<LearningUnit> = loadJsonFromAssets("data/m2_microlearning_units.json", object : TypeToken<List<LearningUnit>>() {}.type)
        val completedSet = getCompletedUnitIds()
        list.forEach { it.isCompleted = completedSet.contains(it.id) }
        list
    }

    val scenarioQuestions: List<ScenarioQuestion> by lazy {
        loadJsonFromAssets("data/m3_scenario_questions.json", object : TypeToken<List<ScenarioQuestion>>() {}.type)
    }

    val climateResources: List<ClimateResource> by lazy {
        loadJsonFromAssets("data/m5_climate_resources.json", object : TypeToken<List<ClimateResource>>() {}.type)
    }

    private fun <T> loadJsonFromAssets(fileName: String, type: java.lang.reflect.Type): List<T> {
        return try {
            appContext.assets.open(fileName).use { stream ->
                InputStreamReader(stream).use { reader ->
                    gson.fromJson<List<T>>(reader, type) ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getCompletedUnitIds(): Set<String> {
        return prefs.getStringSet("completed_unit_ids", emptySet()) ?: emptySet()
    }

    fun setUnitCompleted(unitId: String, completed: Boolean) {
        val currentSet = getCompletedUnitIds().toMutableSet()
        if (completed) {
            currentSet.add(unitId)
        } else {
            currentSet.remove(unitId)
        }
        prefs.edit().putStringSet("completed_unit_ids", currentSet).apply()
        learningUnits.find { it.id == unitId }?.isCompleted = completed
    }

    fun getCompletedUnitsCount(): Int {
        return getCompletedUnitIds().size
    }

    fun savePreAssessmentScore(totalScore: Int, rainScore: Int, heatScore: Int, hazeScore: Int) {
        prefs.edit()
            .putInt("pre_score_total", totalScore)
            .putInt("pre_score_rain", rainScore)
            .putInt("pre_score_heat", heatScore)
            .putInt("pre_score_haze", hazeScore)
            .putBoolean("has_completed_pre_assessment", true)
            .apply()
    }

    fun hasCompletedPreAssessment(): Boolean {
        return prefs.getBoolean("has_completed_pre_assessment", false)
    }

    fun getPreAssessmentTotalScore(): Int {
        return prefs.getInt("pre_score_total", 0)
    }

    companion object {
        @Volatile
        private var instance: AssetDataManager? = null

        fun getInstance(context: Context): AssetDataManager {
            return instance ?: synchronized(this) {
                instance ?: AssetDataManager(context).also { instance = it }
            }
        }
    }
}

