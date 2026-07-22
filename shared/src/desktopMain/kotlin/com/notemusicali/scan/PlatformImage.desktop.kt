package com.notemusicali.scan

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual typealias PlatformImage = BufferedImage

actual fun PlatformImage.toBase64Jpeg(maxDim: Int, quality: Int): String {
    val scaled = if (width > maxDim || height > maxDim) {
        val scale = maxDim.toDouble() / maxOf(width, height)
        val newW = (width * scale).toInt()
        val newH = (height * scale).toInt()
        val img = BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.drawImage(this, 0, 0, newW, newH, null)
        g.dispose()
        img
    } else {
        this
    }

    val stream = ByteArrayOutputStream()
    ImageIO.write(scaled, "JPEG", stream)
    return java.util.Base64.getEncoder().encodeToString(stream.toByteArray())
}
