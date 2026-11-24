/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.presentation.screenLogic

import android.location.Location
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.data.CameraXEngine
import com.wanbaohe.camera.watermark.domain.CameraCaptureMode
import com.wanbaohe.camera.watermark.domain.CameraFlashMode
import com.wanbaohe.camera.watermark.domain.CameraGrid
import com.wanbaohe.camera.watermark.domain.CameraLens
import com.wanbaohe.camera.watermark.domain.CameraRatio
import com.wanbaohe.camera.watermark.domain.CameraTimer
import com.wanbaohe.camera.watermark.domain.CameraWatermarkMetadata
import com.wanbaohe.camera.watermark.domain.CameraWatermarkPreferences
import com.wanbaohe.camera.watermark.domain.CameraWatermarkSavedState
import com.wanbaohe.camera.watermark.domain.CreativeCameraSettings
import com.wanbaohe.camera.watermark.domain.CreativeCameraUiState
import com.wanbaohe.camera.watermark.domain.CreativeFilter
import com.wanbaohe.camera.watermark.domain.CreativeScene
import com.wanbaohe.camera.watermark.domain.GalleryLauncher
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.state.update
import com.wanbaohe.core.weather.domain.repository.CurrentLocationProvider
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraWatermarkComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    val cameraEngine: CameraXEngine,
    private val currentLocationProvider: CurrentLocationProvider,
    private val weatherRepository: WeatherRepository,
    private val preferences: CameraWatermarkPreferences,
    private val galleryLauncher: GalleryLauncher,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext), CameraXEngine.Listener {
    private val _state = mutableStateOf(CreativeCameraUiState())
    val state: CreativeCameraUiState by _state
    private var shutterJob: Job? = null
    private var locationJob: Job? = null
    private var persistJob: Job? = null
    private var preferencesLoaded = false

    init {
        cameraEngine.listener = this
        componentScope.launch {
            val saved = withContext(ioDispatcher) { preferences.load() }
            _state.update {
                it.copy(
                    settings = saved.settings,
                    exposureIndex = saved.exposureIndex,
                    zoomRatio = saved.zoomRatio,
                    lastCaptureUri = saved.lastCaptureUri.takeIf(String::isNotBlank)?.let(android.net.Uri::parse),
                    settingsTabName = saved.settingsTabName,
                )
            }
            preferencesLoaded = true
            cameraEngine.updateSettings(saved.settings)
        }
        componentContext.lifecycle.doOnDestroy {
            shutterJob?.cancel()
            locationJob?.cancel()
            cameraEngine.release()
        }
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _state.update { it.copy(isCameraReady = false) }
        cameraEngine.bind(lifecycleOwner, previewView, state.settings)
    }

    fun unbindCamera() {
        cameraEngine.unbind()
        _state.update { it.copy(isCameraReady = false) }
    }

    fun updateSettings(transform: (CreativeCameraSettings) -> CreativeCameraSettings) {
        val updated = transform(state.settings)
        _state.update { it.copy(settings = updated, messageRes = null) }
        cameraEngine.updateSettings(updated)
        persistState()
    }

    fun selectSettingsTab(name: String) {
        _state.update { it.copy(settingsTabName = name) }
        persistState()
    }

    fun openGallery() {
        if (!galleryLauncher.open(state.lastCaptureUri)) {
            _state.update { it.copy(messageRes = R.string.camera_gallery_open_failed) }
        }
    }

    private fun persistState() {
        if (!preferencesLoaded) return
        val current = state
        val saved = CameraWatermarkSavedState(
            settings = current.settings,
            exposureIndex = current.exposureIndex,
            zoomRatio = current.zoomRatio,
            lastCaptureUri = current.lastCaptureUri?.toString().orEmpty(),
            settingsTabName = current.settingsTabName,
        )
        persistJob?.cancel()
        persistJob = componentScope.launch {
            delay(150)
            withContext(ioDispatcher) { preferences.save(saved) }
        }
    }

    fun selectMode(mode: CameraCaptureMode) {
        if (state.isRecording) return
        updateSettings { it.copy(mode = mode, torchEnabled = false, hdrEnabled = false) }
    }

    fun switchCamera() {
        if (!state.capabilities.hasFrontCamera || state.isRecording) return
        updateSettings {
            it.copy(
                lens = if (it.lens == CameraLens.BACK) CameraLens.FRONT else CameraLens.BACK,
                flashMode = CameraFlashMode.OFF,
                torchEnabled = false,
            )
        }
        _state.update { it.copy(zoomRatio = 1f, exposureIndex = 0) }
        persistState()
    }

    fun cycleFlashMode() {
        if (!state.capabilities.hasFlashUnit) return
        updateSettings {
            it.copy(
                flashMode = when (it.flashMode) {
                    CameraFlashMode.OFF -> CameraFlashMode.AUTO
                    CameraFlashMode.AUTO -> CameraFlashMode.ON
                    CameraFlashMode.ON -> CameraFlashMode.OFF
                },
            )
        }
    }

    fun cycleRatio() {
        val values = CameraRatio.entries
        updateSettings { it.copy(ratio = values[(values.indexOf(it.ratio) + 1) % values.size]) }
    }

    fun cycleTimer() {
        val values = CameraTimer.entries
        updateSettings { it.copy(timer = values[(values.indexOf(it.timer) + 1) % values.size]) }
    }

    fun cycleGrid() {
        val values = CameraGrid.entries
        updateSettings { it.copy(grid = values[(values.indexOf(it.grid) + 1) % values.size]) }
    }

    fun selectFilter(filter: CreativeFilter) {
        updateSettings { it.copy(filter = filter, scene = CreativeScene.AUTO) }
    }

    fun selectScene(scene: CreativeScene) {
        updateSettings { it.copy(scene = scene, filter = CreativeFilter.ORIGINAL) }
    }

    fun setExposureCompensation(index: Int) {
        val value = index.coerceIn(
            state.capabilities.minExposureIndex,
            state.capabilities.maxExposureIndex,
        )
        _state.update { it.copy(exposureIndex = value) }
        cameraEngine.setExposureCompensation(value)
        persistState()
    }

    fun setZoomRatio(ratio: Float) {
        _state.update { it.copy(zoomRatio = ratio) }
        cameraEngine.setZoomRatio(ratio)
        persistState()
    }

    fun toggleLocation(enabled: Boolean) {
        updateSettings { it.copy(locationEnabled = enabled) }
        when {
            enabled -> refreshLocation()
            !state.settings.altitudeEnabled && !state.settings.weatherEnabled -> {
                locationJob?.cancel()
                cameraEngine.updateLocation(null)
                cameraEngine.updateWatermarkMetadata(CameraWatermarkMetadata())
            }
        }
    }

    fun refreshLocation() {
        locationJob?.cancel()
        locationJob = componentScope.launch {
            val snapshot = withContext(ioDispatcher) {
                runCatching { currentLocationProvider.currentLocation() }.getOrNull()
            }
            val location = snapshot?.let { value ->
                Location("OneBox").apply {
                    latitude = value.latitude
                    longitude = value.longitude
                    value.altitudeMeters?.let(::setAltitude)
                    accuracy = value.accuracyMeters ?: 0f
                    time = System.currentTimeMillis()
                }
            }
            cameraEngine.updateLocation(location)
            if (location == null) {
                if (state.settings.locationEnabled) {
                    _state.update { it.copy(messageRes = R.string.camera_location_failed) }
                }
                return@launch
            }
            val metadata = withContext(ioDispatcher) {
                val weather = if (state.settings.weatherEnabled) {
                    weatherRepository.getPlaceWeather(location.latitude, location.longitude)
                        .getOrNull()?.current
                } else {
                    null
                }
                val city = if (state.settings.locationEnabled) {
                    weatherRepository.getCityAtLocation(location.latitude, location.longitude).getOrNull()
                } else {
                    null
                }
                CameraWatermarkMetadata(
                    placeLabel = city?.let { place ->
                        listOf(place.adm1, place.adm2, place.name)
                            .filter(String::isNotBlank)
                            .distinct()
                            .joinToString(" · ")
                    }.orEmpty(),
                    weatherText = weather?.text.orEmpty(),
                    temperatureCelsius = weather?.temp.orEmpty(),
                    windDirection = weather?.windDir.orEmpty(),
                    windScale = weather?.windScale.orEmpty(),
                    humidity = weather?.humidity.orEmpty(),
                )
            }
            cameraEngine.updateWatermarkMetadata(metadata)
        }
    }

    fun onShutter(audioPermissionGranted: Boolean) {
        if (state.isRecording) {
            cameraEngine.stopRecording()
            return
        }
        if (state.countdownSeconds != null) {
            cancelCountdown()
            return
        }
        if (state.isCapturing) return
        val seconds = state.settings.timer.seconds
        if (seconds > 0) {
            shutterJob = componentScope.launch {
                for (second in seconds downTo 1) {
                    _state.update { it.copy(countdownSeconds = second) }
                    delay(1000L)
                }
                _state.update { it.copy(countdownSeconds = null) }
                performShutter(audioPermissionGranted)
            }
        } else {
            performShutter(audioPermissionGranted)
        }
    }

    private fun performShutter(audioPermissionGranted: Boolean) {
        _state.update { it.copy(isCapturing = true) }
        when (state.settings.mode) {
            CameraCaptureMode.PHOTO -> cameraEngine.capturePhoto()
            CameraCaptureMode.VIDEO -> cameraEngine.startRecording(audioPermissionGranted)
        }
    }

    fun toggleRecordingPause() {
        if (!state.isRecording) return
        if (state.isRecordingPaused) cameraEngine.resumeRecording() else cameraEngine.pauseRecording()
    }

    fun toggleRecordingMute() {
        if (!state.isRecording) return
        val muted = !state.isRecordingMuted
        cameraEngine.muteRecording(muted)
        _state.update { it.copy(isRecordingMuted = muted) }
    }

    fun savePendingPhoto() {
        val photo = state.pendingPhoto ?: return
        _state.update { it.copy(isSaving = true) }
        cameraEngine.savePhoto(photo) { savedUri ->
            _state.update { current ->
                current.copy(
                    pendingPhoto = null,
                    lastCaptureUri = savedUri,
                    isSaving = false,
                    messageRes = R.string.camera_photo_saved,
                )
            }
            persistState()
        }
    }

    fun retake() {
        state.pendingPhoto?.let(cameraEngine::discardPhoto)
        _state.update { it.copy(pendingPhoto = null, isSaving = false) }
    }

    fun cancelCountdown() {
        shutterJob?.cancel()
        shutterJob = null
        _state.update { it.copy(countdownSeconds = null) }
    }

    fun consumeMessage() {
        _state.update { it.copy(messageRes = null) }
    }

    fun handleBack(): Boolean = when {
        state.isRecording -> {
            cameraEngine.stopRecording()
            true
        }
        state.countdownSeconds != null -> {
            cancelCountdown()
            true
        }
        state.pendingPhoto != null -> {
            retake()
            true
        }
        else -> false
    }

    override fun onReady(capabilities: com.wanbaohe.camera.watermark.domain.CreativeCameraCapabilities) {
        _state.update { current ->
            val settings = current.settings
            val updatedSettings = settings.copy(
                filter = if (settings.filter in capabilities.supportedFilters) settings.filter else CreativeFilter.ORIGINAL,
                scene = if (settings.scene in capabilities.supportedScenes) settings.scene else CreativeScene.AUTO,
                whiteBalance = if (settings.whiteBalance in capabilities.supportedWhiteBalances) {
                    settings.whiteBalance
                } else {
                    com.wanbaohe.camera.watermark.domain.CameraWhiteBalance.AUTO
                },
                videoQuality = if (settings.videoQuality in capabilities.supportedVideoQualities) {
                    settings.videoQuality
                } else {
                    capabilities.supportedVideoQualities.first()
                },
                frameRate = if (settings.frameRate in capabilities.supportedFrameRates) {
                    settings.frameRate
                } else {
                    com.wanbaohe.camera.watermark.domain.CameraFrameRate.AUTO
                },
                videoStabilization = settings.videoStabilization && capabilities.supportsVideoStabilization,
            )
            current.copy(
                capabilities = capabilities,
                settings = updatedSettings,
                isCameraReady = true,
                isCapturing = false,
                zoomRatio = current.zoomRatio.coerceIn(capabilities.minZoomRatio, capabilities.maxZoomRatio),
            )
        }
        cameraEngine.updateSettings(state.settings)
        cameraEngine.setExposureCompensation(state.exposureIndex)
        cameraEngine.setZoomRatio(state.zoomRatio)
    }

    override fun onZoomChanged(ratio: Float) {
        _state.update { it.copy(zoomRatio = ratio) }
        persistState()
    }

    override fun onFailure(failure: CameraXEngine.Failure) {
        val message = when (failure) {
            CameraXEngine.Failure.INITIALIZATION -> R.string.camera_initialization_failed
            CameraXEngine.Failure.CAPTURE -> R.string.camera_capture_failed
            CameraXEngine.Failure.RECORDING -> R.string.camera_recording_failed
            CameraXEngine.Failure.EFFECT -> R.string.camera_effect_failed
            CameraXEngine.Failure.SAVE -> R.string.camera_save_failed
        }
        _state.update { current ->
            current.copy(
                messageRes = message,
                isCapturing = false,
                isSaving = false,
                isRecording = false,
                isRecordingPaused = false,
                countdownSeconds = null,
            )
        }
    }

    override fun onPhotoCaptured(uri: android.net.Uri) {
        _state.update { current ->
            current.copy(
                pendingPhoto = uri,
                lastCaptureUri = uri,
                isCapturing = false,
            )
        }
    }

    override fun onRecordingStarted() {
        _state.update { current ->
            current.copy(
                isCapturing = false,
                isRecording = true,
                isRecordingPaused = false,
                isRecordingMuted = false,
                recordingDurationMs = 0L,
            )
        }
    }

    override fun onRecordingPaused() {
        _state.update { it.copy(isRecordingPaused = true) }
    }

    override fun onRecordingResumed() {
        _state.update { it.copy(isRecordingPaused = false) }
    }

    override fun onRecordingProgress(durationMs: Long) {
        _state.update { it.copy(recordingDurationMs = durationMs) }
    }

    override fun onVideoSaved(uri: android.net.Uri) {
        _state.update { current ->
            current.copy(
                lastCaptureUri = uri,
                isCapturing = false,
                isRecording = false,
                isRecordingPaused = false,
                isRecordingMuted = false,
                recordingDurationMs = 0L,
                messageRes = R.string.camera_video_saved,
            )
        }
        persistState()
    }

    override fun onHistogram(values: List<Int>) {
        _state.update { it.copy(histogram = values) }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): CameraWatermarkComponent
    }
}
