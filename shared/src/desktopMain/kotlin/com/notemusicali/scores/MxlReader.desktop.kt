package com.notemusicali.scores

import java.util.zip.ZipInputStream

actual fun extractMusicXmlFromBytes(bytes: ByteArray): String? {
    if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
        return try {
            ZipInputStream(bytes.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".xml") && !entry.name.startsWith("META-INF")) {
                        return@use zis.bufferedReader().readText()
                    }
                    entry = zis.nextEntry
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }
    return bytes.decodeToString()
}
