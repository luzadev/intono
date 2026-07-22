package com.notemusicali.util

import platform.Foundation.NSLog

actual object CrashHandler {
    actual fun install() {
        // On iOS, Kotlin/Native uncaught exceptions terminate the process.
        // Log a message to help with post-crash diagnostics.
        NSLog("InTono: CrashHandler installed")
    }
}
