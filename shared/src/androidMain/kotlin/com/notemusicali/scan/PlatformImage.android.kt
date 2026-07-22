package com.notemusicali.scan

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

actual typealias PlatformImage = Bitmap

actual fun PlatformImage.toBase64Jpeg(maxDim: Int, quality: Int): String {
    val scaled = if (width > maxDim || height > maxDim) {
        val scale = maxDim.toFloat() / maxOf(width, height)
        Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true,
        )
    } else {
        this
    }

    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}
