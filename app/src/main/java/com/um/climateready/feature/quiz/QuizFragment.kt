
package com.um.climateready.feature.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScenarioQuizViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartScenarioQuiz.setOnClickListener {
            startQuiz()
        }

        binding.btnSubmitAnswer.setOnClickListener {
            submitAnswer()
        }

        binding.btnNextQuestion.setOnClickListener {
            if (viewModel.isFinished()) {
                finishQuiz()
            } else if (viewModel.nextQuestion()) {
                displayQuestion()
            }
        }
    }

    private fun startQuiz() {
        val dataManager =
            AssetDataManager.getInstance(requireContext())

        val scenarios = dataManager.scenarioQuestions

        if (scenarios.size < 3) {
            Toast.makeText(
                requireContext(),
                "Not enough scenario questions.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.resetQuiz()

        val selectedQuestions = scenarios.shuffled().take(3)

        selectedQuestions.forEach { source ->
            viewModel.addQuestion(
                ScenarioQuestion(
                    question = source.scenario,
                    options = source.options.associate {
                        it.id to it.text
                    },
                    correctAnswer = source.correctOptionId,
                    explanation = source.explanation
                )
            )
        }

        binding.introCard.visibility = View.GONE
        binding.quizContainer.visibility = View.VISIBLE

        displayQuestion()
    }

    private fun displayQuestion() {
        val question =
            viewModel.getCurrentQuestion() ?: return

        binding.tvProgress.text =
            "Question ${viewModel.currentQuestionIndex + 1} of 3"

        binding.tvQuestion.text = question.question

        binding.rbA.text = "A. ${question.options["A"].orEmpty()}"
        binding.rbB.text = "B. ${question.options["B"].orEmpty()}"
        binding.rbC.text = "C. ${question.options["C"].orEmpty()}"
        binding.rbD.text = "D. ${question.options["D"].orEmpty()}"

        binding.rgAnswers.clearCheck()

        listOf(
            binding.rbA,
            binding.rbB,
            binding.rbC,
            binding.rbD
        ).forEach {
            it.isEnabled = true
        }

        binding.btnSubmitAnswer.visibility = View.VISIBLE
        binding.tvExplanation.visibility = View.GONE
        binding.btnNextQuestion.visibility = View.GONE
    }

    private fun submitAnswer() {
        val selectedId = binding.rgAnswers.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(
                requireContext(),
                "Please select an answer.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val answer = when (selectedId) {
            binding.rbA.id -> "A"
            binding.rbB.id -> "B"
            binding.rbC.id -> "C"
            binding.rbD.id -> "D"
            else -> return
        }

        val isCorrect = viewModel.selectAnswer(answer)

        val result = if (isCorrect) {
            "Correct!"
        } else {
            "Incorrect."
        }

        binding.tvExplanation.text =
            "$result\n\n${viewModel.getExplanation().orEmpty()}"

        binding.tvExplanation.visibility = View.VISIBLE
        binding.btnSubmitAnswer.visibility = View.GONE
        binding.btnNextQuestion.visibility = View.VISIBLE

        listOf(
            binding.rbA,
            binding.rbB,
            binding.rbC,
            binding.rbD
        ).forEach {
            it.isEnabled = false
        }

        binding.btnNextQuestion.text =
            if (viewModel.isFinished()) {
                "View Results"
            } else {
                "Next Question"
            }
    }

    private fun finishQuiz() {
        binding.tvProgress.text = "Quiz Completed!"
        binding.tvQuestion.text =
            "Your Score: ${viewModel.score} / 3"

        binding.rgAnswers.visibility = View.GONE
        binding.tvExplanation.visibility = View.GONE
        binding.btnSubmitAnswer.visibility = View.GONE
        binding.btnNextQuestion.visibility = View.GONE

        Toast.makeText(
            requireContext(),
            "Quiz completed!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
