package com.notemusicali.audio

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

private class DesktopAudioSource(
    private val sampleRate: Int,
    private val bufferSize: Int,
) : AudioSource {
    override fun audioFlow(): Flow<FloatArray> = callbackFlow {
        val format = AudioFormat(
            sampleRate.toFloat(),
            16,           // 16-bit samples
            1,            // mono
            true,         // signed
            false,        // little-endian
        )

        val line: TargetDataLine = try {
            val dataLineInfo = javax.sound.sampled.DataLine.Info(TargetDataLine::class.java, format)
            AudioSystem.getLine(dataLineInfo) as TargetDataLine
        } catch (e: Exception) {
            close(IllegalStateException("No audio input available: ${e.message}"))
            return@callbackFlow
        }

        try {
            line.open(format, bufferSize * 2)
            line.start()

            val byteBuffer = ByteArray(bufferSize * 2) // 16-bit = 2 bytes per sample

            while (isActive) {
                val bytesRead = line.read(byteBuffer, 0, byteBuffer.size)
                if (bytesRead > 0) {
                    val samplesRead = bytesRead / 2
                    val floatBuffer = FloatArray(samplesRead)
                    for (i in 0 until samplesRead) {
                        val low = byteBuffer[i * 2].toInt() and 0xFF
                        val high = byteBuffer[i * 2 + 1].toInt()
                        val sample = (high shl 8) or low
                        floatBuffer[i] = sample / 32768f
                    }
                    trySend(floatBuffer)
                }
            }
        } finally {
            line.stop()
            line.close()
        }

        awaitClose()
    }
}

actual fun createAudioSource(sampleRate: Int, bufferSize: Int): AudioSource =
    DesktopAudioSource(sampleRate, bufferSize)
