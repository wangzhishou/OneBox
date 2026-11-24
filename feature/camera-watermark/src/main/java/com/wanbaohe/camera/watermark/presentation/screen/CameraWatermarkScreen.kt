/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.resources.Icons as CoreIcons
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.FlashAuto
import com.t8rin.imagetoolbox.core.resources.icons.FlashOff
import com.t8rin.imagetoolbox.core.resources.icons.FlashOn
import com.t8rin.imagetoolbox.core.resources.icons.PhotoLibrary
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMic
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePause
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.CameraCaptureMode
import com.wanbaohe.camera.watermark.domain.CameraFlashMode
import com.wanbaohe.camera.watermark.domain.CameraGrid
import com.wanbaohe.camera.watermark.domain.CameraRatio
import com.wanbaohe.camera.watermark.domain.CameraWatermarkStyle
import com.wanbaohe.camera.watermark.domain.CameraWhiteBalance
import com.wanbaohe.camera.watermark.domain.CreativeCameraUiState
import com.wanbaohe.camera.watermark.domain.CreativeScene
import com.wanbaohe.camera.watermark.presentation.components.CameraSettingsSheet
import com.wanbaohe.camera.watermark.presentation.components.CameraSettingsTab
import com.wanbaohe.camera.watermark.presentation.screenLogic.CameraWatermarkComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton

