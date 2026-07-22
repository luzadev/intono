package com.notemusicali.audio

/**
 * Platform-agnostic tone generator for producing audio output.
 * Used by metronome, ear training, and reference note features.
 */
interface ToneGenerator {
    /** Play a sine wave tone at the given frequency for the specified duration in milliseconds. */
    fun playTone(frequencyHz: Float, durationMs: Long)

    /** Play a short click sound. If accent is true, play a louder/higher click. */
    fun playClick(accent: Boolean = false)

    /** Stop any currently playing sound. */
    fun stop()

    /** Release all audio resources. Call when done using the generator. */
    fun release()
}

/**
 * Creates a platform-specific ToneGenerator.
 */
expect fun createToneGenerator(sampleRate: Int = 44100): ToneGenerator
