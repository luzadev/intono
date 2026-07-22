package com.notemusicali.challenge

import kotlin.test.Test
import kotlin.test.assertEquals

class ChallengeScoringTest {

    @Test
    fun `accuracy is ratio of first-attempt notes over notes played`() {
        assertEquals(0.7f, ChallengeScoring.accuracy(notesPlayed = 10, firstAttemptNotes = 7))
    }

    @Test
    fun `accuracy is 1 when every note was right on first attempt`() {
        assertEquals(1.0f, ChallengeScoring.accuracy(notesPlayed = 5, firstAttemptNotes = 5))
    }

    @Test
    fun `accuracy is 0 when no notes were played`() {
        assertEquals(0f, ChallengeScoring.accuracy(notesPlayed = 0, firstAttemptNotes = 0))
    }

    @Test
    fun `accuracy never exceeds 1 even with inconsistent counters`() {
        assertEquals(1.0f, ChallengeScoring.accuracy(notesPlayed = 3, firstAttemptNotes = 4))
    }
}
