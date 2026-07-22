package com.notemusicali.scan

/**
 * Platform-agnostic wrapper for image data.
 */
expect class PlatformImage

/**
 * Encode a platform image to Base64 JPEG string.
 */
expect fun PlatformImage.toBase64Jpeg(maxDim: Int = 1024, quality: Int = 85): String