@Composable
fun CameraWatermarkScreen(
    component: CameraWatermarkComponent,
) {
    val context = LocalContext.current
    val activity = LocalComponentActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = component.state
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val selectedSettingsTab = CameraSettingsTab.entries.firstOrNull { it.name == state.settingsTabName }
        ?: CameraSettingsTab.WATERMARK
    var showLocationGuide by rememberSaveable { mutableStateOf(true) }
    var hasCameraPermission by remember {
        mutableStateOf(context.hasPermission(Manifest.permission.CAMERA))
    }
    var hasAudioPermission by remember {
        mutableStateOf(context.hasPermission(Manifest.permission.RECORD_AUDIO))
    }
    var hasLocationPermission by remember {
        mutableStateOf(
            context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
        )
    }
    var requestLocationAfterPermission by rememberSaveable { mutableStateOf(false) }
    var requestRecordingAfterPermission by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasCameraPermission = result[Manifest.permission.CAMERA] == true ||
            context.hasPermission(Manifest.permission.CAMERA)
        hasAudioPermission = result[Manifest.permission.RECORD_AUDIO] == true ||
            context.hasPermission(Manifest.permission.RECORD_AUDIO)
        hasLocationPermission = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (requestLocationAfterPermission && hasLocationPermission) {
            component.toggleLocation(true)
        }
        requestLocationAfterPermission = false
        if (requestRecordingAfterPermission && hasCameraPermission && hasAudioPermission) {
            component.onShutter(true)
        }
        requestRecordingAfterPermission = false
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }
    LaunchedEffect(hasCameraPermission, hasLocationPermission) {
        if (hasCameraPermission && hasLocationPermission) component.refreshLocation()
    }
    LaunchedEffect(state.messageRes) {
        val message = state.messageRes ?: return@LaunchedEffect
        when (message) {
            R.string.camera_photo_saved, R.string.camera_video_saved -> AppToastHost.showToast(message)
            else -> AppToastHost.showFailureToast(message)
        }
        component.consumeMessage()
    }
    LaunchedEffect(
        previewView,
        hasCameraPermission,
        state.pendingPhoto,
        state.settings.mode,
        state.settings.lens,
        state.settings.ratio,
        state.settings.photoQuality,
        state.settings.videoQuality,
        state.settings.frameRate,
        state.settings.videoStabilization,
        state.settings.histogramEnabled,
        state.settings.watermarkStyle,
        state.settings.hdrEnabled,
    ) {
        val view = previewView
        if (hasCameraPermission && state.pendingPhoto == null && view != null && view.width > 0) {
            component.bindCamera(lifecycleOwner, view)
        } else if (state.pendingPhoto != null) {
            component.unbindCamera()
        }
    }
    DisposableEffect(lifecycleOwner) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            component.unbindCamera()
        }
    }
    BackHandler {
        when {
            showSettings -> showSettings = false
            !component.handleBack() -> component.onGoBack()
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionContent(
            onBack = component.onGoBack,
            onRequest = { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) },
        )
        return
    }
    state.pendingPhoto?.let { photo ->
        CapturedPhotoContent(
            uri = photo,
            isSaving = state.isSaving,
            onRetake = component::retake,
            onSave = component::savePendingPhoto,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        CameraPreviewArea(
            state = state,
            onPreviewView = { previewView = it },
            onBind = { view -> component.bindCamera(lifecycleOwner, view) },
        )
        CameraTopControls(
            state = state,
            onBack = { if (!component.handleBack()) component.onGoBack() },
            onFlash = component::cycleFlashMode,
            onTorch = { enabled -> component.updateSettings { it.copy(torchEnabled = enabled) } },
            onSettings = {
                showLocationGuide = false
                showSettings = true
            },
        )
        AnimatedVisibility(
            visible = !showSettings,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(animationSpec = tween(160)),
            exit = fadeOut(animationSpec = tween(120)),
        ) {
            CameraBottomControls(
                state = state,
            onTemplate = { style ->
                component.updateSettings { it.copy(watermarkStyle = style) }
            },
            selectedSettingsTab = selectedSettingsTab,
            onSettingsTab = { tab ->
                component.selectSettingsTab(tab.name)
                showLocationGuide = false
            },
            onQuickAction = { action ->
                when (action) {
                    CameraQuickAction.TOGGLE_MODE -> component.selectMode(
                        if (state.settings.mode == CameraCaptureMode.PHOTO) {
                            CameraCaptureMode.VIDEO
                        } else {
                            CameraCaptureMode.PHOTO
                        },
                    )
                    CameraQuickAction.CYCLE_RATIO -> component.cycleRatio()
                    CameraQuickAction.CYCLE_TIMER -> component.cycleTimer()
                    CameraQuickAction.CYCLE_GRID -> component.cycleGrid()
                    CameraQuickAction.CYCLE_FILTER -> component.selectFilter(
                        state.capabilities.supportedFilters.sortedBy { it.ordinal }
                            .nextValue(state.settings.filter),
                    )
                    CameraQuickAction.OPEN_EXPOSURE -> {
                        showLocationGuide = false
                        showSettings = true
                    }
                    CameraQuickAction.CYCLE_ZOOM -> {
                        val next = when {
                            state.zoomRatio < 1.5f -> 2f.coerceAtMost(state.capabilities.maxZoomRatio)
                            state.zoomRatio < state.capabilities.maxZoomRatio -> state.capabilities.maxZoomRatio
                            else -> state.capabilities.minZoomRatio
                        }
                        component.setZoomRatio(next)
                    }
                    CameraQuickAction.EXPAND_SCENE,
                    CameraQuickAction.EXPAND_WHITE_BALANCE -> Unit
                }
            },
            onSceneChange = component::selectScene,
            onWhiteBalanceChange = { whiteBalance ->
                component.updateSettings { it.copy(whiteBalance = whiteBalance) }
            },
            onGallery = component::openGallery,
            onShutter = {
                if (state.settings.mode == CameraCaptureMode.VIDEO &&
                    state.settings.audioEnabled && !hasAudioPermission && !state.isRecording
                ) {
                    requestRecordingAfterPermission = true
                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                } else {
                    component.onShutter(hasAudioPermission)
                }
            },
            onPause = component::toggleRecordingPause,
            onMute = component::toggleRecordingMute,
            onSwitch = component::switchCamera,
            )
        }
        state.countdownSeconds?.let { seconds ->
            Text(
                text = seconds.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        if (!state.isCameraReady) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

    LocationPermissionGuide(
        visible = showLocationGuide && !hasLocationPermission &&
            state.settings.watermarkStyle != CameraWatermarkStyle.NONE &&
            (state.settings.locationEnabled || state.settings.altitudeEnabled || state.settings.weatherEnabled),
        onAllow = {
            showLocationGuide = false
            requestLocationAfterPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
            )
        },
        onDismiss = { showLocationGuide = false },
    )
    CameraSettingsSheet(
        visible = showSettings,
        state = state,
        selectedTab = selectedSettingsTab,
        onDismiss = { showSettings = false },
        onSettingsChange = component::updateSettings,
        onExposureChange = component::setExposureCompensation,
        onZoomChange = component::setZoomRatio,
        onSceneChange = component::selectScene,
        onLocationChange = { enabled ->
            if (enabled && !hasLocationPermission) {
                requestLocationAfterPermission = true
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                )
            } else {
                component.toggleLocation(enabled)
            }
        },
        onModeChange = component::selectMode,
        onFilterChange = component::selectFilter,
    )
}

@Composable
private fun CameraPreviewArea(
    state: CreativeCameraUiState,
    onPreviewView: (PreviewView) -> Unit,
    onBind: (PreviewView) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val portrait = maxHeight > maxWidth
        val ratio = state.settings.ratio.value?.let { if (portrait) 1f / it else it }
        val previewModifier = if (ratio == null) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .align(Alignment.Center)
        }
        Box(
            modifier = previewModifier.clip(RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        onPreviewView(this)
                        doOnLayout { onBind(this) }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            CameraGridOverlay(state.settings.grid, Modifier.fillMaxSize())
            if (state.settings.histogramEnabled && state.histogram.isNotEmpty()) {
                CameraHistogram(
                    values = state.histogram,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 88.dp, end = 20.dp)
                        .size(width = 116.dp, height = 64.dp),
                )
            }
            if (state.isRecording) {
                RecordingBadge(
                    durationMs = state.recordingDurationMs,
                    paused = state.isRecordingPaused,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 80.dp),
                )
            }
        }
    }
}

