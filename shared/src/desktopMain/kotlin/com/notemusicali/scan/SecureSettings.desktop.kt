package com.notemusicali.scan

import com.russhwolf.settings.Settings

// No OS-agnostic secure store on desktop: keys stay in the obfuscated store
internal actual fun createSecureSettings(): Settings? = null
