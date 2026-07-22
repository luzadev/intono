package com.notemusicali.util

actual object CrashHandler {
    actual fun install() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            System.err.println("Uncaught exception in ${thread.name}: ${throwable.message}")
            throwable.printStackTrace()
        }
    }
}
