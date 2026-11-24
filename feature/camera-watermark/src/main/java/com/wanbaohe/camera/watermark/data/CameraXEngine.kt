/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Range
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.DynamicRange
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.ZoomState
import androidx.camera.effects.OverlayEffect
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.CameraCaptureMode
import com.wanbaohe.camera.watermark.domain.CameraFrameRate
import com.wanbaohe.camera.watermark.domain.CameraLens
import com.wanbaohe.camera.watermark.domain.CameraWatermarkMetadata
import com.wanbaohe.camera.watermark.domain.CameraWatermarkStyle
import com.wanbaohe.camera.watermark.domain.CreativeCameraCapabilities
import com.wanbaohe.camera.watermark.domain.CreativeCameraSettings
import com.wanbaohe.camera.watermark.domain.CreativeFilter
import com.wanbaohe.camera.watermark.domain.CreativeScene
import com.wanbaohe.camera.watermark.domain.CameraWhiteBalance
import com.wanbaohe.camera.watermark.domain.VideoQualityOption
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalCamera2Interop::class)
class CameraXEngine @Inject constructor(
    @ApplicationContext context: Context,
    private val overlayRenderer: WatermarkOverlayRenderer,
) {
    enum class Failure {
        INITIALIZATION,
        CAPTURE,
        RECORDING,
        EFFECT,
        SAVE,
    }

    interface Listener {
        fun onReady(capabilities: CreativeCameraCapabilities)
        fun onZoomChanged(ratio: Float)
        fun onFailure(failure: Failure)
        fun onPhotoCaptured(uri: Uri)
        fun onRecordingStarted()
        fun onRecordingPaused()
        fun onRecordingResumed()
        fun onRecordingProgress(durationMs: Long)
        fun onVideoSaved(uri: Uri)
        fun onHistogram(values: List<Int>)
    }

    private val appContext = context.applicationContext
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(appContext)
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private val overlayThread = HandlerThread("CameraWatermarkOverlay").apply { start() }
    private val overlayHandler = Handler(overlayThread.looper)
    private val providerFuture = ProcessCameraProvider.getInstance(appContext)
    private var provider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null
    private var settings = CreativeCameraSettings()
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var overlayEffect: OverlayEffect? = null
    private var zoomObserver: Observer<ZoomState>? = null
    private var location: Location? = null
    private var watermarkMetadata = CameraWatermarkMetadata()
    private var cameraParameters = ""
    private var lastAnalysisAt = 0L
    private var released = false
    var listener: Listener? = null

    private val deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
        .filter(String::isNotBlank)
        .joinToString(" ")
        .ifBlank { "Camera" }

    init {
        providerFuture.addListener(
            {
                runCatching { providerFuture.get() }
                    .onSuccess {
                        if (!released) {
                            provider = it
                            bindInternal()
                        }
                    }
                    .onFailure { listener?.onFailure(Failure.INITIALIZATION) }
            },
            mainExecutor,
        )
    }

    fun bind(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        settings: CreativeCameraSettings,
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.previewView = previewView
        this.settings = settings
        bindInternal()
    }

    fun updateSettings(settings: CreativeCameraSettings) {
        this.settings = settings
        imageCapture?.flashMode = settings.flashMode.cameraXValue
        val currentCamera = camera ?: return
        runCatching {
            currentCamera.cameraControl.enableTorch(settings.torchEnabled && settings.mode == CameraCaptureMode.VIDEO)
            currentCamera.cameraControl.setZoomRatio(
                currentCamera.cameraInfo.zoomState.value?.zoomRatio?.coerceIn(
                    currentCamera.cameraInfo.zoomState.value?.minZoomRatio ?: 1f,
                    currentCamera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f,
                ) ?: 1f,
            )
            currentCamera.cameraControl.enableLowLightBoostAsync(settings.lowLightBoostEnabled)
        }
        applyCamera2Options()
    }

    fun setExposureCompensation(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
    }

    fun setZoomRatio(ratio: Float) {
        val zoom = camera?.cameraInfo?.zoomState?.value ?: return
        camera?.cameraControl?.setZoomRatio(ratio.coerceIn(zoom.minZoomRatio, zoom.maxZoomRatio))
    }

    fun updateLocation(location: Location?) {
        this.location = location
        if (location == null) watermarkMetadata = CameraWatermarkMetadata()
    }

    fun updateWatermarkMetadata(metadata: CameraWatermarkMetadata) {
        watermarkMetadata = metadata
    }

    fun capturePhoto() {
        val capture = imageCapture ?: run {
            listener?.onFailure(Failure.CAPTURE)
            return
        }
        val directory = File(appContext.cacheDir, "camera-watermark/captures").apply { mkdirs() }
        val name = "OneBox_${timestampForFilename()}.jpg"
        val target = File(directory, name)
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = settings.lens == CameraLens.FRONT && settings.mirrorFrontCamera
            location = this@CameraXEngine.location.takeIf { settings.locationEnabled }
        }
        val output = ImageCapture.OutputFileOptions.Builder(target).setMetadata(metadata).build()
        capture.flashMode = settings.flashMode.cameraXValue
        capture.takePicture(
            output,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    ioExecutor.execute {
                        runCatching {
                            processPhotoWatermark(target)
                            writeExif(target)
                        }.onSuccess {
                            mainExecutor.execute { listener?.onPhotoCaptured(Uri.fromFile(target)) }
                        }.onFailure { error ->
                            Log.e("CameraWatermark", "Unable to process photo", error)
                            target.delete()
                            mainExecutor.execute { listener?.onFailure(Failure.CAPTURE) }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    target.delete()
                    listener?.onFailure(Failure.CAPTURE)
                }
            },
        )
    }

    fun savePhoto(uri: Uri, onSaved: (Uri) -> Unit) {
        ioExecutor.execute {
            runCatching {
                val source = uri.path?.let(::File) ?: error("Missing capture file")
                val name = "OneBox_${timestampForFilename()}.jpg"
                val values = mediaValues(name, "image/jpeg", isVideo = false)
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val outputUri = appContext.contentResolver.insert(collection, values)
                    ?: error("Cannot create image")
                try {
                    appContext.contentResolver.openOutputStream(outputUri)?.use { output ->
                        source.inputStream().use { input -> input.copyTo(output) }
                    } ?: error("Cannot open image")
                    publishPending(outputUri, isVideo = false)
                    source.delete()
                    mainExecutor.execute { onSaved(outputUri) }
                } catch (error: Throwable) {
                    appContext.contentResolver.delete(outputUri, null, null)
                    throw error
                }
            }.onFailure {
                mainExecutor.execute { listener?.onFailure(Failure.SAVE) }
            }
        }
    }

    fun discardPhoto(uri: Uri) {
        uri.path?.let(::File)?.delete()
    }

    fun startRecording(audioPermissionGranted: Boolean) {
        if (recording != null) {
            listener?.onFailure(Failure.RECORDING)
            return
        }
        val capture = videoCapture ?: run {
            listener?.onFailure(Failure.RECORDING)
            return
        }
        val name = "OneBox_${timestampForFilename()}.mp4"
        val values = mediaValues(name, "video/mp4", isVideo = true)
        val output = MediaStoreOutputOptions.Builder(
            appContext.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        ).setContentValues(values).build()
        val pending = capture.output.prepareRecording(appContext, output)
            .withAudioEnabled(settings.audioEnabled && audioPermissionGranted)
        recording = pending.start(mainExecutor) { event ->
            when (event) {
                is VideoRecordEvent.Start -> listener?.onRecordingStarted()
                is VideoRecordEvent.Pause -> listener?.onRecordingPaused()
                is VideoRecordEvent.Resume -> listener?.onRecordingResumed()
                is VideoRecordEvent.Status -> {
                    listener?.onRecordingProgress(event.recordingStats.recordedDurationNanos / 1_000_000L)
                }
                is VideoRecordEvent.Finalize -> {
                    recording = null
                    val outputUri = event.outputResults.outputUri
                    if (event.hasError()) {
                        outputUri?.let { appContext.contentResolver.delete(it, null, null) }
                        listener?.onFailure(Failure.RECORDING)
                    } else if (outputUri != null) {
                        publishPending(outputUri, isVideo = true)
                        listener?.onVideoSaved(outputUri)
                    }
                }
            }
        }
    }

    fun pauseRecording() {
        recording?.pause()
    }

    fun resumeRecording() {
        recording?.resume()
    }

    fun muteRecording(muted: Boolean) {
        recording?.mute(muted)
    }

    fun stopRecording() {
        recording?.stop()
    }

    fun unbind() {
        zoomObserver?.let { observer -> camera?.cameraInfo?.zoomState?.removeObserver(observer) }
        zoomObserver = null
        recording?.stop()
        recording = null
        runCatching { provider?.unbindAll() }
        camera = null
        imageCapture = null
        videoCapture = null
        overlayEffect?.close()
        overlayEffect = null
        lifecycleOwner = null
        previewView = null
    }

    fun release() {
        if (released) return
        released = true
        unbind()
        ioExecutor.shutdown()
        analysisExecutor.shutdown()
        overlayThread.quitSafely()
    }

    private fun bindInternal() {
        val cameraProvider = provider ?: return
        val owner = lifecycleOwner ?: return
        val view = previewView ?: return
        if (released || view.width == 0 || view.height == 0) return
        runCatching {
            zoomObserver?.let { observer -> camera?.cameraInfo?.zoomState?.removeObserver(observer) }
            cameraProvider.unbindAll()
            overlayEffect?.close()
            val rotation = view.display?.rotation ?: Surface.ROTATION_0
            val previewBuilder = Preview.Builder()
                .setTargetAspectRatio(settings.ratio.cameraXValue)
                .setTargetRotation(rotation)
            if (settings.frameRate != CameraFrameRate.AUTO) {
                previewBuilder.setTargetFrameRate(Range(settings.frameRate.fps, settings.frameRate.fps))
            }
            if (settings.videoStabilization && settings.mode == CameraCaptureMode.VIDEO) {
                previewBuilder.setPreviewStabilizationEnabled(true)
            }
            if (settings.hdrEnabled && settings.mode == CameraCaptureMode.VIDEO) {
                previewBuilder.setDynamicRange(DynamicRange.HDR_UNSPECIFIED_10_BIT)
            }
            val preview = previewBuilder.build().also {
                it.surfaceProvider = view.surfaceProvider
            }
            val groupBuilder = UseCaseGroup.Builder()
                .setViewPort(
                    ViewPort.Builder(Rational(view.width, view.height), rotation)
                        .setScaleType(ViewPort.FILL_CENTER)
                        .build(),
                )
                .addUseCase(preview)
            val stillCapture = ImageCapture.Builder()
                .setCaptureMode(settings.photoQuality.captureMode)
                .setFlashMode(settings.flashMode.cameraXValue)
                .setJpegQuality(96)
                .setTargetAspectRatio(settings.ratio.cameraXValue)
                .setTargetRotation(rotation)
                .build()
            imageCapture = stillCapture
            val quality = QualitySelector.fromOrderedList(
                listOf(
                    settings.videoQuality.quality,
                    VideoQualityOption.FHD.quality,
                    VideoQualityOption.HD.quality,
                    VideoQualityOption.SD.quality,
                ).distinct(),
                FallbackStrategy.higherQualityOrLowerThan(settings.videoQuality.quality),
            )
            val recorder = Recorder.Builder()
                .setQualitySelector(quality)
                .setAspectRatio(settings.ratio.cameraXValue)
                .build()
            val videoBuilder = VideoCapture.Builder(recorder)
                .setTargetRotation(rotation)
            if (settings.frameRate != CameraFrameRate.AUTO) {
                videoBuilder.setTargetFrameRate(Range(settings.frameRate.fps, settings.frameRate.fps))
            }
            if (settings.videoStabilization && settings.mode == CameraCaptureMode.VIDEO) {
                videoBuilder.setVideoStabilizationEnabled(true)
            }
            if (settings.hdrEnabled && settings.mode == CameraCaptureMode.VIDEO) {
                videoBuilder.setDynamicRange(DynamicRange.HDR_UNSPECIFIED_10_BIT)
            }
            val movieCapture = videoBuilder.build()
            videoCapture = movieCapture
            val effectTargets: Int
            if (settings.mode == CameraCaptureMode.PHOTO) {
                groupBuilder.addUseCase(stillCapture)
                effectTargets = CameraEffect.PREVIEW
            } else {
                groupBuilder.addUseCase(movieCapture)
                effectTargets = CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE
            }
            if (settings.histogramEnabled || settings.watermarkStyle == CameraWatermarkStyle.MINIMAL) {
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetAspectRatio(settings.ratio.cameraXValue)
                    .setTargetRotation(rotation)
                    .build()
                analysis.setAnalyzer(analysisExecutor, ::analyzeFrame)
                groupBuilder.addUseCase(analysis)
            }
            val effect = OverlayEffect(effectTargets, 0, overlayHandler) {
                listener?.onFailure(Failure.EFFECT)
            }
            effect.setOnDrawListener { frame ->
                overlayRenderer.draw(frame, createOverlayState())
            }
            overlayEffect = effect
            groupBuilder.addEffect(effect)
            val selector = when (settings.lens) {
                CameraLens.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                CameraLens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            }
            val boundCamera = cameraProvider.bindToLifecycle(owner, selector, groupBuilder.build())
            camera = boundCamera
            val observer = Observer<ZoomState> { state ->
                listener?.onZoomChanged(state?.zoomRatio ?: 1f)
            }
            zoomObserver = observer
            boundCamera.cameraInfo.zoomState.observe(owner, observer)
            installGestures(view)
            applyCamera2Options()
            listener?.onReady(readCapabilities(boundCamera, cameraProvider))
        }.onFailure {
            Log.e("CameraWatermark", "Unable to bind camera", it)
            listener?.onFailure(Failure.INITIALIZATION)
        }
    }

    private fun readCapabilities(
        boundCamera: Camera,
        cameraProvider: ProcessCameraProvider,
    ): CreativeCameraCapabilities {
        val info = boundCamera.cameraInfo
        val camera2Info = Camera2CameraInfo.from(info)
        val zoom = info.zoomState.value
        val exposure = info.exposureState
        val effects = camera2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS)
            ?.toSet().orEmpty()
        val scenes = camera2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
            ?.toSet().orEmpty()
        val whiteBalances = camera2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
            ?.toSet().orEmpty()
        val focalLengths = camera2Info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val apertures = camera2Info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        cameraParameters = buildList {
            apertures?.minOrNull()?.let { add("f/%.1f".format(Locale.US, it)) }
            focalLengths?.maxOrNull()?.let { add("%.0fmm".format(Locale.US, it)) }
        }.joinToString(" ")
        val qualities = QualitySelector.getSupportedQualities(info).toSet()
        val supportedHdr = runCatching {
            info.querySupportedDynamicRanges(setOf(DynamicRange.HDR_UNSPECIFIED_10_BIT)).isNotEmpty()
        }.getOrDefault(false)
        return CreativeCameraCapabilities(
            hasFlashUnit = info.hasFlashUnit(),
            hasFrontCamera = runCatching {
                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            }.getOrDefault(false),
            minZoomRatio = zoom?.minZoomRatio ?: 1f,
            maxZoomRatio = zoom?.maxZoomRatio ?: 1f,
            minExposureIndex = exposure.exposureCompensationRange.lower,
            maxExposureIndex = exposure.exposureCompensationRange.upper,
            exposureStep = exposure.exposureCompensationStep.toFloat(),
            supportedFilters = CreativeFilter.entries.filter { it.requestValue in effects }.toSet()
                .ifEmpty { setOf(CreativeFilter.ORIGINAL) },
            supportedScenes = CreativeScene.entries.filter { it.requestValue in scenes }.toSet()
                .plus(CreativeScene.AUTO),
            supportedWhiteBalances = CameraWhiteBalance.entries.filter { it.requestValue in whiteBalances }.toSet()
                .ifEmpty { setOf(CameraWhiteBalance.AUTO) },
            supportedVideoQualities = VideoQualityOption.entries.filter { it.quality in qualities }.toSet()
                .ifEmpty { setOf(VideoQualityOption.HD) },
            supportedFrameRates = buildSet {
                add(CameraFrameRate.AUTO)
                add(CameraFrameRate.FPS_30)
                if (info.supportedFrameRateRanges.any { it.upper >= 60 }) add(CameraFrameRate.FPS_60)
            },
            supportsVideoStabilization = runCatching {
                Recorder.getVideoCapabilities(info).isStabilizationSupported
            }.getOrDefault(false),
            supportsLowLightBoost = runCatching { info.isLowLightBoostSupported }.getOrDefault(false),
            supportsHdr = supportedHdr,
            supportsTorchStrength = runCatching { info.isTorchStrengthSupported }.getOrDefault(false),
        )
    }

    private fun applyCamera2Options() {
        val currentCamera = camera ?: return
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_EFFECT_MODE, settings.filter.requestValue)
            .setCaptureRequestOption(
                CaptureRequest.CONTROL_MODE,
                if (settings.scene == CreativeScene.AUTO) {
                    CaptureRequest.CONTROL_MODE_AUTO
                } else {
                    CaptureRequest.CONTROL_MODE_USE_SCENE_MODE
                },
            )
            .setCaptureRequestOption(CaptureRequest.CONTROL_SCENE_MODE, settings.scene.requestValue)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, settings.whiteBalance.requestValue)
            .build()
        runCatching {
            Camera2CameraControl.from(currentCamera.cameraControl).setCaptureRequestOptions(options)
        }
    }

    private fun installGestures(view: PreviewView) {
        val detector = ScaleGestureDetector(
            view.context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val current = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                    setZoomRatio(current * detector.scaleFactor)
                    return true
                }
            },
        )
        view.setOnTouchListener { touchedView, event ->
            detector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP && !detector.isInProgress) {
                val point = view.meteringPointFactory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(
                    point,
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
                ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                runCatching { camera?.cameraControl?.startFocusAndMetering(action) }
                touchedView.performClick()
            }
            true
        }
    }

    private fun analyzeFrame(image: androidx.camera.core.ImageProxy) {
        image.use { proxy ->
            val now = System.currentTimeMillis()
            if (now - lastAnalysisAt < 250L) return@use
            lastAnalysisAt = now
            val plane = proxy.planes.firstOrNull() ?: return@use
            val buffer = plane.buffer
            val top = sampleLuminance(
                buffer = buffer.duplicate(),
                rowStride = plane.rowStride,
                pixelStride = plane.pixelStride,
                width = proxy.width,
                height = proxy.height,
                startFraction = 0.04f,
                endFraction = 0.2f,
            )
            val bottom = sampleLuminance(
                buffer = buffer.duplicate(),
                rowStride = plane.rowStride,
                pixelStride = plane.pixelStride,
                width = proxy.width,
                height = proxy.height,
                startFraction = 0.58f,
                endFraction = 0.78f,
            )
            overlayRenderer.updateLuminance(top, bottom)
            if (!settings.histogramEnabled) return@use
            val histogramBuffer = buffer.duplicate().apply { rewind() }
            val bins = IntArray(32)
            var index = 0
            while (histogramBuffer.hasRemaining()) {
                val value = histogramBuffer.get().toInt() and 0xFF
                if (index++ % 8 == 0) bins[value * 31 / 255]++
            }
            mainExecutor.execute { listener?.onHistogram(bins.toList()) }
        }
    }

    private fun sampleLuminance(
        buffer: java.nio.ByteBuffer,
        rowStride: Int,
        pixelStride: Int,
        width: Int,
        height: Int,
        startFraction: Float,
        endFraction: Float,
    ): Float {
        val startY = (height * startFraction).toInt().coerceIn(0, height - 1)
        val endY = (height * endFraction).toInt().coerceIn(startY + 1, height)
        val stepY = ((endY - startY) / 8).coerceAtLeast(1)
        val stepX = (width / 12).coerceAtLeast(1)
        var sum = 0L
        var count = 0
        for (y in startY until endY step stepY) {
            for (x in 0 until width step stepX) {
                val index = y * rowStride + x * pixelStride
                if (index !in 0 until buffer.limit()) continue
                sum += buffer.get(index).toInt() and 0xFF
                count++
            }
        }
        return if (count == 0) 0.35f else sum.toFloat() / count / 255f
    }

    private fun createOverlayState(parameters: String = cameraParameters): WatermarkOverlayState {
        val currentLocation = location
        val coordinates = currentLocation?.let { value ->
            "%.2f°%s %.2f°%s".format(
                Locale.US,
                abs(value.latitude),
                if (value.latitude >= 0) "N" else "S",
                abs(value.longitude),
                if (value.longitude >= 0) "E" else "W",
            )
        }.orEmpty()
        val hasWeather = listOf(
            watermarkMetadata.weatherText,
            watermarkMetadata.temperatureCelsius,
            watermarkMetadata.windDirection,
            watermarkMetadata.humidity,
        ).any(String::isNotBlank)
        return WatermarkOverlayState(
            style = settings.watermarkStyle,
            deviceName = deviceName,
            cameraParameters = parameters,
            locationLabel = watermarkMetadata.placeLabel.ifBlank { coordinates },
            coordinatesLabel = coordinates,
            altitudeLabel = currentLocation
                ?.takeIf(Location::hasAltitude)
                ?.let { appContext.getString(R.string.camera_altitude_value, it.altitude.roundToInt()) }
                .orEmpty(),
            weatherLabel = if (hasWeather) {
                appContext.getString(
                    R.string.camera_weather_value,
                    watermarkMetadata.weatherText,
                    watermarkMetadata.temperatureCelsius,
                ).trim()
            } else {
                ""
            },
            weatherDetailsLabel = if (hasWeather) {
                appContext.getString(
                    R.string.camera_weather_details,
                    watermarkMetadata.windDirection.ifBlank { "--" },
                    watermarkMetadata.windScale.ifBlank { "--" },
                    watermarkMetadata.humidity.ifBlank { "--" },
                )
            } else {
                ""
            },
            showLocation = settings.locationEnabled,
            showAltitude = settings.altitudeEnabled,
            showWeather = settings.weatherEnabled,
            showTimestamp = settings.timestampEnabled,
            showCameraParameters = settings.cameraParametersEnabled,
            hideUnavailableInfo = settings.hideUnavailableWatermarkInfo,
        )
    }

    private fun processPhotoWatermark(file: File) {
        if (settings.watermarkStyle == CameraWatermarkStyle.NONE) return
        val source = BitmapFactory.decodeFile(file.absolutePath) ?: error("Cannot decode capture")
        val exif = ExifInterface(file.absolutePath)
        val matrix = Matrix().apply {
            if (exif.rotationDegrees != 0) postRotate(exif.rotationDegrees.toFloat())
            if (exif.isFlipped) postScale(-1f, 1f)
        }
        val oriented = if (matrix.isIdentity) {
            source
        } else {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
        val working = oriented.copy(Bitmap.Config.ARGB_8888, true)
        overlayRenderer.drawPhoto(
            canvas = Canvas(working),
            bitmap = working,
            state = createOverlayState(photoCameraParameters(exif)),
        )
        FileOutputStream(file).use { output ->
            if (!working.compress(Bitmap.CompressFormat.JPEG, 96, output)) {
                error("Cannot encode capture")
            }
        }
        if (working !== oriented) working.recycle()
        if (oriented !== source) oriented.recycle()
        source.recycle()
    }

    private fun photoCameraParameters(exif: ExifInterface): String = buildList {
        if (cameraParameters.isNotBlank()) add(cameraParameters)
        exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
            ?.trim()?.takeIf(String::isNotEmpty)?.let { add("ISO$it") }
        val exposure = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, -1.0)
        if (exposure > 0.0) {
            add(
                if (exposure >= 1.0) {
                    "%.1fs".format(Locale.US, exposure)
                } else {
                    "1/%.0fs".format(Locale.US, 1.0 / exposure)
                },
            )
        }
    }.joinToString(" ")

    private fun writeExif(file: File) {
        runCatching {
            val exif = ExifInterface(file.absolutePath)
            val now = Date()
            val value = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(now)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, value)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, value)
            exif.setAttribute(ExifInterface.TAG_DATETIME, value)
            exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER)
            exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "OneBox")
            exif.setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL.toString(),
            )
            if (settings.locationEnabled) location?.let(exif::setGpsInfo)
            exif.saveAttributes()
        }
    }

    private fun mediaValues(name: String, mimeType: String, isVideo: Boolean): ContentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val directory = if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_DCIM
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/OneBox")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            val directory = if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_DCIM
            val parent = File(Environment.getExternalStoragePublicDirectory(directory), "OneBox").apply { mkdirs() }
            put(MediaStore.MediaColumns.DATA, File(parent, name).absolutePath)
        }
    }

    private fun publishPending(uri: Uri, isVideo: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            appContext.contentResolver.update(uri, values, null, null)
        }
    }

    private fun timestampForFilename(): String =
        SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.US).format(Date())
}
