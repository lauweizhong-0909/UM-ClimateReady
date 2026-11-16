package com.um.climateready

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.model.ClimateResource
import com.um.climateready.core.model.LearningUnit
import com.um.climateready.core.model.ScenarioQuestion
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AssetSeedDataTest {

    private val gson = Gson()
    private val assetsDir = File("src/main/assets/data")

    @Test
    fun testModule1QuestionsAsset() {
        val file = File(assetsDir, "m1_assessment_questions.json")
        assertTrue("M1 JSON file must exist", file.exists())

        val type = object : TypeToken<List<AssessmentQuestion>>() {}.type
        val questions: List<AssessmentQuestion> = gson.fromJson(file.readText(), type)

        assertEquals("Must have exactly 18 assessment questions", 18, questions.size)
        val preList = questions.filter { it.type == "PRE" }
        val postList = questions.filter { it.type == "POST" }
        assertEquals(9, preList.size)
        assertEquals(9, postList.size)

        questions.forEach { q ->
            assertNotNull(q.id)
            assertNotNull(q.question)
            assertEquals("Must have 4 options", 4, q.options.size)
            assertTrue("Correct option must be A, B, C, or D", q.correctOptionId in listOf("A", "B", "C", "D"))
            assertNotNull(q.explanation)
        }
    }

    @Test
    fun testModule2LearningUnitsAsset() {
        val file = File(assetsDir, "m2_microlearning_units.json")
        assertTrue("M2 JSON file must exist", file.exists())

        val type = object : TypeToken<List<LearningUnit>>() {}.type
        val units: List<LearningUnit> = gson.fromJson(file.readText(), type)

        assertEquals("Must have exactly 12 learning units", 12, units.size)
        val rainUnits = units.filter { it.category == "HEAVY_RAIN" }
        val heatUnits = units.filter { it.category == "EXTREME_HEAT" }
        val hazeUnits = units.filter { it.category == "HAZE_AIR_QUALITY" }

        assertEquals(4, rainUnits.size)
        assertEquals(4, heatUnits.size)
        assertEquals(4, hazeUnits.size)

        units.forEach { u ->
            assertNotNull(u.id)
            assertNotNull(u.title)
            assertNotNull(u.summary)
            assertNotNull(u.realWorldExample)
            assertNotNull(u.umStudentTip)
            assertNotNull(u.knowledgeCheck)
            assertEquals(4, u.knowledgeCheck?.options?.size)
        }
    }

    @Test
    fun testModule3ScenarioQuestionsAsset() {
        val file = File(assetsDir, "m3_scenario_questions.json")
        assertTrue("M3 JSON file must exist", file.exists())

        val type = object : TypeToken<List<ScenarioQuestion>>() {}.type
        val scenarios: List<ScenarioQuestion> = gson.fromJson(file.readText(), type)

        assertEquals("Must have exactly 60 scenario questions", 60, scenarios.size)
        val campusSpecific = scenarios.filter { it.isCampusSpecific }
        assertEquals("Must have 15 campus-specific scenario questions", 15, campusSpecific.size)

        scenarios.forEach { s ->
            assertNotNull(s.id)
            assertNotNull(s.scenario)
            assertEquals(4, s.options.size)
            assertTrue(s.correctOptionId in listOf("A", "B", "C", "D"))
            assertNotNull(s.explanation)
        }
    }

    @Test
    fun testModule5ClimateResourcesAsset() {
        val file = File(assetsDir, "m5_climate_resources.json")
        assertTrue("M5 JSON file must exist", file.exists())

        val type = object : TypeToken<List<ClimateResource>>() {}.type
        val resources: List<ClimateResource> = gson.fromJson(file.readText(), type)

        assertEquals("Must have exactly 12 verified resources", 12, resources.size)
        resources.forEach { r ->
            assertNotNull(r.id)
            assertNotNull(r.name)
            assertNotNull(r.sourceOrganization)
            assertNotNull(r.officialUrl)
            assertEquals("VERIFIED", r.verificationStatus)
        }
    }
}

