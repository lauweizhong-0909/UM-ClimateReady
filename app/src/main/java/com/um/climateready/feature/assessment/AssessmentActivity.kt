package com.um.climateready.feature.assessment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.um.climateready.core.database.AssetDataManager
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.databinding.ActivityAssessmentBinding

class AssessmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssessmentBinding
    private lateinit var dataManager: AssetDataManager
    private var preQuestions: List<AssessmentQuestion> = emptyList()
    private var currentIndex = 0
    private val userAnswers = mutableMapOf<Int, String>() // questionIndex -> selectedOptionId

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.assessmentRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val topPadding = maxOf(systemBars.top, displayCutout.top)
            binding.toolbarAssessment.setPadding(0, topPadding, 0, 0)
            binding.layoutBottomAction.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        binding.toolbarAssessment.setNavigationOnClickListener {
            finish()
        }

        dataManager = AssetDataManager.getInstance(this)
        preQuestions = dataManager.assessmentQuestions.filter { it.type == "PRE" }

        if (preQuestions.isEmpty()) {
            Toast.makeText(this, "No pre-assessment questions found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayQuestion(currentIndex)

        binding.btnNextQuestion.setOnClickListener {
            val selectedOptionId = getSelectedOptionId()
            if (selectedOptionId == null) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userAnswers[currentIndex] = selectedOptionId

            if (currentIndex < preQuestions.size - 1) {
                currentIndex++
                displayQuestion(currentIndex)
            } else {
                finishAssessment()
            }
        }
    }

    private fun displayQuestion(index: Int) {
        val q = preQuestions[index]
        binding.tvQuestionProgress.text = "Question ${index + 1} of ${preQuestions.size}"
        binding.tvCategoryTag.text = q.category.replace("_", " ")
        binding.tvQuestionTitle.text = q.title
        binding.tvQuestionBody.text = q.question

        binding.rgOptions.clearCheck()

        if (q.options.size >= 4) {
            binding.rbOptA.text = "A) ${q.options[0].text}"
            binding.rbOptB.text = "B) ${q.options[1].text}"
            binding.rbOptC.text = "C) ${q.options[2].text}"
            binding.rbOptD.text = "D) ${q.options[3].text}"
        }

        val previousAnswer = userAnswers[index]
        when (previousAnswer) {
            "A" -> binding.rbOptA.isChecked = true
            "B" -> binding.rbOptB.isChecked = true
            "C" -> binding.rbOptC.isChecked = true
            "D" -> binding.rbOptD.isChecked = true
        }

        if (index == preQuestions.size - 1) {
            binding.btnNextQuestion.text = "Submit Assessment"
        } else {
            binding.btnNextQuestion.text = "Next Question"
        }
    }

    private fun getSelectedOptionId(): String? {
        return when (binding.rgOptions.checkedRadioButtonId) {
            binding.rbOptA.id -> "A"
            binding.rbOptB.id -> "B"
            binding.rbOptC.id -> "C"
            binding.rbOptD.id -> "D"
            else -> null
        }
    }

    private fun finishAssessment() {
        var totalScore = 0
        var rainScore = 0
        var heatScore = 0
        var hazeScore = 0

        for (i in preQuestions.indices) {
            val q = preQuestions[i]
            val answer = userAnswers[i]
            if (answer != null && answer.equals(q.correctOptionId, ignoreCase = true)) {
                totalScore++
                when (q.category) {
                    "HEAVY_RAIN" -> rainScore++
                    "EXTREME_HEAT" -> heatScore++
                    "HAZE_AIR_QUALITY" -> hazeScore++
                }
            }
        }

        dataManager.savePreAssessmentScore(totalScore, rainScore, heatScore, hazeScore)
        Toast.makeText(
            this,
            "Assessment Complete! Score: $totalScore / ${preQuestions.size}",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}

