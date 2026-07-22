package com.notemusicali.util

/**
 * Platform-specific crash handler for logging uncaught exceptions.
 */
expect object CrashHandler {
    fun install()
}
