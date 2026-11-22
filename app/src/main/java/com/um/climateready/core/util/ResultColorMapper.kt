package com.um.climateready.core.util

import androidx.annotation.ColorRes
import com.um.climateready.R

enum class ScoreBand(
    val label: String,
    val guidance: String,
    @param:ColorRes val textColorRes: Int,
    @param:ColorRes val containerColorRes: Int
) {
    RED(
        label = "Needs Urgent Review",
        guidance = "Further learning recommended.",
        textColorRes = R.color.score_red,
        containerColorRes = R.color.score_red_container
    ),
    YELLOW(
        label = "Basic Understanding",
        guidance = "Basic understanding demonstrated.",
        textColorRes = R.color.score_yellow,
        containerColorRes = R.color.score_yellow_container
    ),
    GREEN(
        label = "Sound Awareness",
        guidance = "All three questions in this category were answered correctly.",
        textColorRes = R.color.score_green,
        containerColorRes = R.color.score_green_container
    )
}

object ResultColorMapper {

    /**
     * Maps a category score (0-3) to its fixed deterministic band:
     * 0-1 -> RED ("Needs Urgent Review")
     * 2   -> YELLOW ("Basic Understanding")
     * 3   -> GREEN ("Sound Awareness")
     */
    fun mapCategoryScore(score: Int): ScoreBand {
        return when {
            score <= 1 -> ScoreBand.RED
            score == 2 -> ScoreBand.YELLOW
            else -> ScoreBand.GREEN
        }
    }

    /**
     * Maps an overall assessment score (0-9) to a visual summary band:
     * 0-3 -> RED
     * 4-6 -> YELLOW
     * 7-9 -> GREEN
     */
    fun mapTotalScore(totalScore: Int): ScoreBand {
        return when {
            totalScore <= 3 -> ScoreBand.RED
            totalScore <= 6 -> ScoreBand.YELLOW
            else -> ScoreBand.GREEN
        }
    }

    fun categoryDisplayName(category: String): String {
        return when (category) {
            ScoringEngine.CATEGORY_HEAVY_RAIN -> "Heavy Rain & Flooding"
            ScoringEngine.CATEGORY_EXTREME_HEAT -> "Extreme Heat"
            ScoringEngine.CATEGORY_HAZE -> "Haze & Air Quality"
            else -> category.replace("_", " ")
        }
    }

    fun categoryShortName(category: String): String {
        return when (category) {
            ScoringEngine.CATEGORY_HEAVY_RAIN -> "Heavy Rain"
            ScoringEngine.CATEGORY_EXTREME_HEAT -> "Extreme Heat"
            ScoringEngine.CATEGORY_HAZE -> "Haze & AQI"
            else -> category.replace("_", " ")
        }
    }
}
