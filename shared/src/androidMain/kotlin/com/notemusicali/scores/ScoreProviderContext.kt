package com.notemusicali.scores

import android.content.Context
import java.io.File

object ScoreProviderContext {
    var scoresDir: File? = null
    var appContext: Context? = null

    fun init(context: Context, externalFilesDir: File?) {
        appContext = context.applicationContext
        scoresDir = externalFilesDir?.let { File(it, "scores") }
    }
}
