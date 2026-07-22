package com.notemusicali.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive

private class AndroidAudioSource(
    private val sampleRate: Int,
    private val bufferSize: Int,
) : AudioSource {

    @SuppressLint("MissingPermission")
    override fun audioFlow(): Flow<FloatArray> = callbackFlow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
        )
        val recordBufferSize = maxOf(minBufferSize, bufferSize * 4)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            recordBufferSize,
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            close(IllegalStateException("AudioRecord failed to initialize"))
            return@callbackFlow
        }

        audioRecord.startRecording()

        val buffer = FloatArray(bufferSize)

        try {
            while (isActive) {
                val read = audioRecord.read(buffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
                if (read > 0) {
                    trySend(buffer.copyOf(read))
                } else if (read < 0) {
                    break
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }

        awaitClose()
    }
}

actual fun createAudioSource(sampleRate: Int, bufferSize: Int): AudioSource =
    AndroidAudioSource(sampleRate, bufferSize)
