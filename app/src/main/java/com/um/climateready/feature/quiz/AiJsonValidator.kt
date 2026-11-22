
package com.um.climateready.feature.quiz

import org.json.JSONObject

object AiJsonValidator {

    fun validate(json: String): Boolean {
        return try {
            val question = JSONObject(json)

            val questionText = question.getString("question")
            val options = question.getJSONObject("options")
            val correctAnswer = question.getString("correctAnswer")
            val explanation = question.getString("explanation")

            if (questionText.isBlank() || explanation.isBlank()) {
                return false
            }

            val choices = listOf("A", "B", "C", "D")

            if (!choices.all { options.has(it) }) {
                return false
            }

            if (choices.any {
                    options.getString(it).isBlank()
                }) {
                return false
            }

            correctAnswer in choices

        } catch (e: Exception) {
            false
        }
    }
}
