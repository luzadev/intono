package com.notemusicali.ui.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.audio.AudioProcessor
import com.notemusicali.metronome.MetronomeEngine
import com.notemusicali.metronome.TimeSignature
import com.notemusicali.util.currentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class TuningDisplay {
    SILENCE, IN_TUNE, OUT_OF_TUNE
}

data class MetronomeUiState(
    val bpm: Int = 120,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val isPlaying: Boolean = false,
    val currentBeat: Int = 0,
    // Tuner
    val noteName: String = "",
    val octave: Int = 0,
    val frequency: Float = 0f,
    val centsDeviation: Float = 0f,
    val tuningDisplay: TuningDisplay = TuningDisplay.SILENCE,
    val tunerActive: Boolean = false,
)

class MetronomeViewModel : ViewModel() {
    private val engine = MetronomeEngine()
    private val audioProcessor = AudioProcessor()

    private val _uiState = MutableStateFlow(MetronomeUiState())
    val uiState: StateFlow<MetronomeUiState> = _uiState.asStateFlow()

    // Tap tempo tracking
    private val tapTimes = mutableListOf<Long>()
    private var silenceJob: Job? = null
    private var isListening = false

    init {
        viewModelScope.launch {
            engine.isPlaying.collect { playing ->
                _uiState.value = _uiState.value.copy(isPlaying = playing)
            }
        }
        viewModelScope.launch {
            engine.currentBeat.collect { beat ->
                _uiState.value = _uiState.value.copy(currentBeat = beat)
            }
        }
    }

    fun setBpm(bpm: Int) {
        val clamped = bpm.coerceIn(30, 240)
        engine.bpm = clamped
        _uiState.value = _uiState.value.copy(bpm = clamped)
    }

    fun setTimeSignature(ts: TimeSignature) {
        val wasPlaying = engine.isPlaying.value
        if (wasPlaying) engine.stop()
        engine.timeSignature = ts
        _uiState.value = _uiState.value.copy(timeSignature = ts)
        if (wasPlaying) engine.start(viewModelScope)
    }

    fun togglePlayStop() {
        if (engine.isPlaying.value) {
            engine.stop()
        } else {
            engine.start(viewModelScope)
        }
    }

    fun tapTempo() {
        val now = currentTimeMillis()
        tapTimes.add(now)

        // Keep only last 5 taps
        if (tapTimes.size > 5) tapTimes.removeFirst()

        if (tapTimes.size >= 2) {
            val intervals = tapTimes.zipWithNext { a, b -> b - a }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val bpm = (60_000.0 / avgInterval).toInt().coerceIn(30, 240)
                setBpm(bpm)
            }
        }
    }

    fun startTuner() {
        if (isListening) return
        isListening = true
        audioProcessor.start(viewModelScope)
        _uiState.value = _uiState.value.copy(tunerActive = true)

        viewModelScope.launch {
            audioProcessor.result.collect { result ->
                when (result) {
                    is AudioProcessor.ProcessedResult.Detected -> {
                        silenceJob?.cancel()
                        silenceJob = null
                        val nr = result.noteResult
                        val inTune = abs(nr.centsDeviation) <= 15f
                        _uiState.value = _uiState.value.copy(
                            noteName = nr.note.displayName,
                            octave = nr.note.octave,
                            frequency = nr.frequency,
                            centsDeviation = nr.centsDeviation,
                            tuningDisplay = if (inTune) TuningDisplay.IN_TUNE else TuningDisplay.OUT_OF_TUNE,
                        )
                    }
                    is AudioProcessor.ProcessedResult.Silence,
                    is AudioProcessor.ProcessedResult.NoNote -> {
                        if (silenceJob == null && _uiState.value.noteName.isNotEmpty()) {
                            silenceJob = viewModelScope.launch {
                                delay(1500L)
                                _uiState.value = _uiState.value.copy(
                                    noteName = "",
                                    tuningDisplay = TuningDisplay.SILENCE,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopTuner() {
        isListening = false
        silenceJob?.cancel()
        silenceJob = null
        audioProcessor.stop()
        _uiState.value = _uiState.value.copy(
            tunerActive = false,
            noteName = "",
            tuningDisplay = TuningDisplay.SILENCE,
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopTuner()
        engine.release()
    }
}
