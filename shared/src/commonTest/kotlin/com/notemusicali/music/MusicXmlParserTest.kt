package com.notemusicali.music

import kotlin.test.Test
import kotlin.test.assertEquals

class MusicXmlParserTest {

    @Test
    fun parseSimpleMusicXmlWithTwoNotes() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise>
              <part id="P1">
                <measure number="1">
                  <note>
                    <pitch>
                      <step>C</step>
                      <octave>4</octave>
                    </pitch>
                    <duration>4</duration>
                  </note>
                  <note>
                    <pitch>
                      <step>E</step>
                      <octave>4</octave>
                    </pitch>
                    <duration>4</duration>
                  </note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(2, sequence.notes.size)
        assertEquals("DO", sequence.notes[0].displayName)
        assertEquals(4, sequence.notes[0].octave)
        assertEquals("MI", sequence.notes[1].displayName)
        assertEquals(4, sequence.notes[1].octave)
    }

    @Test
    fun parseMusicXmlWithSharpNotes() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise>
              <part id="P1">
                <measure number="1">
                  <note>
                    <pitch>
                      <step>F</step>
                      <alter>1</alter>
                      <octave>4</octave>
                    </pitch>
                    <duration>4</duration>
                  </note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(1, sequence.notes.size)
        assertEquals("FA#", sequence.notes[0].displayName)
    }

    @Test
    fun parseMusicXmlSkipsRests() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise>
              <part id="P1">
                <measure number="1">
                  <note>
                    <pitch>
                      <step>A</step>
                      <octave>4</octave>
                    </pitch>
                    <duration>4</duration>
                  </note>
                  <note>
                    <rest/>
                    <duration>4</duration>
                  </note>
                  <note>
                    <pitch>
                      <step>B</step>
                      <octave>4</octave>
                    </pitch>
                    <duration>4</duration>
                  </note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(2, sequence.notes.size)
        assertEquals("LA", sequence.notes[0].displayName)
        assertEquals("SI", sequence.notes[1].displayName)
    }

    @Test
    fun parseTakesOnlyFirstPartOfMultiPartScore() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise>
              <part id="P1">
                <measure number="1">
                  <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
                </measure>
              </part>
              <part id="P2">
                <measure number="1">
                  <note><pitch><step>G</step><octave>2</octave></pitch><duration>4</duration></note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(1, sequence.notes.size)
        assertEquals("DO", sequence.notes[0].displayName)
    }

    @Test
    fun parseKeepsOnlyFirstNoteOfChord() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
              <note><chord/><pitch><step>E</step><octave>4</octave></pitch><duration>4</duration></note>
              <note><chord/><pitch><step>G</step><octave>4</octave></pitch><duration>4</duration></note>
              <note><pitch><step>D</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(2, sequence.notes.size)
        assertEquals("DO", sequence.notes[0].displayName)
        assertEquals("RE", sequence.notes[1].displayName)
    }

    @Test
    fun parseKeepsOnlyFirstVoice() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice></note>
              <note><pitch><step>D</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice></note>
              <backup><duration>8</duration></backup>
              <note><pitch><step>C</step><octave>3</octave></pitch><duration>8</duration><voice>2</voice></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(2, sequence.notes.size)
        assertEquals(5, sequence.notes[0].octave)
        assertEquals(5, sequence.notes[1].octave)
    }

    @Test
    fun parseSkipsGraceNotes() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><grace/><pitch><step>B</step><octave>4</octave></pitch></note>
              <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(1, sequence.notes.size)
        assertEquals("DO", sequence.notes[0].displayName)
    }

    @Test
    fun parseSkipsTieContinuations() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><tie type="start"/></note>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><tie type="stop"/></note>
              <note><pitch><step>D</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(2, sequence.notes.size)
        assertEquals("DO", sequence.notes[0].displayName)
        assertEquals("RE", sequence.notes[1].displayName)
    }

    @Test
    fun parseSkipsRestsWithAttributes() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><rest measure="yes"/><duration>16</duration></note>
              <note><pitch><step>E</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(1, sequence.notes.size)
        assertEquals("MI", sequence.notes[0].displayName)
    }

    @Test
    fun parseDerivesDurationFromDivisionsWhenTypeIsMissing() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <attributes><divisions>2</divisions></attributes>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>8</duration></note>
              <note><pitch><step>D</step><octave>4</octave></pitch><duration>2</duration></note>
              <note><pitch><step>E</step><octave>4</octave></pitch><duration>1</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(3, sequence.notes.size)
        assertEquals(NoteDuration.WHOLE, sequence.notes[0].duration) // 8/2 = 4 quarters
        assertEquals(NoteDuration.QUARTER, sequence.notes[1].duration) // 2/2 = 1 quarter
        assertEquals(NoteDuration.EIGHTH, sequence.notes[2].duration) // 1/2 = half quarter
    }

    @Test
    fun parseExplicitTypeWinsOverDurationRatio() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <attributes><divisions>2</divisions></attributes>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>8</duration><type>half</type></note>
            </measure></part></score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(NoteDuration.HALF, sequence.notes[0].duration)
    }

    @Test
    fun parseEmptyMusicXmlReturnsEmptySequence() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <score-partwise>
              <part id="P1">
                <measure number="1">
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        val sequence = MusicXmlParser.parse(xml)
        assertEquals(0, sequence.notes.size)
    }

    @Test
    fun parseExtractsTimeSignature() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <attributes><time><beats>3</beats><beat-type>4</beat-type></time></attributes>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()
        val sequence = MusicXmlParser.parse(xml)
        assertEquals(3, sequence.beats)
        assertEquals(4, sequence.beatType)
    }

    @Test
    fun parseDefaultsToFourFourWithoutTimeSignature() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()
        val sequence = MusicXmlParser.parse(xml)
        assertEquals(4, sequence.beats)
        assertEquals(4, sequence.beatType)
    }

    @Test
    fun parseExtractsTimeSignatureWithAttributes() {
        val xml = """
            <score-partwise><part id="P1"><measure number="1">
              <attributes><time symbol="common"><beats>4</beats><beat-type>4</beat-type></time></attributes>
              <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>
            </measure></part></score-partwise>
        """.trimIndent()
        val sequence = MusicXmlParser.parse(xml)
        assertEquals(4, sequence.beats)
        assertEquals(4, sequence.beatType)
    }
}
