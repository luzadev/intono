package com.notemusicali.audio

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PitchResultTest {

    @Test
    fun `result emitted by detector is valid regardless of its confidence value`() {
        // detect() only emits frequency > 0 for results that already passed the
        // detector's configurable confidenceThreshold, so a custom-threshold
        // detector (e.g. 0.5) must not be silently re-filtered by a hardcoded 0.7
        val result = PitchDetector.PitchResult(frequency = 440f, confidence = 0.5f, rmsAmplitude = 0.1f)
        assertTrue(result.isValid)
    }

    @Test
    fun `zero frequency result is not valid`() {
        val result = PitchDetector.PitchResult(frequency = 0f, confidence = 0f, rmsAmplitude = 0.001f)
        assertFalse(result.isValid)
    }
}
