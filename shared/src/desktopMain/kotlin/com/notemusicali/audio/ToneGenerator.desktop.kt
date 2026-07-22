package com.notemusicali.audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

private class DesktopToneGenerator(
    private val sampleRate: Int,
) : ToneGenerator {

    private var currentLine: javax.sound.sampled.SourceDataLine? = null
    private var playThread: Thread? = null

    override fun playTone(frequencyHz: Float, durationMs: Long) {
        stop()
        val floatSamples = WaveGenerator.generateToneSamples(frequencyHz, durationMs, sampleRate)
        playFloatSamples(floatSamples)
    }

    override fun playClick(accent: Boolean) {
        stop()
        val floatSamples = WaveGenerator.generateClickSamples(accent, sampleRate)
        playFloatSamples(floatSamples)
    }

    private fun playFloatSamples(floatSamples: FloatArray) {
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val buffer = ByteArray(floatSamples.size * 2)

        for (i in floatSamples.indices) {
            val value = (floatSamples[i] * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            buffer[i * 2] = (value.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
        }

        playThread = Thread {
            try {
                val line = AudioSystem.getSourceDataLine(format)
                line.open(format)
                currentLine = line
                line.start()
                line.write(buffer, 0, buffer.size)
                line.drain()
                line.close()
            } catch (_: Exception) { }
        }.apply { isDaemon = true; start() }
    }

    override fun stop() {
        currentLine?.let { line ->
            try {
                line.stop()
                line.close()
            } catch (_: Exception) { }
        }
        currentLine = null
        playThread?.interrupt()
        playThread = null
    }

    override fun release() {
        stop()
    }
}

actual fun createToneGenerator(sampleRate: Int): ToneGenerator =
    DesktopToneGenerator(sampleRate)
