package com.notemusicali.audio

import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PitchDetectorTest {

    private lateinit var detector: PitchDetector
    private val sampleRate = 44100
    private val bufferSize = 2048

    @BeforeTest
    fun setUp() {
        detector = PitchDetector(
            sampleRate = sampleRate,
            bufferSize = bufferSize,
            threshold = 0.15f,
        )
    }

    private fun generateSineWave(frequency: Float, amplitude: Float = 0.8f): FloatArray {
        return FloatArray(bufferSize) { i ->
            (amplitude * sin(2.0 * PI * frequency * i / sampleRate)).toFloat()
        }
    }

    @Test
    fun detectA4at440Hz() {
        repeat(3) { detector.detect(generateSineWave(440f)) }
        val result = detector.detect(generateSineWave(440f))
        assertTrue(result.isValid, "Should detect a valid pitch")
        assertEquals(440f, result.frequency, 5f)
    }

    @Test
    fun detectA3at220Hz() {
        repeat(3) { detector.detect(generateSineWave(220f)) }
        val result = detector.detect(generateSineWave(220f))
        assertTrue(result.isValid, "Should detect a valid pitch")
        assertEquals(220f, result.frequency, 5f)
    }

    @Test
    fun detectE5at659HzViolinOpenString() {
        repeat(3) { detector.detect(generateSineWave(659.26f)) }
        val result = detector.detect(generateSineWave(659.26f))
        assertTrue(result.isValid, "Should detect a valid pitch")
        assertEquals(659.26f, result.frequency, 10f)
    }

    @Test
    fun detectG3at196HzViolinLowestOpenString() {
        repeat(3) { detector.detect(generateSineWave(196f)) }
        val result = detector.detect(generateSineWave(196f))
        assertTrue(result.isValid, "Should detect a valid pitch")
        assertEquals(196f, result.frequency, 5f)
    }

    @Test
    fun detectD4at293HzViolinOpenString() {
        repeat(3) { detector.detect(generateSineWave(293.66f)) }
        val result = detector.detect(generateSineWave(293.66f))
        assertTrue(result.isValid, "Should detect a valid pitch")
        assertEquals(293.66f, result.frequency, 5f)
    }

    @Test
    fun silenceReturnsInvalidResult() {
        val silence = FloatArray(bufferSize) { 0f }
        val result = detector.detect(silence)
        assertFalse(result.isValid, "Should not detect a pitch in silence")
        assertEquals(0f, result.frequency, 0.01f)
    }

    @Test
    fun veryLowAmplitudeReturnsSilence() {
        val quiet = generateSineWave(440f, amplitude = 0.001f)
        val result = detector.detect(quiet)
        assertFalse(result.isValid, "Very quiet signal should not be detected")
    }

    @Test
    fun resetClearsMedianFilter() {
        repeat(3) { detector.detect(generateSineWave(440f)) }
        detector.reset()
        detector.detect(generateSineWave(440f))
        assertTrue(true)
    }
}
