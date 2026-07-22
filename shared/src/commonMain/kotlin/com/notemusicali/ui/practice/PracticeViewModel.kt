package com.notemusicali.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.audio.AudioProcessor
import com.notemusicali.audio.InstrumentPreset
import com.notemusicali.goals.DailyGoalManager
import com.notemusicali.history.NoteResult
import com.notemusicali.history.PracticeSession
import com.notemusicali.history.SessionRepository
import com.notemusicali.music.MusicalNote
import com.notemusicali.music.NoteSequence
import com.notemusicali.util.currentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class PracticeViewModel : ViewModel() {

    private val audioProcessor = AudioProcessor()

    enum class NoteMatchState {
        WAITING,
        CORRECT,
        INCORRECT,
    }

    data class PracticeUiState(
        val sequence: NoteSequence = NoteSequence.empty(),
        val currentIndex: Int = 0,
        val targetNote: MusicalNote? = null,
        val detectedNoteName: String = "",
        val detectedCents: Float = 0f,
        val matchState: NoteMatchState = NoteMatchState.WAITING,
        val isCompleted: Boolean = false,
        val progress: Float = 0f,
        val lastSession: PracticeSession? = null,
    )

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var correctHoldJob: Job? = null
    private var isListening = false

    // Per-note tracking
    private var sessionStartTime: Long = 0L
    private var noteStartTime: Long = 0L
    private var firstAttemptForNote: Boolean = true
    private var lastCentsForNote: Float = 0f
    private val collectedResults = mutableListOf<NoteResult>()
    private var currentInstrument: InstrumentPreset = InstrumentPreset.VIOLINO

    fun selectInstrument(preset: InstrumentPreset) {
        val wasListening = isListening
        if (wasListening) stopListening()
        currentInstrument = preset
        audioProcessor.updatePreset(preset)
        if (wasListening) startListening()
    }

    fun setSequence(sequence: NoteSequence) {
        stopListening()
        collectedResults.clear()
        sessionStartTime = currentTimeMillis()
        noteStartTime = sessionStartTime
        firstAttemptForNote = true
        lastCentsForNote = 0f
        _uiState.value = PracticeUiState(
            sequence = sequence,
            targetNote = sequence.noteAt(0),
            progress = if (sequence.size > 0) 0f else 1f,
        )
    }

    fun startListening() {
        if (isListening) return
        isListening = true
        audioProcessor.start(viewModelScope)

        viewModelScope.launch {
            audioProcessor.result.collect { result ->
                val state = _uiState.value
                if (state.isCompleted) return@collect

                when (result) {
                    is AudioProcessor.ProcessedResult.Detected -> {
                        val detected = result.noteResult
                        val target = state.targetNote ?: return@collect

                        val isCorrect = detected.note.noteIndex == target.noteIndex &&
                            detected.note.octave == target.octave &&
                            abs(detected.centsDeviation) <= 30f

                        _uiState.value = state.copy(
                            detectedNoteName = detected.note.displayName,
                            detectedCents = detected.centsDeviation,
                            matchState = if (isCorrect) NoteMatchState.CORRECT else NoteMatchState.INCORRECT,
                        )

                        if (isCorrect && correctHoldJob == null) {
                            lastCentsForNote = detected.centsDeviation
                            val holdTime = target.duration.holdMs
                            correctHoldJob = viewModelScope.launch {
                                delay(holdTime)
                                advanceToNext()
                            }
                        } else if (!isCorrect) {
                            correctHoldJob?.cancel()
                            correctHoldJob = null
                            firstAttemptForNote = false
                        }
                    }
                    is AudioProcessor.ProcessedResult.Silence,
                    is AudioProcessor.ProcessedResult.NoNote -> {
                        correctHoldJob?.cancel()
                        correctHoldJob = null
                        _uiState.value = state.copy(
                            detectedNoteName = "",
                            detectedCents = 0f,
                            matchState = NoteMatchState.WAITING,
                        )
                    }
                }
            }
        }
    }

    private fun advanceToNext() {
        correctHoldJob = null
        val state = _uiState.value
        val now = currentTimeMillis()

        // Collect result for the completed note
        state.targetNote?.let { target ->
            collectedResults.add(
                NoteResult(
                    noteName = target.fullName,
                    midiNumber = target.midiNumber,
                    centsDeviation = lastCentsForNote,
                    reactionTimeMs = now - noteStartTime,
                    firstAttemptSuccess = firstAttemptForNote,
                )
            )
        }

        val nextIndex = state.currentIndex + 1

        if (nextIndex >= state.sequence.size) {
            val session = buildSession(now, completed = true)
            _uiState.value = state.copy(
                isCompleted = true,
                progress = 1f,
                matchState = NoteMatchState.WAITING,
                lastSession = session,
            )
        } else {
            noteStartTime = now
            firstAttemptForNote = true
            lastCentsForNote = 0f
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                targetNote = state.sequence.noteAt(nextIndex),
                matchState = NoteMatchState.WAITING,
                detectedNoteName = "",
                progress = nextIndex.toFloat() / state.sequence.size,
            )
        }
    }

    private fun buildSession(endTime: Long, completed: Boolean): PracticeSession {
        val state = _uiState.value
        return PracticeSession(
            id = sessionStartTime.toString(),
            sequenceName = state.sequence.name,
            instrumentName = currentInstrument.displayName,
            dateMillis = sessionStartTime,
            totalTimeMs = endTime - sessionStartTime,
            noteResults = collectedResults.toList(),
            noteMidiNumbers = state.sequence.notes.map { it.midiNumber },
            completed = completed,
            beats = state.sequence.beats,
            beatType = state.sequence.beatType,
        )
    }

    fun saveSession() {
        _uiState.value.lastSession?.let { session ->
            SessionRepository.save(session)
            DailyGoalManager.recordPractice(session.totalTimeMs)
        }
    }

    fun savePartialSession() {
        val state = _uiState.value
        if (state.isCompleted || sessionStartTime == 0L || state.sequence.size == 0) return
        if (collectedResults.isEmpty()) return
        val session = buildSession(currentTimeMillis(), completed = false)
        SessionRepository.save(session)
        DailyGoalManager.recordPractice(session.totalTimeMs)
    }

    fun stopListening() {
        isListening = false
        audioProcessor.stop()
        correctHoldJob?.cancel()
        correctHoldJob = null
    }

    fun reset() {
        stopListening()
        val seq = _uiState.value.sequence
        collectedResults.clear()
        sessionStartTime = currentTimeMillis()
        noteStartTime = sessionStartTime
        firstAttemptForNote = true
        lastCentsForNote = 0f
        _uiState.value = PracticeUiState(
            sequence = seq,
            targetNote = seq.noteAt(0),
        )
        startListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
