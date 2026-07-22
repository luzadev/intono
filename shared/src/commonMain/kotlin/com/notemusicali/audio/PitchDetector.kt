package com.notemusicali.audio

import kotlin.math.abs

/**
 * YIN algorithm implementation for pitch detection.
 * Optimized for violin range (180Hz-2700Hz, G3-E7).
 *
 * The algorithm follows 5 steps:
 * 1. Difference function
 * 2. Cumulative mean normalized difference (CMND)
 * 3. Absolute threshold
 * 4. Parabolic interpolation
 * 5. Hz conversion
 */
class PitchDetector(
    private val sampleRate: Int = 44100,
    private val bufferSize: Int = 2048,
    private val threshold: Float = 0.15f,
    private val minFrequency: Float = 180f,
    private val maxFrequency: Float = 2700f,
    private val confidenceThreshold: Float = 0.7f,
) {
    private val maxLag = (sampleRate / minFrequency).toInt().coerceAtMost(bufferSize / 2)
    private val minLag = (sampleRate / maxFrequency).toInt().coerceAtLeast(2)

    private val recentPitches = ArrayDeque<Float>(3)

    data class PitchResult(
        val frequency: Float,
        val confidence: Float,
        val rmsAmplitude: Float,
    ) {
        // detect() only emits frequency > 0 after applying confidenceThreshold,
        // so validity must not re-check confidence against a hardcoded value
        val isValid: Boolean get() = frequency > 0f
    }

    fun detect(audioBuffer: FloatArray): PitchResult {
        val rms = computeRms(audioBuffer)
        if (rms < 0.01f) {
            return PitchResult(0f, 0f, rms)
        }

        // Step 1: Difference function
        val diff = differenceFunction(audioBuffer)

        // Step 2: Cumulative mean normalized difference
        val cmnd = cumulativeMeanNormalizedDifference(diff)

        // Step 3: Absolute threshold
        val lag = absoluteThreshold(cmnd) ?: return PitchResult(0f, 0f, rms)

        // Step 4: Parabolic interpolation
        val refinedLag = parabolicInterpolation(cmnd, lag)

        // Step 5: Hz conversion
        val frequency = sampleRate.toFloat() / refinedLag
        val confidence = 1f - cmnd[lag]

        if (frequency < minFrequency || frequency > maxFrequency || confidence < confidenceThreshold) {
            return PitchResult(0f, confidence, rms)
        }

        // Median filter on last 3 detections
        val filteredFrequency = medianFilter(frequency)

        return PitchResult(filteredFrequency, confidence, rms)
    }

    private fun computeRms(buffer: FloatArray): Float {
        var sum = 0f
        for (sample in buffer) {
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / buffer.size)
    }

    private fun differenceFunction(buffer: FloatArray): FloatArray {
        val diff = FloatArray(maxLag + 1)
        for (tau in 1..maxLag) {
            var sum = 0f
            for (i in 0 until bufferSize - tau) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }
        return diff
    }

    private fun cumulativeMeanNormalizedDifference(diff: FloatArray): FloatArray {
        val cmnd = FloatArray(diff.size)
        cmnd[0] = 1f
        var runningSum = 0f
        for (tau in 1 until diff.size) {
            runningSum += diff[tau]
            cmnd[tau] = if (runningSum != 0f) {
                diff[tau] * tau / runningSum
            } else {
                1f
            }
        }
        return cmnd
    }

    private fun absoluteThreshold(cmnd: FloatArray): Int? {
        // Find first dip below threshold after minLag
        var tau = minLag
        while (tau < cmnd.size) {
            if (cmnd[tau] < threshold) {
                // Walk to the minimum of this dip
                while (tau + 1 < cmnd.size && cmnd[tau + 1] < cmnd[tau]) {
                    tau++
                }
                return tau
            }
            tau++
        }
        return null
    }

    private fun parabolicInterpolation(cmnd: FloatArray, tau: Int): Float {
        if (tau < 1 || tau >= cmnd.size - 1) return tau.toFloat()

        val s0 = cmnd[tau - 1]
        val s1 = cmnd[tau]
        val s2 = cmnd[tau + 1]

        val adjustment = (s2 - s0) / (2f * (2f * s1 - s2 - s0))

        return if (abs(adjustment) < 1f) tau + adjustment else tau.toFloat()
    }

    private fun medianFilter(frequency: Float): Float {
        recentPitches.addLast(frequency)
        if (recentPitches.size > 3) {
            recentPitches.removeFirst()
        }

        if (recentPitches.size < 3) return frequency

        val sorted = recentPitches.toList().sorted()
        return sorted[1]
    }

    fun reset() {
        recentPitches.clear()
    }
}