@Composable
private fun CameraTopControls(
    state: CreativeCameraUiState,
    onBack: () -> Unit,
    onFlash: () -> Unit,
    onTorch: (Boolean) -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CameraIconButton(onClick = onBack, description = stringResource(R.string.camera_back)) {
            Icon(painterResource(R.drawable.ic_camera_back), null)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.camera_watermark_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(
                text = stringResource(
                    if (state.settings.mode == CameraCaptureMode.PHOTO) R.string.camera_mode_photo
                    else R.string.camera_mode_video,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.capabilities.hasFlashUnit && state.settings.mode == CameraCaptureMode.PHOTO) {
                CameraIconButton(onClick = onFlash, description = stringResource(R.string.camera_flash)) {
                    Icon(
                        imageVector = when (state.settings.flashMode) {
                            CameraFlashMode.OFF -> CoreIcons.Outlined.FlashOff
                            CameraFlashMode.AUTO -> CoreIcons.Outlined.FlashAuto
                            CameraFlashMode.ON -> CoreIcons.Outlined.FlashOn
                        },
                        contentDescription = null,
                    )
                }
            }
            if (state.capabilities.hasFlashUnit && state.settings.mode == CameraCaptureMode.VIDEO) {
                CameraIconButton(
                    onClick = { onTorch(!state.settings.torchEnabled) },
                    description = stringResource(R.string.camera_torch),
                ) {
                    Icon(
                        CoreIcons.Outlined.FlashOn,
                        null,
                        tint = if (state.settings.torchEnabled) Color(0xFFFFD166) else Color.White,
                    )
                }
            }
            CameraIconButton(onClick = onSettings, description = stringResource(R.string.camera_settings)) {
                Icon(CoreIcons.Outlined.LineSettings, null)
            }
        }
    }
}

private enum class CameraQuickAction {
    TOGGLE_MODE,
    CYCLE_RATIO,
    CYCLE_TIMER,
    CYCLE_GRID,
    CYCLE_FILTER,
    OPEN_EXPOSURE,
    CYCLE_ZOOM,
    EXPAND_SCENE,
    EXPAND_WHITE_BALANCE,
}

private data class CameraQuickSetting(
    val label: String,
    val action: CameraQuickAction,
)

