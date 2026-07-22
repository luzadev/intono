package com.notemusicali.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

private class AndroidToneGenerator(
    private val sampleRate: Int,
) : ToneGenerator {

    private var audioTrack: AudioTrack? = null

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
        val buffer = ShortArray(floatSamples.size) { i ->
            (floatSamples[i] * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        audioTrack = track
    }

    override fun stop() {
        audioTrack?.let { track ->
            try {
                track.stop()
                track.release()
            } catch (_: Exception) { }
        }
        audioTrack = null
    }

    override fun release() {
        stop()
    }
}

actual fun createToneGenerator(sampleRate: Int): ToneGenerator =
    AndroidToneGenerator(sampleRate)
