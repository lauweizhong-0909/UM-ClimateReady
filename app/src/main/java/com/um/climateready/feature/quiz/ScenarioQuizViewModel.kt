
package com.um.climateready.feature.quiz

import androidx.lifecycle.ViewModel

data class QuizUiQuestion(
    val question: String,
    val options: Map<String, String>,
    val correctAnswer: String,
    val explanation: String
)

class ScenarioQuizViewModel : ViewModel() {

    private val questions = mutableListOf<QuizUiQuestion>()

    var currentQuestionIndex = 0
        private set

    var selectedAnswer: String? = null
        private set

    var score = 0
        private set

    val totalQuestions = 3

    fun addQuestion(question: QuizUiQuestion): Boolean {
        if (questions.size >= totalQuestions) return false
        questions.add(question)
        return true
    }

    fun getCurrentQuestion(): QuizUiQuestion? {
        return questions.getOrNull(currentQuestionIndex)
    }

    fun selectAnswer(answer: String): Boolean {
        val question = getCurrentQuestion() ?: return false

        if (selectedAnswer != null) return false
        if (answer !in listOf("A", "B", "C", "D")) return false

        selectedAnswer = answer

        val isCorrect = answer == question.correctAnswer

        if (isCorrect) {
            score++
        }

        return isCorrect
    }

    fun getExplanation(): String? {
        if (selectedAnswer == null) return null
        return getCurrentQuestion()?.explanation
    }

    fun nextQuestion(): Boolean {
        if (selectedAnswer == null) return false
        if (currentQuestionIndex + 1 >= questions.size) return false

        currentQuestionIndex++
        selectedAnswer = null
        return true
    }

    fun isFinished(): Boolean {
        return selectedAnswer != null &&
                currentQuestionIndex == totalQuestions - 1 &&
                questions.size == totalQuestions
    }

    fun resetQuiz() {
        questions.clear()
        currentQuestionIndex = 0
        selectedAnswer = null
        score = 0
    }
}
