package com.notemusicali.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVAudioSessionPortOverrideSpeaker
import platform.AVFAudio.setActive
import platform.AVFAudio.setPreferredSampleRate
import platform.Foundation.NSLog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
private class IosAudioSource(
    private val sampleRate: Int,
    private val bufferSize: Int,
) : AudioSource {

    override fun audioFlow(): Flow<FloatArray> = callbackFlow {
        try {
            NSLog("InTono AudioSource: starting setup sampleRate=%d bufferSize=%d", sampleRate, bufferSize)

            val session = AVAudioSession.sharedInstance()
            val categoryOk = session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeDefault,
                options = 8u, // DefaultToSpeaker
                error = null,
            )
            NSLog("InTono AudioSource: setCategory result=%s", if (categoryOk) "OK" else "FAIL")

            session.setPreferredSampleRate(sampleRate.toDouble(), error = null)
            session.setActive(true, error = null)

            // Route audio to main speaker (works even with PlayAndRecord)
            session.overrideOutputAudioPort(AVAudioSessionPortOverrideSpeaker, error = null)

            // Request microphone permission at runtime
            val granted = suspendCoroutine { cont ->
                session.requestRecordPermission { allowed ->
                    cont.resume(allowed)
                }
            }
            NSLog("InTono AudioSource: mic permission=%s", if (granted) "granted" else "denied")

            if (!granted) {
                close()
                return@callbackFlow
            }

            val engine = AVAudioEngine()
            NSLog("InTono AudioSource: engine created")

            val inputNode = engine.inputNode
            NSLog("InTono AudioSource: inputNode obtained")

            // Use the input node's native format for the tap to avoid format conflicts.
            // Then resample/convert ourselves if needed.
            val hwFormat = inputNode.outputFormatForBus(0u)
            val hwSampleRate = hwFormat.sampleRate
            val hwChannels = hwFormat.channelCount
            NSLog("InTono AudioSource: hw format sampleRate=%.0f channels=%d", hwSampleRate, hwChannels)

            // Use mono format at hardware sample rate to avoid resampling issues
            val tapFormat = if (hwSampleRate > 0.0) {
                AVAudioFormat(
                    standardFormatWithSampleRate = hwSampleRate,
                    channels = 1u,
                )
            } else {
                AVAudioFormat(
                    standardFormatWithSampleRate = sampleRate.toDouble(),
                    channels = 1u,
                )
            }

            // Calculate downsample ratio if hardware rate differs from requested rate
            val actualSampleRate = if (hwSampleRate > 0.0) hwSampleRate else sampleRate.toDouble()
            val downsampleRatio = actualSampleRate / sampleRate.toDouble()

            // Accumulation buffer: tap may deliver variable-sized chunks,
            // but PitchDetector needs exactly `bufferSize` samples.
            val accumulator = FloatArray(bufferSize)
            var accumulatorPos = 0

            val tapBufferSize: UInt = 4096u

            NSLog("InTono AudioSource: installing tap, downsampleRatio=%.4f", downsampleRatio)

            // Tap the input node with hardware-compatible format.
            inputNode.installTapOnBus(
                bus = 0u,
                bufferSize = tapBufferSize,
                format = tapFormat,
            ) { buffer: AVAudioPCMBuffer?, _ ->
                if (buffer == null) return@installTapOnBus

                val frameCount = buffer.frameLength.toInt()
                val channelData = buffer.floatChannelData ?: return@installTapOnBus
                val samples = channelData[0] ?: return@installTapOnBus

                if (downsampleRatio <= 1.01) {
                    // No resampling needed (rates are essentially the same)
                    var srcPos = 0
                    while (srcPos < frameCount) {
                        val remaining = bufferSize - accumulatorPos
                        val available = frameCount - srcPos
                        val toCopy = minOf(remaining, available)

                        for (i in 0 until toCopy) {
                            accumulator[accumulatorPos + i] = samples[srcPos + i]
                        }
                        accumulatorPos += toCopy
                        srcPos += toCopy

                        if (accumulatorPos == bufferSize) {
                            trySend(accumulator.copyOf())
                            accumulatorPos = 0
                        }
                    }
                } else {
                    // Simple decimation for downsampling (e.g., 48000→44100)
                    var srcIdx = 0.0
                    while (srcIdx < frameCount) {
                        val idx = srcIdx.toInt()
                        if (idx < frameCount) {
                            accumulator[accumulatorPos] = samples[idx]
                            accumulatorPos++
                            if (accumulatorPos == bufferSize) {
                                trySend(accumulator.copyOf())
                                accumulatorPos = 0
                            }
                        }
                        srcIdx += downsampleRatio
                    }
                }
            }

            NSLog("InTono AudioSource: tap installed, preparing engine")
            engine.prepare()

            val startOk = engine.startAndReturnError(null)
            NSLog("InTono AudioSource: engine start result=%s", if (startOk) "OK" else "FAIL")

            awaitClose {
                NSLog("InTono AudioSource: closing, removing tap")
                inputNode.removeTapOnBus(0u)
                engine.stop()
                session.setActive(false, error = null)
            }
        } catch (e: Exception) {
            NSLog("InTono AudioSource: EXCEPTION %s", e.message ?: "unknown")
            close()
        }
    }
}

actual fun createAudioSource(sampleRate: Int, bufferSize: Int): AudioSource =
    IosAudioSource(sampleRate, bufferSize)
