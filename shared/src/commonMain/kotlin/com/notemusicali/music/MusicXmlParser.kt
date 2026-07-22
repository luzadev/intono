package com.notemusicali.music

/**
 * Parses MusicXML content to extract a monophonic note sequence suitable for
 * guided practice:
 * - only the first <part> is read (typically the melody / right hand)
 * - only the first voice of that part is kept
 * - chord continuation notes (<chord/>), grace notes and tied-note
 *   continuations (<tie type="stop">) are skipped
 * - rests are skipped
 * - only the first time signature is used; mid-piece meter changes are ignored
 */
object MusicXmlParser {

    // <part\s...> or <part>, but not <part-list>/<part-name>
    private val PART_START_REGEX = Regex("""<part(\s[^>]*)?>""")
    private val NOTE_BLOCK_REGEX = Regex("""<note\b[^>]*>(.*?)</note>""", RegexOption.DOT_MATCHES_ALL)
    private val REST_REGEX = Regex("""<rest\b""")
    private val CHORD_REGEX = Regex("""<chord\b""")
    private val GRACE_REGEX = Regex("""<grace\b""")
    private val TIE_STOP_REGEX = Regex("""<tie\b[^>]*type\s*=\s*["']stop["']""")
    private val VOICE_REGEX = Regex("""<voice>\s*(\d+)\s*</voice>""")
    private val DIVISIONS_REGEX = Regex("""<divisions>\s*(\d+)\s*</divisions>""")
    private val DURATION_REGEX = Regex("""<duration>\s*(\d+)\s*</duration>""")
    private val STEP_REGEX = Regex("""<step>([A-Ga-g])</step>""")
    private val ALTER_REGEX = Regex("""<alter>(-?\d+)</alter>""")
    private val OCTAVE_REGEX = Regex("""<octave>(\d+)</octave>""")
    private val TYPE_REGEX = Regex("""<type>(\w+)</type>""")
    private val TIME_REGEX = Regex(
        """<time(?:\s[^>]*)?>.*?<beats>\s*(\d+)\s*</beats>.*?<beat-type>\s*(\d+)\s*</beat-type>.*?</time>""",
        RegexOption.DOT_MATCHES_ALL,
    )

    fun parse(xmlContent: String, name: String = "MusicXML"): NoteSequence {
        val partContent = firstPartContent(xmlContent)
        val notes = mutableListOf<MusicalNote>()
        var firstVoice: String? = null

        // <divisions> (in <attributes>) defines how many duration units make a
        // quarter note; it can change mid-part, so track each occurrence's position
        val divisionsByOffset = DIVISIONS_REGEX.findAll(partContent)
            .mapNotNull { m -> m.groupValues[1].toIntOrNull()?.let { m.range.first to it } }
            .toList()

        NOTE_BLOCK_REGEX.findAll(partContent).forEach { noteMatch ->
            val noteBlock = noteMatch.groupValues[1]

            if (REST_REGEX.containsMatchIn(noteBlock)) return@forEach
            if (CHORD_REGEX.containsMatchIn(noteBlock)) return@forEach
            if (GRACE_REGEX.containsMatchIn(noteBlock)) return@forEach
            if (TIE_STOP_REGEX.containsMatchIn(noteBlock)) return@forEach

            val voice = VOICE_REGEX.find(noteBlock)?.groupValues?.get(1)
            if (voice != null) {
                if (firstVoice == null) firstVoice = voice
                else if (voice != firstVoice) return@forEach
            }

            val step = STEP_REGEX.find(noteBlock)?.groupValues?.get(1) ?: return@forEach
            val alter = ALTER_REGEX.find(noteBlock)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val octave = OCTAVE_REGEX.find(noteBlock)?.groupValues?.get(1)?.toIntOrNull() ?: 4

            val noteIndex = ItalianNotation.fromMusicXmlStep(step.uppercase(), alter) ?: return@forEach
            val type = TYPE_REGEX.find(noteBlock)?.groupValues?.get(1)
            val duration = when {
                type != null -> NoteDuration.fromMusicXml(type)
                else -> durationFromRatio(noteBlock, noteMatch.range.first, divisionsByOffset)
            }
            notes.add(MusicalNote.fromNameAndOctave(noteIndex, octave, duration, preferFlat = alter < 0))
        }

        val time = TIME_REGEX.find(partContent)
        val beats = time?.groupValues?.get(1)?.toIntOrNull() ?: 4
        val beatType = time?.groupValues?.get(2)?.toIntOrNull() ?: 4
        return NoteSequence(name = name, notes = notes, beats = beats, beatType = beatType)
    }

    /** Derives the note value from <duration>/<divisions> when <type> is absent. */
    private fun durationFromRatio(
        noteBlock: String,
        noteOffset: Int,
        divisionsByOffset: List<Pair<Int, Int>>,
    ): NoteDuration {
        val durationUnits = DURATION_REGEX.find(noteBlock)?.groupValues?.get(1)?.toIntOrNull()
            ?: return NoteDuration.QUARTER
        val divisions = divisionsByOffset.lastOrNull { it.first < noteOffset }?.second ?: 1
        if (divisions <= 0) return NoteDuration.QUARTER
        val quarters = durationUnits.toDouble() / divisions
        return when {
            quarters >= 4.0 -> NoteDuration.WHOLE
            quarters >= 2.0 -> NoteDuration.HALF
            quarters >= 1.0 -> NoteDuration.QUARTER
            quarters >= 0.5 -> NoteDuration.EIGHTH
            quarters >= 0.25 -> NoteDuration.SIXTEENTH
            else -> NoteDuration.THIRTY_SECOND
        }
    }

    /** Content of the first <part> element, or the whole document if none is found. */
    private fun firstPartContent(xml: String): String {
        val start = PART_START_REGEX.find(xml) ?: return xml
        val contentStart = start.range.last + 1
        val end = xml.indexOf("</part>", contentStart)
        return if (end >= 0) xml.substring(contentStart, end) else xml.substring(contentStart)
    }
}
