package com.notemusicali.util

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertTrue

class TimeProviderLocaleTest {

    @Test
    fun `currentDateString returns 8 ASCII digits regardless of default locale`() {
        val original = Locale.getDefault()
        try {
            // Arabic (Egypt) formats numbers with Eastern Arabic numerals by default
            Locale.setDefault(Locale("ar", "EG"))
            val date = currentDateString()
            assertTrue(
                date.length == 8 && date.all { it in '0'..'9' },
                "expected 8 ASCII digits, got \"$date\"",
            )
        } finally {
            Locale.setDefault(original)
        }
    }
}