@Composable
private fun CameraBottomControls(
    state: CreativeCameraUiState,
    modifier: Modifier = Modifier,
    onTemplate: (CameraWatermarkStyle) -> Unit,
    selectedSettingsTab: CameraSettingsTab,
    onSettingsTab: (CameraSettingsTab) -> Unit,
    onQuickAction: (CameraQuickAction) -> Unit,
    onSceneChange: (CreativeScene) -> Unit,
    onWhiteBalanceChange: (CameraWhiteBalance) -> Unit,
    onGallery: () -> Unit,
    onShutter: () -> Unit,
    onPause: () -> Unit,
    onMute: () -> Unit,
    onSwitch: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.54f), Color.Black.copy(alpha = 0.9f)),
                ),
            )
            .navigationBarsPadding()
            .padding(top = 56.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!state.isRecording) {
            QuickSettingsContent(
                state = state,
                selectedTab = selectedSettingsTab,
                onTemplate = onTemplate,
                onQuickAction = onQuickAction,
                onSceneChange = onSceneChange,
                onWhiteBalanceChange = onWhiteBalanceChange,
            )
            Spacer(Modifier.height(12.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isRecording) {
                CameraIconButton(onClick = onMute, description = stringResource(R.string.camera_mute)) {
                    Icon(
                        CoreIcons.Outlined.LineMic,
                        null,
                        tint = if (state.isRecordingMuted) Color.Red else Color.White,
                    )
                }
            } else {
                GlassSurface(
                    onClick = onGallery,
                    enabled = true,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    style = GlassStyle.Thin,
                    borderWidth = 1.dp,
                ) {
                    if (state.lastCaptureUri != null) {
                        AsyncImage(
                            model = state.lastCaptureUri,
                            contentDescription = stringResource(R.string.camera_last_capture),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(
                            CoreIcons.Outlined.PhotoLibrary,
                            null,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            ShutterButton(
                mode = state.settings.mode,
                isRecording = state.isRecording,
                enabled = !state.isCapturing,
                onClick = onShutter,
            )
            if (state.isRecording) {
                CameraIconButton(onClick = onPause, description = stringResource(R.string.camera_pause)) {
                    Icon(
                        if (state.isRecordingPaused) CoreIcons.Outlined.CameraAlt else CoreIcons.Outlined.LinePause,
                        null,
                    )
                }
            } else {
                val switchDescription = stringResource(R.string.camera_switch)
                GlassSurface(
                    onClick = onSwitch,
                    enabled = state.capabilities.hasFrontCamera,
                    modifier = Modifier
                        .size(56.dp)
                        .semantics { contentDescription = switchDescription },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    style = GlassStyle.Thin,
                    borderWidth = 1.dp,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_switch),
                        contentDescription = null,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        }
        if (!state.isRecording) {
            Spacer(Modifier.height(8.dp))
            CameraSettingsDestinations(
                selected = selectedSettingsTab,
                onSelected = onSettingsTab,
            )
        }
    }
}

@Composable
private fun QuickSettingsContent(
    state: CreativeCameraUiState,
    selectedTab: CameraSettingsTab,
    onTemplate: (CameraWatermarkStyle) -> Unit,
    onQuickAction: (CameraQuickAction) -> Unit,
    onSceneChange: (CreativeScene) -> Unit,
    onWhiteBalanceChange: (CameraWhiteBalance) -> Unit,
) {
    AnimatedContent(
        targetState = selectedTab,
        modifier = Modifier.fillMaxWidth(),
        transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            val enter = slideInHorizontally(
                animationSpec = tween(240),
                initialOffsetX = { if (forward) it else -it },
            ) + fadeIn(animationSpec = tween(180))
            val exit = slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { if (forward) -it else it },
            ) + fadeOut(animationSpec = tween(140))
            enter togetherWith exit
        },
        label = "camera_quick_settings",
    ) { tab ->
        when (tab) {
            CameraSettingsTab.WATERMARK -> WatermarkTemplateCarousel(
                selected = state.settings.watermarkStyle,
                onSelected = onTemplate,
            )
            CameraSettingsTab.CAPTURE -> {
                val gridLabel = stringResource(
                    when (state.settings.grid) {
                        CameraGrid.OFF -> R.string.camera_grid_off
                        CameraGrid.THIRDS -> R.string.camera_grid_thirds
                        CameraGrid.GOLDEN -> R.string.camera_grid_golden
                        CameraGrid.DIAGONAL -> R.string.camera_grid_diagonal
                    },
                )
                QuickSettingsCarousel(
                    settings = listOf(
                        CameraQuickSetting(
                            label = stringResource(
                                if (state.settings.mode == CameraCaptureMode.PHOTO) {
                                    R.string.camera_mode_photo
                                } else {
                                    R.string.camera_mode_video
                                },
                            ),
                            action = CameraQuickAction.TOGGLE_MODE,
                        ),
                        CameraQuickSetting(
                            label = when (state.settings.ratio) {
                                CameraRatio.FULL -> stringResource(R.string.camera_ratio_full)
                                CameraRatio.RATIO_4_3 -> "4:3"
                                CameraRatio.RATIO_1_1 -> "1:1"
                                CameraRatio.RATIO_16_9 -> "16:9"
                            },
                            action = CameraQuickAction.CYCLE_RATIO,
                        ),
                        CameraQuickSetting(
                            label = "${state.settings.timer.seconds}s",
                            action = CameraQuickAction.CYCLE_TIMER,
                        ),
                        CameraQuickSetting(gridLabel, CameraQuickAction.CYCLE_GRID),
                        CameraQuickSetting(
                            label = stringResource(state.settings.filter.labelRes),
                            action = CameraQuickAction.CYCLE_FILTER,
                        ),
                    ),
                    onAction = onQuickAction,
                )
            }
            CameraSettingsTab.PROFESSIONAL -> ProfessionalQuickSettings(
                state = state,
                onQuickAction = onQuickAction,
                onSceneChange = onSceneChange,
                onWhiteBalanceChange = onWhiteBalanceChange,
            )
        }
    }
}

@Composable
private fun ProfessionalQuickSettings(
    state: CreativeCameraUiState,
    onQuickAction: (CameraQuickAction) -> Unit,
    onSceneChange: (CreativeScene) -> Unit,
    onWhiteBalanceChange: (CameraWhiteBalance) -> Unit,
) {
    val sceneOptions = state.capabilities.supportedScenes.sortedBy { it.ordinal }
    val whiteBalanceOptions = state.capabilities.supportedWhiteBalances.sortedBy { it.ordinal }
    var expandedSetting by rememberSaveable { mutableStateOf<String?>(null) }
    AnimatedContent(
        targetState = expandedSetting,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
        },
        label = "professional_quick_setting_expansion",
    ) { expanded ->
        when {
            expanded == "scene" && sceneOptions.size > 1 -> ExpandedProfessionalOptions(
                title = stringResource(R.string.camera_scene),
                options = sceneOptions,
                selected = state.settings.scene,
                label = { stringResource(it.labelRes) },
                onDismiss = { expandedSetting = null },
                onSelected = {
                    onSceneChange(it)
                    expandedSetting = null
                },
            )
            expanded == "white_balance" && whiteBalanceOptions.size > 1 -> ExpandedProfessionalOptions(
                title = stringResource(R.string.camera_white_balance),
                options = whiteBalanceOptions,
                selected = state.settings.whiteBalance,
                label = { stringResource(it.labelRes) },
                onDismiss = { expandedSetting = null },
                onSelected = {
                    onWhiteBalanceChange(it)
                    expandedSetting = null
                },
            )
            else -> {
                val settings = mutableListOf<CameraQuickSetting>()
                if (state.capabilities.minExposureIndex < state.capabilities.maxExposureIndex) {
                    settings += CameraQuickSetting(
                        label = stringResource(
                            R.string.camera_exposure_short,
                            state.exposureIndex * state.capabilities.exposureStep,
                        ),
                        action = CameraQuickAction.OPEN_EXPOSURE,
                    )
                }
                if (state.capabilities.maxZoomRatio > state.capabilities.minZoomRatio) {
                    settings += CameraQuickSetting(
                        label = "%.1fx".format(state.zoomRatio),
                        action = CameraQuickAction.CYCLE_ZOOM,
                    )
                }
                if (sceneOptions.size > 1) {
                    settings += CameraQuickSetting(
                        label = "${stringResource(R.string.camera_scene)} · " +
                            stringResource(state.settings.scene.labelRes),
                        action = CameraQuickAction.EXPAND_SCENE,
                    )
                }
                if (whiteBalanceOptions.size > 1) {
                    settings += CameraQuickSetting(
                        label = "${stringResource(R.string.camera_white_balance)} · " +
                            stringResource(state.settings.whiteBalance.labelRes),
                        action = CameraQuickAction.EXPAND_WHITE_BALANCE,
                    )
                }
                QuickSettingsCarousel(
                    settings = settings,
                    onAction = { action ->
                        when (action) {
                            CameraQuickAction.EXPAND_SCENE -> expandedSetting = "scene"
                            CameraQuickAction.EXPAND_WHITE_BALANCE -> expandedSetting = "white_balance"
                            else -> onQuickAction(action)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun <T> ExpandedProfessionalOptions(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassFilterChip(
                selected = false,
                onClick = onDismiss,
                label = { Text(stringResource(R.string.camera_back)) },
                shape = RoundedCornerShape(percent = 50),
                colors = FilterChipDefaults.filterChipColors(labelColor = Color.White),
                glassContainerColor = Color.White,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            items(options, key = { it.toString() }) { option ->
                val isSelected = option == selected
                GlassFilterChip(
                    selected = isSelected,
                    onClick = { onSelected(option) },
                    label = { Text(label(option)) },
                    shape = RoundedCornerShape(percent = 50),
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedLabelColor = Color.Black,
                    ),
                    glassContainerColor = Color.White,
                    glassSelectedContainerColor = Color.White,
                )
            }
        }
    }
}

@Composable
private fun QuickSettingsCarousel(
    settings: List<CameraQuickSetting>,
    onAction: (CameraQuickAction) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        items(settings, key = CameraQuickSetting::action) { setting ->
            GlassFilterChip(
                selected = false,
                onClick = { onAction(setting.action) },
                label = {
                    Text(text = setting.label, style = MaterialTheme.typography.labelMedium)
                },
                shape = RoundedCornerShape(percent = 50),
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = Color.White,
                    selectedLabelColor = Color.Black,
                ),
                glassContainerColor = Color.White,
                glassSelectedContainerColor = Color.White,
                style = GlassStyle.Regular,
            )
        }
    }
}

@Composable
private fun WatermarkTemplateCarousel(
    selected: CameraWatermarkStyle,
    onSelected: (CameraWatermarkStyle) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        items(
            items = CameraWatermarkStyle.entries.filterNot { it == CameraWatermarkStyle.NONE },
            key = CameraWatermarkStyle::name,
        ) { style ->
            val isSelected = style == selected
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith
                        fadeOut(animationSpec = tween(120))
                },
                label = "watermark_chip_selection",
            ) { selectedState ->
                GlassFilterChip(
                    selected = selectedState,
                    onClick = { onSelected(style) },
                    label = {
                        Text(
                            text = stringResource(style.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedState) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    shape = RoundedCornerShape(percent = 50),
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedLabelColor = Color.Black,
                    ),
                    glassContainerColor = Color.White,
                    glassSelectedContainerColor = Color.White,
                    style = GlassStyle.Regular,
                )
            }
        }
    }
}

@Composable
private fun CameraSettingsDestinations(
    selected: CameraSettingsTab,
    onSelected: (CameraSettingsTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CameraSettingsTab.entries.forEach { tab ->
            val label = stringResource(tab.labelRes)
            val isSelected = tab == selected
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.64f),
                animationSpec = tween(180),
                label = "camera_settings_tab_color",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(tab) }
                    .padding(vertical = 8.dp)
                    .semantics { contentDescription = label },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(if (isSelected) Color.White else Color.Transparent, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun CameraIconButton(
    onClick: () -> Unit,
    description: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = description },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.35f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun ShutterButton(
    mode: CameraCaptureMode,
    isRecording: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.camera_shutter)
    GlassSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(84.dp)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = Color.White,
        style = GlassStyle.None,
        borderWidth = 0.dp,
    ) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            drawCircle(Color.White, style = Stroke(width = 3.dp.toPx()))
            if (mode == CameraCaptureMode.VIDEO && isRecording) {
                val size = this.size.minDimension * 0.38f
                drawRoundRect(
                    color = Color.Red,
                    topLeft = Offset((this.size.width - size) / 2f, (this.size.height - size) / 2f),
                    size = androidx.compose.ui.geometry.Size(size, size),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
                )
            } else {
                drawCircle(
                    color = if (mode == CameraCaptureMode.VIDEO) Color.Red else Color.White,
                    radius = this.size.minDimension * 0.38f,
                )
            }
        }
    }
}

@Composable
private fun CameraGridOverlay(grid: CameraGrid, modifier: Modifier = Modifier) {
    if (grid == CameraGrid.OFF) return
    Canvas(modifier) {
        val color = Color.White.copy(alpha = 0.48f)
        val stroke = 0.7.dp.toPx()
        when (grid) {
            CameraGrid.OFF -> Unit
            CameraGrid.THIRDS -> {
                listOf(1f / 3f, 2f / 3f).forEach { fraction ->
                    drawLine(color, Offset(size.width * fraction, 0f), Offset(size.width * fraction, size.height), stroke)
                    drawLine(color, Offset(0f, size.height * fraction), Offset(size.width, size.height * fraction), stroke)
                }
            }
            CameraGrid.GOLDEN -> {
                listOf(0.382f, 0.618f).forEach { fraction ->
                    drawLine(color, Offset(size.width * fraction, 0f), Offset(size.width * fraction, size.height), stroke)
                    drawLine(color, Offset(0f, size.height * fraction), Offset(size.width, size.height * fraction), stroke)
                }
            }
            CameraGrid.DIAGONAL -> {
                drawLine(color, Offset.Zero, Offset(size.width, size.height), stroke)
                drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), stroke)
                drawCircle(color, radius = size.minDimension * 0.18f, style = Stroke(stroke))
            }
        }
    }
}

