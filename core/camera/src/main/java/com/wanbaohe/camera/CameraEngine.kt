package com.wanbaohe.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executor

internal class CameraEngine(context: Context) {

    private val executor: Executor = ContextCompat.getMainExecutor(context)

    val controller = LifecycleCameraController(context.applicationContext).apply {
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        imageCaptureFlashMode = ImageCapture.FLASH_MODE_OFF
        isPinchToZoomEnabled = true
        isTapToFocusEnabled = true
    }

    fun bind(lifecycleOwner: LifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
    }

    fun unbind() {
        controller.unbind()
    }

    fun setFlashMode(mode: Int) {
        controller.imageCaptureFlashMode = mode
    }

    fun selectCamera(useFrontCamera: Boolean): Boolean {
        val selector = if (useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        if (!runCatching { controller.hasCamera(selector) }.getOrDefault(false)) return false
        controller.cameraSelector = selector
        return true
    }

    fun capture(
        target: File,
        useFrontCamera: Boolean,
        onSaved: () -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = useFrontCamera
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(target)
            .setMetadata(metadata)
            .build()
        controller.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSaved()
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}

@Composable
internal fun CameraPreview(
    engine: CameraEngine,
    aspectRatio: CameraAspectRatio,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(engine, lifecycleOwner) {
        runCatching { engine.bind(lifecycleOwner) }.onFailure(onError)
        onDispose { engine.unbind() }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            androidx.camera.view.PreviewView(context).apply {
                controller = engine.controller
            }
        },
        update = { previewView ->
            previewView.scaleType = if (aspectRatio == CameraAspectRatio.ORIGINAL) {
                androidx.camera.view.PreviewView.ScaleType.FIT_CENTER
            } else {
                androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            }
        }
    )
}
