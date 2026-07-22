package com.notemusicali.scores

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.z_stream
import platform.zlib.zlibVersion

actual fun extractMusicXmlFromBytes(bytes: ByteArray): String? {
    if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()) {
        return try {
            extractFromZip(bytes)
        } catch (_: Throwable) {
            null
        }
    }
    return bytes.decodeToString()
}

private fun readU16(b: ByteArray, off: Int): Int =
    (b[off].toInt() and 0xFF) or ((b[off + 1].toInt() and 0xFF) shl 8)

private fun readU32(b: ByteArray, off: Int): Long =
    (b[off].toLong() and 0xFF) or
        ((b[off + 1].toLong() and 0xFF) shl 8) or
        ((b[off + 2].toLong() and 0xFF) shl 16) or
        ((b[off + 3].toLong() and 0xFF) shl 24)

private const val EOCD_SIGNATURE = 0x06054b50L
private const val CENTRAL_DIR_SIGNATURE = 0x02014b50L
private const val EOCD_MIN_SIZE = 22
private const val LOCAL_HEADER_SIZE = 30
private const val METHOD_STORED = 0
private const val METHOD_DEFLATED = 8

/**
 * Minimal ZIP reader driven by the central directory: returns the content of the
 * first .xml entry outside META-INF, mirroring the JVM implementations.
 */
private fun extractFromZip(bytes: ByteArray): String? {
    // The End Of Central Directory record sits at the end of the file, possibly
    // followed by a comment of up to 65535 bytes.
    var eocd = -1
    var i = bytes.size - EOCD_MIN_SIZE
    val stop = maxOf(0, bytes.size - EOCD_MIN_SIZE - 0xFFFF)
    while (i >= stop) {
        if (readU32(bytes, i) == EOCD_SIGNATURE) {
            eocd = i
            break
        }
        i--
    }
    if (eocd < 0) return null

    val entryCount = readU16(bytes, eocd + 10)
    var offset = readU32(bytes, eocd + 16).toInt()

    repeat(entryCount) {
        if (offset + 46 > bytes.size || readU32(bytes, offset) != CENTRAL_DIR_SIGNATURE) return null
        val method = readU16(bytes, offset + 10)
        val compressedSize = readU32(bytes, offset + 20).toInt()
        val uncompressedSize = readU32(bytes, offset + 24).toInt()
        val nameLen = readU16(bytes, offset + 28)
        val extraLen = readU16(bytes, offset + 30)
        val commentLen = readU16(bytes, offset + 32)
        val localHeaderOffset = readU32(bytes, offset + 42).toInt()
        val name = bytes.decodeToString(offset + 46, offset + 46 + nameLen)
        offset += 46 + nameLen + extraLen + commentLen

        if (name.endsWith(".xml") && !name.startsWith("META-INF")) {
            // Name/extra field lengths in the local header can differ from the
            // central directory ones, so the data offset must be re-derived here.
            val lhNameLen = readU16(bytes, localHeaderOffset + 26)
            val lhExtraLen = readU16(bytes, localHeaderOffset + 28)
            val dataStart = localHeaderOffset + LOCAL_HEADER_SIZE + lhNameLen + lhExtraLen
            if (dataStart + compressedSize > bytes.size) return null
            val data = bytes.copyOfRange(dataStart, dataStart + compressedSize)
            return when (method) {
                METHOD_STORED -> data.decodeToString()
                METHOD_DEFLATED -> inflateRaw(data, uncompressedSize)?.decodeToString()
                else -> null
            }
        }
    }
    return null
}

@OptIn(ExperimentalForeignApi::class)
private fun inflateRaw(compressed: ByteArray, uncompressedSize: Int): ByteArray? {
    if (uncompressedSize <= 0 || compressed.isEmpty()) return null
    val output = ByteArray(uncompressedSize)
    return memScoped {
        val stream = alloc<z_stream>()
        // Negative windowBits selects raw deflate (no zlib header), as used by ZIP
        val init = inflateInit2_(stream.ptr, -15, zlibVersion()?.toKString(), sizeOf<z_stream>().toInt())
        if (init != Z_OK) return@memScoped null
        try {
            compressed.usePinned { inPin ->
                output.usePinned { outPin ->
                    stream.next_in = inPin.addressOf(0).reinterpret()
                    stream.avail_in = compressed.size.toUInt()
                    stream.next_out = outPin.addressOf(0).reinterpret()
                    stream.avail_out = uncompressedSize.toUInt()
                    if (inflate(stream.ptr, Z_FINISH) != Z_STREAM_END) return@memScoped null
                }
            }
            output
        } finally {
            inflateEnd(stream.ptr)
        }
    }
}
