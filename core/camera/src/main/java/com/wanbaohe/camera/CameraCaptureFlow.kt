/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.camera

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.wanbaohe.camera.R
import kotlinx.coroutines.launch

private enum class CameraStage {
    CAPTURE,
    CONFIRM,
    EDIT
}

@Composable
internal fun CameraCaptureFlow(
    request: CameraCaptureRequest,
    processor: PhotoFileProcessor,
    onCancel: () -> Unit,
    onComplete: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { CameraEngine(context) }

    var stage by rememberSaveable { mutableStateOf(CameraStage.CAPTURE) }
    var captureRatio by rememberSaveable { mutableStateOf(request.initialAspectRatio) }
    var editRatio by rememberSaveable { mutableStateOf(CameraAspectRatio.FREE) }
    var originalUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var workingUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var useFrontCamera by rememberSaveable { mutableStateOf(false) }
    var flashMode by rememberSaveable { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var showGrid by rememberSaveable { mutableStateOf(true) }
    var isCapturing by rememberSaveable { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val originalUri = originalUriString?.let(Uri::parse)
    val workingUri = workingUriString?.let(Uri::parse)
    val isBusy = isCapturing || isProcessing || isLoading

    fun showError(messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }

    fun retake() {
        if (isProcessing) return
        stage = CameraStage.CAPTURE
    }

    fun confirmPhoto() {
        val source = workingUri ?: return
        if (isProcessing) return
        scope.launch {
            isProcessing = true
            try {
                processor.export(source, request.outputUri, captureRatio, request.jpegQuality)
                onComplete(request.outputUri)
            } catch (_: Throwable) {
                showError(R.string.camera_image_failed)
            } finally {
                isProcessing = false
            }
        }
    }

    fun transformPhoto(rotation: Int = 0, flip: Boolean = false) {
        val source = workingUri ?: return
        if (isProcessing) return
        scope.launch {
            isProcessing = true
            try {
                val transformed = processor.transform(source, rotation, flip)
                workingUriString = transformed.toString()
            } catch (_: Throwable) {
                showError(R.string.camera_image_failed)
            } finally {
                isProcessing = false
            }
        }
    }

    fun exportCropped(cropped: Bitmap) {
        scope.launch {
            isProcessing = true
            try {
                processor.export(cropped, request.outputUri, request.jpegQuality)
                onComplete(request.outputUri)
            } catch (_: Throwable) {
                showError(R.string.camera_image_failed)
            } finally {
                isProcessing = false
            }
        }
    }

    fun capturePhoto() {
        if (isCapturing || isProcessing) return
        isCapturing = true
        val target = processor.createCaptureFile()
        engine.capture(
            target = target,
            useFrontCamera = useFrontCamera,
            onSaved = {
                val capturedUri = Uri.fromFile(target)
                processor.discard(originalUri, workingUri)
                originalUriString = capturedUri.toString()
                workingUriString = capturedUri.toString()
                editRatio = if (captureRatio == CameraAspectRatio.ORIGINAL || captureRatio == CameraAspectRatio.FULL_SCREEN) {
                    CameraAspectRatio.FREE
                } else {
                    captureRatio
                }
                isCapturing = false
                stage = if (request.editingEnabled) CameraStage.EDIT else CameraStage.CONFIRM
            },
            onError = {
                target.delete()
                isCapturing = false
                showError(R.string.camera_capture_failed)
            }
        )
    }

    LaunchedEffect(Unit) {
        processor.restoreSession(originalUri, workingUri)
        if (useFrontCamera && !engine.selectCamera(true)) {
            useFrontCamera = false
        }
    }

    LaunchedEffect(flashMode) {
        engine.setFlashMode(flashMode)
    }

    LaunchedEffect(workingUriString) {
        val source = workingUri
        if (source == null) {
            bitmap = null
        } else {
            isLoading = true
            try {
                bitmap = processor.loadBitmap(source)
            } catch (_: Throwable) {
                showError(R.string.camera_image_failed)
            } finally {
                isLoading = false
            }
        }
    }

    BackHandler {
        if (isProcessing) return@BackHandler
        when (stage) {
            CameraStage.CAPTURE -> onCancel()
            CameraStage.CONFIRM,
            CameraStage.EDIT -> retake()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (stage) {
            CameraStage.CAPTURE -> CameraCaptureScreen(
                engine = engine,
                aspectRatio = captureRatio,
                flashMode = flashMode,
                showGrid = showGrid,
                isCapturing = isCapturing,
                thumbnail = bitmap,
                onAspectRatioChange = { captureRatio = it },
                onFlashModeChange = { flashMode = it },
                onShowGridChange = { showGrid = it },
                onSwitchCamera = {
                    val targetFront = !useFrontCamera
                    if (engine.selectCamera(targetFront)) {
                        useFrontCamera = targetFront
                    } else {
                        showError(R.string.camera_other_unavailable)
                    }
                },
                onCapture = ::capturePhoto,
                onThumbnailClick = {
                    if (workingUri != null) {
                        stage = if (request.editingEnabled) CameraStage.EDIT else CameraStage.CONFIRM
                    }
                },
                onBack = onCancel,
                onError = { showError(R.string.camera_unavailable) }
            )

            CameraStage.CONFIRM -> bitmap?.let { photo ->
                PhotoConfirmScreen(
                    bitmap = photo,
                    aspectRatio = captureRatio,
                    editingEnabled = request.editingEnabled,
                    isProcessing = isProcessing,
                    onRetake = ::retake,
                    onEdit = { stage = CameraStage.EDIT },
                    onConfirm = ::confirmPhoto,
                    onBack = ::retake
                )
            }

            CameraStage.EDIT -> bitmap?.let { photo ->
                PhotoEditScreen(
                    bitmap = photo,
                    aspectRatio = editRatio,
                    isProcessing = isProcessing,
                    onAspectRatioChange = { editRatio = it },
                    onRotate = { transformPhoto(rotation = 90) },
                    onFlip = { transformPhoto(flip = true) },
                    onReset = {
                        val original = originalUri
                        val working = workingUri
                        if (original != null && working != null) {
                            processor.discardWorkingCopy(original, working)
                            workingUriString = original.toString()
                            editRatio = if (captureRatio == CameraAspectRatio.ORIGINAL || captureRatio == CameraAspectRatio.FULL_SCREEN) {
                                CameraAspectRatio.FREE
                            } else {
                                captureRatio
                            }
                        }
                    },
                    onCropStart = { isProcessing = true },
                    onCropped = ::exportCropped,
                    onBack = ::retake
                )
            }
        }

        if ((isLoading || isProcessing) && stage != CameraStage.CAPTURE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer)
            }
        }
    }
}
