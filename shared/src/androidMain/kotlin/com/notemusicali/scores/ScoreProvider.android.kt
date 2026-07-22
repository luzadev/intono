package com.notemusicali.scores

import android.net.Uri
import android.provider.DocumentsContract

actual object ScoreProvider {
    actual fun listScores(relativePath: String): List<ScoreEntry> {
        val baseDir = ScoreProviderContext.scoresDir ?: return emptyList()
        val dir = if (relativePath.isEmpty()) baseDir
        else java.io.File(baseDir, relativePath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.filter { it.isDirectory || it.extension.lowercase() in listOf("mxl", "xml", "musicxml") }
            ?.sortedWith(compareBy<java.io.File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            ?.map { file ->
                ScoreEntry(
                    name = if (file.isDirectory) file.name else file.nameWithoutExtension,
                    path = if (relativePath.isEmpty()) file.name
                    else "$relativePath/${file.name}",
                    isDirectory = file.isDirectory,
                )
            }
            ?: emptyList()
    }

    actual fun readScoreBytes(path: String): ByteArray? {
        val baseDir = ScoreProviderContext.scoresDir ?: return null
        val file = java.io.File(baseDir, path)
        if (!file.exists()) return null
        return try {
            file.readBytes()
        } catch (_: Exception) {
            null
        }
    }

    actual fun listFromUri(treeUri: String, documentId: String): List<ScoreEntry> {
        val context = ScoreProviderContext.appContext ?: return emptyList()
        val treeUriParsed = Uri.parse(treeUri)
        val parentDocId = if (documentId.isEmpty()) {
            DocumentsContract.getTreeDocumentId(treeUriParsed)
        } else {
            documentId
        }
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUriParsed, parentDocId)
        val results = mutableListOf<ScoreEntry>()
        try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                ),
                null, null, null,
            )?.use { cursor ->
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol) ?: continue
                    val mime = cursor.getString(mimeCol) ?: ""
                    val docId = cursor.getString(idCol) ?: continue
                    val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR
                    val ext = name.substringAfterLast(".", "").lowercase()
                    if (isDir || ext in listOf("mxl", "xml", "musicxml")) {
                        results.add(
                            ScoreEntry(
                                name = if (isDir) name else name.substringBeforeLast("."),
                                path = docId,
                                isDirectory = isDir,
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        return results.sortedWith(compareBy<ScoreEntry> { !it.isDirectory }.thenBy { it.name.lowercase() })
    }

    actual fun readFromUri(treeUri: String, documentId: String): ByteArray? {
        val context = ScoreProviderContext.appContext ?: return null
        val treeUriParsed = Uri.parse(treeUri)
        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUriParsed, documentId)
        return try {
            context.contentResolver.openInputStream(docUri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
