package com.notemusicali.metronome

import com.notemusicali.audio.ToneGenerator
import com.notemusicali.audio.createToneGenerator
import com.notemusicali.util.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimeSignature(val displayName: String, val beatsPerMeasure: Int) {
    TWO_FOUR("2/4", 2),
    THREE_FOUR("3/4", 3),
    FOUR_FOUR("4/4", 4),
    SIX_EIGHT("6/8", 6),
}

class MetronomeEngine(
    private val toneGenerator: ToneGenerator = createToneGenerator(),
    private val nowMs: () -> Long = { currentTimeMillis() },
) {
    private val _currentBeat = MutableStateFlow(0)
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    var bpm: Int = 120
    var timeSignature: TimeSignature = TimeSignature.FOUR_FOUR

    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (_isPlaying.value) return
        _isPlaying.value = true
        _currentBeat.value = 0

        job = scope.launch {
            var beat = 0
            // Absolute-time grid: each tick is scheduled from the accumulated ideal
            // time, so click/scheduler latency never accumulates as drift.
            var nextTickMs = nowMs().toDouble()
            while (isActive) {
                val isAccent = beat == 0
                _currentBeat.value = beat
                toneGenerator.playClick(accent = isAccent)

                nextTickMs += 60_000.0 / bpm
                delay((nextTickMs - nowMs()).toLong().coerceAtLeast(0L))

                beat = (beat + 1) % timeSignature.beatsPerMeasure
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _isPlaying.value = false
        _currentBeat.value = 0
        toneGenerator.stop()
    }

    fun release() {
        stop()
        toneGenerator.release()
    }
}
