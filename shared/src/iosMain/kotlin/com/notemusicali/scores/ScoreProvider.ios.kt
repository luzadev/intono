package com.notemusicali.scores

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual object ScoreProvider {

    private val allowedExtensions = listOf("mxl", "xml", "musicxml")

    actual fun listScores(relativePath: String): List<ScoreEntry> {
        return emptyList()
    }

    actual fun readScoreBytes(path: String): ByteArray? {
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun listFromUri(treeUri: String, documentId: String): List<ScoreEntry> {
        val rootUrl = resolveBookmark(treeUri) ?: return emptyList()
        val accessing = rootUrl.startAccessingSecurityScopedResource()
        try {
            val rootPath = rootUrl.path ?: return emptyList()
            val dirPath = if (documentId.isEmpty()) rootPath else "$rootPath/$documentId"

            val fm = NSFileManager.defaultManager
            val contents = fm.contentsOfDirectoryAtPath(dirPath, error = null) ?: return emptyList()

            val results = mutableListOf<ScoreEntry>()
            for (item in contents) {
                val name = item as? String ?: continue
                if (name.startsWith(".")) continue

                val fullPath = "$dirPath/$name"
                val isDir = isDirectoryAtPath(fullPath)
                val ext = name.substringAfterLast(".", "").lowercase()

                if (isDir || ext in allowedExtensions) {
                    val relativePath = if (documentId.isEmpty()) name else "$documentId/$name"
                    results.add(
                        ScoreEntry(
                            name = if (isDir) name else name.substringBeforeLast("."),
                            path = relativePath,
                            isDirectory = isDir,
                        )
                    )
                }
            }
            return results.sortedWith(
                compareBy<ScoreEntry> { !it.isDirectory }.thenBy { it.name.lowercase() }
            )
        } finally {
            if (accessing) rootUrl.stopAccessingSecurityScopedResource()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun readFromUri(treeUri: String, documentId: String): ByteArray? {
        val rootUrl = resolveBookmark(treeUri) ?: return null
        val accessing = rootUrl.startAccessingSecurityScopedResource()
        try {
            val rootPath = rootUrl.path ?: return null
            val filePath = "$rootPath/$documentId"
            val data = NSData.dataWithContentsOfFile(filePath) ?: return null
            return data.toByteArray()
        } finally {
            if (accessing) rootUrl.stopAccessingSecurityScopedResource()
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalEncodingApi::class)
    private fun resolveBookmark(treeUri: String): NSURL? {
        if (!treeUri.startsWith("bookmark:")) return null
        val base64 = treeUri.removePrefix("bookmark:")
        val bytes = try {
            Base64.decode(base64)
        } catch (_: Exception) {
            return null
        }
        val bookmarkData = bytes.toNSData()
        return try {
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                NSURL(
                    byResolvingBookmarkData = bookmarkData,
                    options = 0u,
                    relativeToURL = null,
                    bookmarkDataIsStale = null,
                    error = errorPtr.ptr,
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun isDirectoryAtPath(path: String): Boolean {
        val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
            ?: return false
        return attrs[NSFileType] == NSFileTypeDirectory
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        if (size == 0) return ByteArray(0)
        val src = this.bytes ?: return ByteArray(0)
        return ByteArray(size).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), src, this@toByteArray.length)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData()
        return usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
                ?: NSData()
        }
    }
}
