package com.notemusicali.scan

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * On iOS, PlatformImage wraps raw JPEG byte data.
 * Full UIImage integration deferred to post-migration.
 */
actual class PlatformImage(val jpegData: ByteArray)

@OptIn(ExperimentalEncodingApi::class)
actual fun PlatformImage.toBase64Jpeg(maxDim: Int, quality: Int): String {
    return Base64.encode(jpegData)
}
