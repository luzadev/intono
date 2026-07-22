package com.notemusicali.metronome

import com.notemusicali.audio.ToneGenerator
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertTrue

class MetronomeEngineTest {

    private class RecordingToneGenerator(private val nowMs: () -> Long) : ToneGenerator {
        val clickTimes = mutableListOf<Long>()
        override fun playTone(frequencyHz: Float, durationMs: Long) {}
        override fun playClick(accent: Boolean) {
            clickTimes.add(nowMs())
        }
        override fun stop() {}
        override fun release() {}
    }

    @Test
    fun `clicks stay on absolute time grid without drift`() = runTest {
        val clock = { testScheduler.currentTime }
        val generator = RecordingToneGenerator(clock)
        val engine = MetronomeEngine(toneGenerator = generator, nowMs = clock)
        engine.bpm = 137 // 60000/137 = 437.956ms: non-integer interval exposes quantization drift

        engine.start(backgroundScope)
        advanceTimeBy(60_001)
        engine.stop()

        assertTrue(generator.clickTimes.size in 137..139, "expected ~137 beats in 1 min, got ${generator.clickTimes.size}")
        generator.clickTimes.forEachIndexed { k, t ->
            val expected = (k * 60_000.0 / 137).roundToLong()
            assertTrue(
                abs(t - expected) <= 1,
                "beat $k fired at ${t}ms, expected ~${expected}ms (drift ${t - expected}ms)",
            )
        }
    }

    @Test
    fun `bpm change mid-run reschedules subsequent beats`() = runTest {
        val clock = { testScheduler.currentTime }
        val generator = RecordingToneGenerator(clock)
        val engine = MetronomeEngine(toneGenerator = generator, nowMs = clock)
        engine.bpm = 60

        engine.start(backgroundScope)
        advanceTimeBy(2_001) // beats at 0, 1000, 2000
        engine.bpm = 120
        advanceTimeBy(2_000) // beats every 500ms from 2500 (approximately)
        engine.stop()

        val afterChange = generator.clickTimes.filter { it > 2_000 }
        assertTrue(afterChange.size >= 3, "expected faster beats after bpm change, got $afterChange")
        val intervals = afterChange.zipWithNext { a, b -> b - a }
        intervals.forEach { interval ->
            assertTrue(abs(interval - 500) <= 1, "expected ~500ms interval after bpm change, got $interval")
        }
    }
}
