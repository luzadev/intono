package com.notemusicali.ui.eartraining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.audio.ToneGenerator
import com.notemusicali.audio.createToneGenerator
import com.notemusicali.eartraining.EarTrainingDifficulty
import com.notemusicali.eartraining.EarTrainingEngine
import com.notemusicali.eartraining.EarTrainingMode
import com.notemusicali.eartraining.EarTrainingQuestion
import com.notemusicali.eartraining.EarTrainingSession
import com.notemusicali.eartraining.EarTrainingStatsRepository
import com.notemusicali.goals.DailyGoalManager
import com.notemusicali.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EarTrainingUiState(
    val mode: EarTrainingMode = EarTrainingMode.NOTE,
    val difficulty: EarTrainingDifficulty = EarTrainingDifficulty.EASY,
    val currentQuestion: EarTrainingQuestion? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 10,
    val correctCount: Int = 0,
    val selectedAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val isSessionComplete: Boolean = false,
    val isPlaying: Boolean = false,
)

class EarTrainingViewModel : ViewModel() {
    private val toneGenerator: ToneGenerator = createToneGenerator()

    private val _uiState = MutableStateFlow(EarTrainingUiState())
    val uiState: StateFlow<EarTrainingUiState> = _uiState.asStateFlow()

    private val sessionStartTime = currentTimeMillis()

    fun setMode(mode: EarTrainingMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun setDifficulty(difficulty: EarTrainingDifficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    fun startSession() {
        _uiState.value = _uiState.value.copy(
            questionNumber = 0,
            correctCount = 0,
            isSessionComplete = false,
            selectedAnswer = null,
            isCorrect = null,
        )
        nextQuestion()
    }

    private fun nextQuestion() {
        val state = _uiState.value
        if (state.questionNumber >= state.totalQuestions) {
            completeSession()
            return
        }

        val question = when (state.mode) {
            EarTrainingMode.NOTE -> EarTrainingEngine.generateNoteQuestion(state.difficulty)
            EarTrainingMode.INTERVAL -> EarTrainingEngine.generateIntervalQuestion(state.difficulty)
        }

        _uiState.value = state.copy(
            currentQuestion = question,
            questionNumber = state.questionNumber + 1,
            selectedAnswer = null,
            isCorrect = null,
        )
    }

    fun playSound() {
        val question = _uiState.value.currentQuestion ?: return
        _uiState.value = _uiState.value.copy(isPlaying = true)

        viewModelScope.launch {
            when (question.mode) {
                EarTrainingMode.NOTE -> {
                    toneGenerator.playTone(question.targetFrequency, 1500L)
                }
                EarTrainingMode.INTERVAL -> {
                    toneGenerator.playTone(question.referenceFrequency, 800L)
                    delay(1000)
                    toneGenerator.playTone(question.targetFrequency, 800L)
                }
            }
            delay(1000)
            _uiState.value = _uiState.value.copy(isPlaying = false)
        }
    }

    fun submitAnswer(answer: String) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.selectedAnswer != null) return // already answered

        val correct = answer == question.correctAnswer
        _uiState.value = state.copy(
            selectedAnswer = answer,
            isCorrect = correct,
            correctCount = if (correct) state.correctCount + 1 else state.correctCount,
        )

        // Auto-advance after delay
        viewModelScope.launch {
            delay(1500)
            nextQuestion()
        }
    }

    private fun completeSession() {
        val state = _uiState.value
        _uiState.value = state.copy(isSessionComplete = true)

        // Save stats
        val session = EarTrainingSession(
            id = sessionStartTime.toString(),
            dateMillis = sessionStartTime,
            mode = state.mode.name,
            difficulty = state.difficulty.name,
            totalQuestions = state.totalQuestions,
            correctAnswers = state.correctCount,
        )
        EarTrainingStatsRepository.save(session)
        DailyGoalManager.recordPractice(currentTimeMillis() - sessionStartTime)
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator.release()
    }
}
