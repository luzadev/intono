package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeXML
import platform.darwin.NSObject
import platform.posix.memcpy
import com.notemusicali.scores.extractMusicXmlFromBytes

private var retainedFileDelegate: FilePickerDelegate? = null

@Composable
actual fun rememberFilePickerLauncher(
    mimeTypes: List<String>,
    onFileContent: (fileName: String, content: String) -> Unit,
): () -> Unit {
    return {
        presentFilePicker(onFileContent)
    }
}

private fun presentFilePicker(onFileContent: (fileName: String, content: String) -> Unit) {
    @Suppress("DEPRECATION")
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val topVC = topPresentedViewController(rootVC)

    val delegate = FilePickerDelegate(onFileContent)
    retainedFileDelegate = delegate

    val contentTypes = listOfNotNull(
        UTTypeXML,
        UTType.typeWithFilenameExtension("mxl"),
        UTType.typeWithFilenameExtension("musicxml"),
    )

    val picker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)
    picker.allowsMultipleSelection = false
    picker.delegate = delegate

    topVC.presentViewController(picker, animated = true, completion = null)
}

private fun topPresentedViewController(vc: UIViewController): UIViewController {
    vc.presentedViewController?.let { return topPresentedViewController(it) }
    return vc
}

private class FilePickerDelegate(
    private val onFileContent: (fileName: String, content: String) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        retainedFileDelegate = null
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val accessing = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url) ?: return
            val bytes = data.toByteArray()
            val content = extractMusicXmlFromBytes(bytes) ?: return
            onFileContent(url.lastPathComponent ?: "Import", content)
        } finally {
            if (accessing) url.stopAccessingSecurityScopedResource()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        retainedFileDelegate = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        if (size == 0) return ByteArray(0)
        val src = this.bytes ?: return ByteArray(0)
        return ByteArray(size).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), src, this@toByteArray.length)
            }
        }
    }
}
