package com.notemusicali.scores

actual object ScoreProvider {
    private val scoresDir: java.io.File? by lazy {
        val userHome = System.getProperty("user.home")
        val dir = java.io.File(userHome, "NoteMusicali/scores")
        if (dir.exists()) dir else null
    }

    actual fun listScores(relativePath: String): List<ScoreEntry> {
        val baseDir = scoresDir ?: return emptyList()
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
        val baseDir = scoresDir ?: return null
        val file = java.io.File(baseDir, path)
        if (!file.exists()) return null
        return try {
            file.readBytes()
        } catch (_: Exception) {
            null
        }
    }

    actual fun listFromUri(treeUri: String, documentId: String): List<ScoreEntry> {
        val baseDir = java.io.File(treeUri)
        val dir = if (documentId.isEmpty()) baseDir else java.io.File(baseDir, documentId)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.filter { it.isDirectory || it.extension.lowercase() in listOf("mxl", "xml", "musicxml") }
            ?.sortedWith(compareBy<java.io.File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            ?.map { file ->
                ScoreEntry(
                    name = if (file.isDirectory) file.name else file.nameWithoutExtension,
                    path = if (documentId.isEmpty()) file.name else "$documentId/${file.name}",
                    isDirectory = file.isDirectory,
                )
            }
            ?: emptyList()
    }

    actual fun readFromUri(treeUri: String, documentId: String): ByteArray? {
        val file = java.io.File(treeUri, documentId)
        if (!file.exists()) return null
        return try { file.readBytes() } catch (_: Exception) { null }
    }
}
