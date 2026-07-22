package com.notemusicali.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.audio.AudioProcessor
import com.notemusicali.challenge.ChallengeRepository
import com.notemusicali.challenge.ChallengeResult
import com.notemusicali.challenge.ChallengeScoring
import com.notemusicali.challenge.ChallengeTimeLimit
import com.notemusicali.goals.DailyGoalManager
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteSequence
import com.notemusicali.util.currentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ChallengeUiState(
    val sequence: NoteSequence = NoteSequence.empty(),
    val timeLimit: ChallengeTimeLimit = ChallengeTimeLimit.MEDIUM,
    val currentIndex: Int = 0,
    val targetNote: MusicalNote? = null,
    val detectedNoteName: String = "",
    val detectedCents: Float = 0f,
    val isCorrect: Boolean = false,
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val remainingMs: Long = 60_000L,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val notesPlayed: Int = 0,
    val lastResult: ChallengeResult? = null,
)

class ChallengeViewModel : ViewModel() {
    private val audioProcessor = AudioProcessor()

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var noteStartTime: Long = 0L
    private var firstAttemptForNote: Boolean = true
    private var firstAttemptNotes: Int = 0

    fun setSequence(sequence: NoteSequence) {
        _uiState.value = ChallengeUiState(
            sequence = sequence,
            targetNote = sequence.noteAt(0),
        )
    }

    fun setTimeLimit(limit: ChallengeTimeLimit) {
        _uiState.value = _uiState.value.copy(
            timeLimit = limit,
            remainingMs = limit.seconds * 1000L,
        )
    }

    fun start() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isRunning = true,
            remainingMs = state.timeLimit.seconds * 1000L,
            currentIndex = 0,
            targetNote = state.sequence.noteAt(0),
            score = 0,
            combo = 0,
            maxCombo = 0,
            notesPlayed = 0,
            isComplete = false,
        )

        noteStartTime = currentTimeMillis()
        firstAttemptForNote = true
        firstAttemptNotes = 0

        // Start audio
        audioProcessor.start(viewModelScope)

        // Start timer
        timerJob = viewModelScope.launch {
            val startTime = currentTimeMillis()
            val totalMs = state.timeLimit.seconds * 1000L
            while (isActive) {
                val elapsed = currentTimeMillis() - startTime
                val remaining = (totalMs - elapsed).coerceAtLeast(0L)
                _uiState.value = _uiState.value.copy(remainingMs = remaining)
                if (remaining <= 0) {
                    endChallenge()
                    break
                }
                delay(100)
            }
        }

        // Listen for notes
        viewModelScope.launch {
            audioProcessor.result.collect { result ->
                val s = _uiState.value
                if (!s.isRunning || s.isComplete) return@collect

                when (result) {
                    is AudioProcessor.ProcessedResult.Detected -> {
                        val detected = result.noteResult
                        val target = s.targetNote ?: return@collect

                        val isCorrect = detected.note.noteIndex == target.noteIndex &&
                            detected.note.octave == target.octave &&
                            abs(detected.centsDeviation) <= 30f

                        _uiState.value = s.copy(
                            detectedNoteName = detected.note.displayName,
                            detectedCents = detected.centsDeviation,
                            isCorrect = isCorrect,
                        )

                        if (isCorrect) {
                            val now = currentTimeMillis()
                            val reactionMs = now - noteStartTime
                            val combo = s.combo + 1
                            val points = ChallengeScoring.scoreNote(
                                centsDeviation = detected.centsDeviation,
                                reactionTimeMs = reactionMs,
                                comboStreak = s.combo,
                                firstAttempt = firstAttemptForNote,
                            )

                            if (firstAttemptForNote) firstAttemptNotes++

                            val nextIndex = s.currentIndex + 1
                            val maxCombo = maxOf(s.maxCombo, combo)

                            if (nextIndex >= s.sequence.size) {
                                // Loop back to start
                                noteStartTime = now
                                firstAttemptForNote = true
                                _uiState.value = s.copy(
                                    currentIndex = 0,
                                    targetNote = s.sequence.noteAt(0),
                                    score = s.score + points,
                                    combo = combo,
                                    maxCombo = maxCombo,
                                    notesPlayed = s.notesPlayed + 1,
                                    isCorrect = false,
                                    detectedNoteName = "",
                                )
                            } else {
                                noteStartTime = now
                                firstAttemptForNote = true
                                _uiState.value = s.copy(
                                    currentIndex = nextIndex,
                                    targetNote = s.sequence.noteAt(nextIndex),
                                    score = s.score + points,
                                    combo = combo,
                                    maxCombo = maxCombo,
                                    notesPlayed = s.notesPlayed + 1,
                                    isCorrect = false,
                                    detectedNoteName = "",
                                )
                            }
                        } else {
                            firstAttemptForNote = false
                            if (s.combo > 0) {
                                _uiState.value = s.copy(
                                    combo = 0,
                                    detectedNoteName = detected.note.displayName,
                                    detectedCents = detected.centsDeviation,
                                )
                            }
                        }
                    }
                    else -> {
                        _uiState.value = s.copy(
                            detectedNoteName = "",
                            isCorrect = false,
                        )
                    }
                }
            }
        }
    }

    private fun endChallenge() {
        timerJob?.cancel()
        audioProcessor.stop()

        val state = _uiState.value
        val result = ChallengeResult(
            id = currentTimeMillis().toString(),
            dateMillis = currentTimeMillis(),
            sequenceName = state.sequence.name,
            timeLimitSec = state.timeLimit.seconds,
            totalScore = state.score,
            notesPlayed = state.notesPlayed,
            maxCombo = state.maxCombo,
            accuracy = ChallengeScoring.accuracy(state.notesPlayed, firstAttemptNotes),
        )
        ChallengeRepository.save(result)
        DailyGoalManager.recordPractice(state.timeLimit.seconds * 1000L)

        _uiState.value = state.copy(
            isRunning = false,
            isComplete = true,
            lastResult = result,
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioProcessor.stop()
    }
}
