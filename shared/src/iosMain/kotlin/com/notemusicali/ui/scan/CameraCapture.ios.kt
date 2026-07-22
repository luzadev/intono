package com.notemusicali.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notemusicali.scan.PlatformImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

// The picker does not retain its delegate; keep a strong reference until dismissal
private var retainedCameraDelegate: CameraPickerDelegate? = null

@Composable
actual fun CameraCapture(
    onImageCaptured: (PlatformImage) -> Unit,
    modifier: Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Fotografa lo spartito oppure scegli un'immagine dalla libreria",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = { presentImagePicker(preferCamera = true, onImageCaptured) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1A0533),
                ),
            ) {
                Text("Scatta foto")
            }
            TextButton(onClick = { presentImagePicker(preferCamera = false, onImageCaptured) }) {
                Text("Scegli dalla libreria", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

private fun presentImagePicker(preferCamera: Boolean, onImageCaptured: (PlatformImage) -> Unit) {
    @Suppress("DEPRECATION")
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val topVC = topPresentedViewController(rootVC)

    val delegate = CameraPickerDelegate(onImageCaptured)
    retainedCameraDelegate = delegate

    val cameraAvailable = UIImagePickerController.isSourceTypeAvailable(
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
    )
    val picker = UIImagePickerController()
    picker.sourceType = if (preferCamera && cameraAvailable) {
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    } else {
        // Simulator and camera-less devices fall back to the photo library
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
    }
    picker.delegate = delegate

    topVC.presentViewController(picker, animated = true, completion = null)
}

private fun topPresentedViewController(vc: UIViewController): UIViewController {
    vc.presentedViewController?.let { return topPresentedViewController(it) }
    return vc
}

private class CameraPickerDelegate(
    private val onImageCaptured: (PlatformImage) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        retainedCameraDelegate = null
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: return
        // The OMR providers resize to ~1024px anyway; cap here to keep the base64 payload small
        val jpeg = UIImageJPEGRepresentation(image.scaledDownTo(1600.0), 0.85) ?: return
        onImageCaptured(PlatformImage(jpeg.toByteArray()))
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        retainedCameraDelegate = null
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.scaledDownTo(maxDim: Double): UIImage {
    val width = size.useContents { width }
    val height = size.useContents { height }
    val largest = maxOf(width, height)
    if (largest <= maxDim || largest <= 0.0) return this
    val scale = maxDim / largest
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(width * scale, height * scale), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, width * scale, height * scale))
    val result = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return result ?: this
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val src = bytes ?: return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), src, this@toByteArray.length)
        }
    }
}
