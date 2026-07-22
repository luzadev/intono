package com.notemusicali.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import platform.AVFAudio.AVAudioEngine
import platform.Foundation.NSNumber
import platform.Foundation.setValue
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive

@OptIn(ExperimentalForeignApi::class)
private class IosToneGenerator(
    private val sampleRate: Int,
) : ToneGenerator {

    private var engine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null

    private fun ensureEngine(): Pair<AVAudioEngine, AVAudioPlayerNode> {
        engine?.let { e ->
            playerNode?.let { p -> return Pair(e, p) }
        }

        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeDefault,
            options = 9u, // MixWithOthers (1) + DefaultToSpeaker (8)
            error = null,
        )
        session.setActive(true, error = null)

        val e = AVAudioEngine()
        val p = AVAudioPlayerNode()
        e.attachNode(p)

        val format = AVAudioFormat(
            standardFormatWithSampleRate = sampleRate.toDouble(),
            channels = 1u,
        )
        e.connect(p, to = e.mainMixerNode, format = format)
        e.prepare()
        e.startAndReturnError(null)
        p.play()

        engine = e
        playerNode = p
        return Pair(e, p)
    }

    private fun playBuffer(player: AVAudioPlayerNode, floatSamples: FloatArray) {
        val numSamples = floatSamples.size
        val format = AVAudioFormat(
            standardFormatWithSampleRate = sampleRate.toDouble(),
            channels = 1u,
        )
        val buf = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = numSamples.toUInt())
            ?: return

        buf.setValue(
            value = NSNumber(unsignedInt = numSamples.toUInt()),
            forKey = "frameLength",
        )

        val channelData = buf.floatChannelData ?: return
        val samples = channelData[0] ?: return
        for (i in 0 until numSamples) {
            samples[i] = floatSamples[i]
        }

        player.play()
        player.scheduleBuffer(buf, completionHandler = null)
    }

    override fun playTone(frequencyHz: Float, durationMs: Long) {
        val (_, player) = ensureEngine()
        player.stop()
        val samples = WaveGenerator.generateToneSamples(frequencyHz, durationMs, sampleRate)
        playBuffer(player, samples)
    }

    override fun playClick(accent: Boolean) {
        val (_, player) = ensureEngine()
        player.stop()
        val samples = WaveGenerator.generateClickSamples(accent, sampleRate)
        playBuffer(player, samples)
    }

    override fun stop() {
        playerNode?.stop()
        playerNode?.play()
    }

    override fun release() {
        playerNode?.stop()
        engine?.stop()
        playerNode = null
        engine = null
    }
}

actual fun createToneGenerator(sampleRate: Int): ToneGenerator =
    IosToneGenerator(sampleRate)
