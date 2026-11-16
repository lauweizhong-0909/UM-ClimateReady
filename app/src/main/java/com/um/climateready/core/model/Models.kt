package com.um.climateready.core.model

data class Option(
    val id: String,
    val text: String
)

data class KnowledgeCheck(
    val question: String,
    val options: List<Option>,
    val correctOptionId: String,
    val explanation: String
)

data class AssessmentQuestion(
    val id: String,
    val type: String, // "PRE" or "POST"
    val category: String, // "HEAVY_RAIN", "EXTREME_HEAT", "HAZE_AIR_QUALITY"
    val title: String,
    val question: String,
    val options: List<Option>,
    val correctOptionId: String,
    val explanation: String,
    val linkedLearningUnitId: String
)

data class LearningUnit(
    val id: String,
    val globalOrder: Int,
    val categoryOrder: Int,
    val category: String,
    val title: String,
    val summary: String,
    val realWorldExample: String,
    val umStudentTip: String,
    val knowledgeCheck: KnowledgeCheck?,
    val takeaway: String,
    var isCompleted: Boolean = false
)

data class ScenarioQuestion(
    val id: String,
    val category: String,
    val categoryOrder: Int,
    val isCampusSpecific: Boolean,
    val scenario: String,
    val options: List<Option>,
    val correctOptionId: String,
    val explanation: String,
    val relatedLearningUnitId: String
)

data class ClimateResource(
    val id: String,
    val name: String,
    val category: String,
    val shortDescription: String,
    val sourceOrganization: String,
    val officialUrl: String,
    val phoneNumber: String,
    val lastVerifiedAt: String,
    val verificationStatus: String
)
