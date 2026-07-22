package com.notemusicali.ui.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notemusicali.audio.AudioProcessor
import com.notemusicali.audio.InstrumentPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class TunerViewModel : ViewModel() {

    private val audioProcessor = AudioProcessor()

    companion object {
        private const val HOLD_TIME_MS = 1500L
    }

    enum class TuningState {
        SILENCE,
        IN_TUNE,
        OUT_OF_TUNE,
    }

    data class TunerUiState(
        val noteName: String = "",
        val octave: Int = 0,
        val frequency: Float = 0f,
        val centsDeviation: Float = 0f,
        val confidence: Float = 0f,
        val tuningState: TuningState = TuningState.SILENCE,
        val instrument: InstrumentPreset = InstrumentPreset.VIOLINO,
    )

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var isListening = false
    private var silenceJob: Job? = null

    fun selectInstrument(preset: InstrumentPreset) {
        val wasListening = isListening
        if (wasListening) stopListening()
        audioProcessor.updatePreset(preset)
        _uiState.value = TunerUiState(instrument = preset)
        if (wasListening) startListening()
    }

    fun startListening() {
        if (isListening) return
        isListening = true
        audioProcessor.start(viewModelScope)

        viewModelScope.launch {
            audioProcessor.result.collect { result ->
                when (result) {
                    is AudioProcessor.ProcessedResult.Detected -> {
                        silenceJob?.cancel()
                        silenceJob = null
                        val noteResult = result.noteResult
                        val isInTune = abs(noteResult.centsDeviation) <= 15f
                        _uiState.value = _uiState.value.copy(
                            noteName = noteResult.note.displayName,
                            octave = noteResult.note.octave,
                            frequency = noteResult.frequency,
                            centsDeviation = noteResult.centsDeviation,
                            confidence = result.confidence,
                            tuningState = if (isInTune) TuningState.IN_TUNE else TuningState.OUT_OF_TUNE,
                        )
                    }
                    is AudioProcessor.ProcessedResult.Silence,
                    is AudioProcessor.ProcessedResult.NoNote -> {
                        if (silenceJob == null && _uiState.value.noteName.isNotEmpty()) {
                            silenceJob = viewModelScope.launch {
                                delay(HOLD_TIME_MS)
                                _uiState.value = _uiState.value.copy(
                                    noteName = "",
                                    tuningState = TuningState.SILENCE,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        silenceJob?.cancel()
        silenceJob = null
        audioProcessor.stop()
        _uiState.value = TunerUiState()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
