package com.um.climateready.feature.assessment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.um.climateready.core.model.AssessmentQuestion
import com.um.climateready.core.util.AssessmentAttempt
import com.um.climateready.core.util.AssessmentComparison
import com.um.climateready.core.util.AssessmentResult
import com.um.climateready.core.util.PostTestUnlockStatus
import com.um.climateready.core.util.RecommendationResult
import com.um.climateready.core.util.ScoringEngine
import com.um.climateready.data.repository.AssessmentRepository

enum class AssessmentScreen {
    HUB,
    QUIZ,
    RESULT,
    COMPARISON
}

class AssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AssessmentRepository(application)

    private val _currentScreen = MutableLiveData(AssessmentScreen.HUB)
    val currentScreen: LiveData<AssessmentScreen> = _currentScreen

    private val _activeType = MutableLiveData(ScoringEngine.TYPE_PRE)
    val activeType: LiveData<String> = _activeType

    private val _questions = MutableLiveData<List<AssessmentQuestion>>(emptyList())
    val questions: LiveData<List<AssessmentQuestion>> = _questions

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _answers = MutableLiveData<Map<String, String>>(emptyMap())
    val answers: LiveData<Map<String, String>> = _answers

    private val _preAttempt = MutableLiveData<AssessmentAttempt?>()
    val preAttempt: LiveData<AssessmentAttempt?> = _preAttempt

    private val _postAttempt = MutableLiveData<AssessmentAttempt?>()
    val postAttempt: LiveData<AssessmentAttempt?> = _postAttempt

    private val _unlockStatus = MutableLiveData<PostTestUnlockStatus>()
    val unlockStatus: LiveData<PostTestUnlockStatus> = _unlockStatus

    private val _activeAttempt = MutableLiveData<AssessmentAttempt?>()
    val activeAttempt: LiveData<AssessmentAttempt?> = _activeAttempt

    private val _activeResult = MutableLiveData<AssessmentResult?>()
    val activeResult: LiveData<AssessmentResult?> = _activeResult

    private val _activeRecommendation = MutableLiveData<RecommendationResult?>()
    val activeRecommendation: LiveData<RecommendationResult?> = _activeRecommendation

    private val _comparison = MutableLiveData<AssessmentComparison?>()
    val comparison: LiveData<AssessmentComparison?> = _comparison

    private var quizStartedAt: Long = 0L

    init {
        refreshHubState()
    }

    fun refreshHubState() {
        val pre = repository.getLatestPreAttempt()
        val post = repository.getLatestPostAttempt()
        val unlock = repository.getPostTestUnlockStatus()

        _preAttempt.value = pre
        _postAttempt.value = post
        _unlockStatus.value = unlock
        _comparison.value = if (pre != null && post != null) {
            ScoringEngine.comparePreAndPost(pre, post)
        } else {
            null
        }
    }

    fun startAssessment(type: String): Boolean {
        if (type == ScoringEngine.TYPE_POST) {
            val unlock = repository.getPostTestUnlockStatus()
            _unlockStatus.value = unlock
            if (!unlock.isUnlocked) return false
        }

        val loadedQuestions = repository.getQuestions(type)
        if (loadedQuestions.isEmpty()) return false

        quizStartedAt = System.currentTimeMillis()
        _activeType.value = type
        _questions.value = loadedQuestions
        _currentIndex.value = 0
        _answers.value = emptyMap()
        _currentScreen.value = AssessmentScreen.QUIZ
        return true
    }

    fun selectOption(questionId: String, optionId: String) {
        val updated = (_answers.value ?: emptyMap()).toMutableMap()
        updated[questionId] = optionId
        _answers.value = updated
    }

    fun getSelectedOptionForCurrentQuestion(): String? {
        val list = _questions.value ?: return null
        val index = _currentIndex.value ?: return null
        val question = list.getOrNull(index) ?: return null
        return _answers.value?.get(question.id)
    }

    fun goToNextQuestion(): Boolean {
        val list = _questions.value ?: return false
        val index = _currentIndex.value ?: 0
        if (index < list.lastIndex) {
            _currentIndex.value = index + 1
            return true
        }
        return false
    }

    fun goToPreviousQuestion(): Boolean {
        val index = _currentIndex.value ?: 0
        if (index > 0) {
            _currentIndex.value = index - 1
            return true
        }
        return false
    }

    fun isLastQuestion(): Boolean {
        val list = _questions.value ?: return true
        val index = _currentIndex.value ?: 0
        return index >= list.lastIndex
    }

    fun submitAssessment(): AssessmentResult? {
        val list = _questions.value.orEmpty()
        if (list.isEmpty()) return null

        val type = _activeType.value ?: ScoringEngine.TYPE_PRE
        val answerMap = _answers.value ?: emptyMap()
        val result = ScoringEngine.calculateScores(
            answers = answerMap,
            questions = list,
            assessmentType = type
        )

        val now = System.currentTimeMillis()
        val started = if (quizStartedAt > 0L) quizStartedAt else now
        val savedAttempt = repository.saveAttempt(
            assessmentType = type,
            startedAt = started,
            completedAt = now,
            result = result
        )

        val recommendation = ScoringEngine.recommendLearningUnit(
            result = result,
            learningUnits = repository.getLearningUnits(),
            completedUnitIds = repository.getCompletedUnitIds()
        )

        refreshHubState()
        _activeAttempt.value = savedAttempt
        _activeResult.value = result
        _activeRecommendation.value = recommendation
        _currentScreen.value = AssessmentScreen.RESULT
        return result
    }

    fun openSavedResult(type: String): Boolean {
        val attempt = if (type == ScoringEngine.TYPE_POST) {
            repository.getLatestPostAttempt()
        } else {
            repository.getLatestPreAttempt()
        } ?: return false

        val result = repository.rebuildResultFromAttempt(attempt)
        val recommendation = ScoringEngine.recommendLearningUnit(
            result = result,
            learningUnits = repository.getLearningUnits(),
            completedUnitIds = repository.getCompletedUnitIds()
        )

        refreshHubState()
        _activeType.value = type
        _activeAttempt.value = attempt
        _activeResult.value = result
        _activeRecommendation.value = recommendation
        _currentScreen.value = AssessmentScreen.RESULT
        return true
    }

    fun openComparison(): Boolean {
        refreshHubState()
        if (_comparison.value == null) return false
        _currentScreen.value = AssessmentScreen.COMPARISON
        return true
    }

    fun returnToHub() {
        refreshHubState()
        _currentScreen.value = AssessmentScreen.HUB
    }

    fun buildShareTextForActiveResult(): String? {
        val attempt = _activeAttempt.value ?: return null
        return ScoringEngine.buildShareableSummary(preAttempt = attempt, postAttempt = null)
    }

    fun buildShareTextForComparison(): String? {
        val pre = _preAttempt.value ?: return null
        val post = _postAttempt.value ?: return null
        return ScoringEngine.buildShareableSummary(preAttempt = pre, postAttempt = post)
    }
}