@Composable
private fun CameraHistogram(values: List<Int>, modifier: Modifier = Modifier) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier.background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(10.dp)).padding(6.dp)) {
        val barWidth = size.width / values.size
        values.forEachIndexed { index, value ->
            val height = size.height * value.toFloat() / max
            drawRect(
                color = Color.White.copy(alpha = 0.82f),
                topLeft = Offset(index * barWidth, size.height - height),
                size = androidx.compose.ui.geometry.Size(barWidth.coerceAtLeast(1f), height),
            )
        }
    }
}

@Composable
private fun RecordingBadge(durationMs: Long, paused: Boolean, modifier: Modifier = Modifier) {
    val totalSeconds = durationMs / 1000L
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.55f),
        style = GlassStyle.Thin,
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(8.dp).background(if (paused) Color.Gray else Color.Red, CircleShape))
            Text(
                text = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun LocationPermissionGuide(
    visible: Boolean,
    onAllow: () -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = CoreIcons.Outlined.LineLocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.camera_location_guide_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.camera_location_guide_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            GlassTonalButton(onClick = onAllow) {
                Text(stringResource(R.string.camera_allow_location))
            }
        },
        dismissButton = {
            GlassOutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.camera_not_now))
            }
        },
    )
}

@Composable
private fun CameraPermissionContent(onBack: () -> Unit, onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            CoreIcons.Outlined.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.camera_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.camera_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        GlassTonalButton(onClick = onRequest) {
            Text(stringResource(R.string.camera_permission_grant))
        }
        Spacer(Modifier.height(12.dp))
        GlassOutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.camera_back))
        }
    }
}

@Composable
private fun CapturedPhotoContent(
    uri: android.net.Uri,
    isSaving: Boolean,
    onRetake: () -> Unit,
    onSave: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CameraIconButton(onClick = onRetake, description = stringResource(R.string.camera_retake)) {
                Icon(painterResource(R.drawable.ic_camera_back), null)
            }
            Text(
                text = stringResource(R.string.camera_confirm_photo),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(48.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.camera_captured_photo),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            GlassOutlinedButton(
                onClick = onRetake,
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                contentColor = Color.White,
            ) {
                Text(
                    text = stringResource(R.string.camera_retake),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.width(12.dp))
            GlassTonalButton(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.camera_save_to_gallery),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun <T> List<T>.nextValue(current: T): T {
    if (isEmpty()) return current
    val index = indexOf(current)
    return this[(index + 1 + size) % size]
}

private fun android.content.Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
