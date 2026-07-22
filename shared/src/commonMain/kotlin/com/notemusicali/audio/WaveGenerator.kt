package com.notemusicali.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Shared waveform generator with additive synthesis and ADSR envelopes.
 * Produces piano-like tones and realistic metronome clicks.
 */
object WaveGenerator {

    // Piano-like harmonic series: (multiplier, relative amplitude)
    // Mimics the overtone structure of a struck string
    private val pianoHarmonics = floatArrayOf(
        1f, 1.0f,
        2f, 0.50f,
        3f, 0.28f,
        4f, 0.14f,
        5f, 0.09f,
        6f, 0.05f,
        7f, 0.03f,
        8f, 0.015f,
    )
    private const val NUM_HARMONICS = 8
    private const val HARMONIC_SUM = 2.085f // sum of amplitudes for normalization

    private const val TWO_PI = 2.0 * PI

    /**
     * Generate a piano-like tone with additive synthesis and ADSR envelope.
     */
    fun generateToneSamples(
        frequencyHz: Float,
        durationMs: Long,
        sampleRate: Int,
    ): FloatArray {
        val totalSamples = (sampleRate * durationMs / 1000).toInt()
        val samples = FloatArray(totalSamples)

        // ADSR envelope (in samples)
        val attackLen = (sampleRate * 0.008).toInt()   // 8ms attack
        val decayLen = (sampleRate * 0.12).toInt()     // 120ms decay
        val sustainLevel = 0.50
        val releaseLen = (sampleRate * 0.06).toInt()   // 60ms release
        val sustainEnd = totalSamples - releaseLen

        val amplitude = 0.70

        // Per-harmonic: progressively faster decay for higher harmonics (natural behavior)
        for (i in 0 until totalSamples) {
            // ADSR envelope
            val env = when {
                i < attackLen -> i.toDouble() / attackLen
                i < attackLen + decayLen -> {
                    val progress = (i - attackLen).toDouble() / decayLen
                    1.0 - (1.0 - sustainLevel) * progress
                }
                i >= sustainEnd -> {
                    val progress = (i - sustainEnd).toDouble() / releaseLen
                    sustainLevel * (1.0 - progress)
                }
                else -> sustainLevel
            }

            // Additive synthesis
            var sample = 0.0
            var idx = 0
            while (idx < NUM_HARMONICS) {
                val mult = pianoHarmonics[idx * 2]
                val amp = pianoHarmonics[idx * 2 + 1]
                val harmonicFreq = frequencyHz * mult
                // Anti-aliasing: skip harmonics above Nyquist frequency
                if (harmonicFreq > sampleRate / 2) break
                // Higher harmonics decay faster over time
                val harmonicDecay = exp(-i.toDouble() / sampleRate * mult * 1.2)
                sample += amp * harmonicDecay * sin(TWO_PI * harmonicFreq * i / sampleRate)
                idx++
            }

            // Normalize and apply envelope
            samples[i] = (sample / HARMONIC_SUM * env * amplitude).toFloat()
        }

        return samples
    }

    /**
     * Generate a realistic metronome click with tonal + noise components.
     * Uses inharmonic overtones for a wood-block-like sound.
     */
    fun generateClickSamples(
        accent: Boolean,
        sampleRate: Int,
    ): FloatArray {
        val durationMs = if (accent) 40 else 30
        val totalSamples = sampleRate * durationMs / 1000
        val samples = FloatArray(totalSamples)

        val toneFreq = if (accent) 1800.0 else 1200.0
        val amplitude = if (accent) 0.80 else 0.60
        val noiseAmount = 0.25

        // Simple deterministic pseudo-random for noise (reproducible across platforms)
        var noiseState = 48271L

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate

            // Sharp exponential decay with initial transient
            val progress = i.toDouble() / totalSamples
            val decay = exp(-progress * 8.0)

            // Tonal component: inharmonic overtones for wood-like timbre
            val tone = sin(TWO_PI * toneFreq * t) +
                0.45 * sin(TWO_PI * toneFreq * 2.4 * t) +
                0.20 * sin(TWO_PI * toneFreq * 4.1 * t) +
                0.10 * sin(TWO_PI * toneFreq * 5.7 * t)

            // Noise component (linear congruential generator)
            noiseState = (noiseState * 1103515245L + 12345L) and 0x7FFFFFFFL
            val noise = noiseState.toDouble() / 0x7FFFFFFFL * 2.0 - 1.0

            // Mix tone and noise, then apply decay
            val mixed = (1.0 - noiseAmount) * tone / 1.75 + noiseAmount * noise
            samples[i] = (mixed * decay * amplitude).toFloat()
        }

        return samples
    }
}
