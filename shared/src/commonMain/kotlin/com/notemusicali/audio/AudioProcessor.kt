package com.notemusicali.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Orchestrates the audio pipeline: AudioSource → PitchDetector → NoteConverter.
 * Exposes processed results as StateFlow.
 */
class AudioProcessor(
    preset: InstrumentPreset = InstrumentPreset.VIOLINO,
) {
    private var audioSource: AudioSource = createAudioSource(bufferSize = preset.bufferSize)
    private var pitchDetector: PitchDetector = PitchDetector(
        bufferSize = preset.bufferSize,
        threshold = preset.yinThreshold,
        minFrequency = preset.minFrequency,
        maxFrequency = preset.maxFrequency,
    )

    fun updatePreset(preset: InstrumentPreset) {
        stop()
        audioSource = createAudioSource(bufferSize = preset.bufferSize)
        pitchDetector = PitchDetector(
            bufferSize = preset.bufferSize,
            threshold = preset.yinThreshold,
            minFrequency = preset.minFrequency,
            maxFrequency = preset.maxFrequency,
        )
    }

    sealed class ProcessedResult {
        data object Silence : ProcessedResult()

        data class Detected(
            val noteResult: NoteConverter.NoteResult,
            val confidence: Float,
            val rmsAmplitude: Float,
        ) : ProcessedResult()

        data class NoNote(
            val rmsAmplitude: Float,
        ) : ProcessedResult()
    }

    private val _result = MutableStateFlow<ProcessedResult>(ProcessedResult.Silence)
    val result: StateFlow<ProcessedResult> = _result.asStateFlow()

    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        stop()
        pitchDetector.reset()
        job = scope.launch {
            audioSource.audioFlow()
                .flowOn(Dispatchers.Default)
                .collect { buffer ->
                    val pitchResult = pitchDetector.detect(buffer)
                    _result.value = when {
                        pitchResult.rmsAmplitude < 0.01f -> ProcessedResult.Silence
                        pitchResult.isValid -> {
                            val noteResult = NoteConverter.frequencyToNote(pitchResult.frequency)
                            ProcessedResult.Detected(
                                noteResult = noteResult,
                                confidence = pitchResult.confidence,
                                rmsAmplitude = pitchResult.rmsAmplitude,
                            )
                        }
                        else -> ProcessedResult.NoNote(pitchResult.rmsAmplitude)
                    }
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _result.value = ProcessedResult.Silence
    }
}
