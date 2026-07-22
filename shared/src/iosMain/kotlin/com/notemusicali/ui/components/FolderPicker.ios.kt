package com.notemusicali.ui.components

import androidx.compose.runtime.Composable
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.base64EncodedStringWithOptions
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject

private var retainedDelegate: FolderPickerDelegate? = null

@Composable
actual fun rememberFolderPickerLauncher(
    onFolderSelected: (uri: String, displayName: String) -> Unit,
): () -> Unit {
    return {
        presentFolderPicker(onFolderSelected)
    }
}

private fun presentFolderPicker(onFolderSelected: (String, String) -> Unit) {
    @Suppress("DEPRECATION")
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val topVC = topViewController(rootVC)

    val delegate = FolderPickerDelegate(onFolderSelected)
    retainedDelegate = delegate

    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeFolder))
    picker.allowsMultipleSelection = false
    picker.delegate = delegate

    topVC.presentViewController(picker, animated = true, completion = null)
}

private fun topViewController(vc: UIViewController): UIViewController {
    vc.presentedViewController?.let { return topViewController(it) }
    return vc
}

private class FolderPickerDelegate(
    private val onFolderSelected: (String, String) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        retainedDelegate = null
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val accessing = url.startAccessingSecurityScopedResource()
        try {
            val displayName = url.lastPathComponent ?: "Folder"

            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                val bookmarkData = url.bookmarkDataWithOptions(
                    options = 0u,
                    includingResourceValuesForKeys = null,
                    relativeToURL = null,
                    error = errorPtr.ptr,
                )
                if (bookmarkData != null) {
                    val base64 = bookmarkData.base64EncodedStringWithOptions(0u)
                    onFolderSelected("bookmark:$base64", displayName)
                }
            }
        } finally {
            if (accessing) url.stopAccessingSecurityScopedResource()
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        retainedDelegate = null
    }
}
