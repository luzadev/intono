package com.notemusicali.history

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PracticeSessionMeterTest {

    @Test
    fun `session carries its meter`() {
        val session = PracticeSession(
            id = "1", sequenceName = "Test", instrumentName = "Violino",
            dateMillis = 0L, totalTimeMs = 0L,
            noteResults = emptyList(), noteMidiNumbers = listOf(60),
            beats = 3, beatType = 4,
        )
        assertEquals(3, session.beats)
        assertEquals(4, session.beatType)
    }

    @Test
    fun `legacy json without meter decodes with four four defaults`() {
        val legacy = """{"id":"1","sequenceName":"Old","instrumentName":"Violino",""" +
            """"dateMillis":0,"totalTimeMs":0,"noteResults":[],"noteMidiNumbers":[60]}"""
        val session = Json { ignoreUnknownKeys = true }.decodeFromString<PracticeSession>(legacy)
        assertEquals(4, session.beats)
        assertEquals(4, session.beatType)
    }
}
