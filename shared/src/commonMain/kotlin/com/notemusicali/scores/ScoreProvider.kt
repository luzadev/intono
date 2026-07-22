package com.notemusicali.scores

expect object ScoreProvider {
    fun listScores(relativePath: String = ""): List<ScoreEntry>
    fun readScoreBytes(path: String): ByteArray?
    fun listFromUri(treeUri: String, documentId: String = ""): List<ScoreEntry>
    fun readFromUri(treeUri: String, documentId: String): ByteArray?
}
