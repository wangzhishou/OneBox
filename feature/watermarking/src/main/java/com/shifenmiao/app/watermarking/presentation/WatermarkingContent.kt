/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 */

package com.shifenmiao.app.watermarking.presentation

import android.Manifest
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.model.event.PermissionRequest
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ImageReset
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.WatermarkBottomBar
import com.shifenmiao.app.watermarking.presentation.components.WatermarkPanelHost
import com.shifenmiao.app.watermarking.presentation.canvas.WatermarkCanvas
import com.shifenmiao.app.watermarking.presentation.panels.ExportPanel
import com.shifenmiao.app.watermarking.presentation.screenLogic.WatermarkingComponent
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.ContextUtils.requestPermissions
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.utils.AutoContentBasedColors
import com.wanbaohe.core.ui.widget.buttons.ShareButton
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.wanbaohe.core.ui.widget.dialogs.ResetDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.image.AutoFilePicker
import com.wanbaohe.core.ui.widget.image.ImageNotPickedWidget
import com.wanbaohe.core.ui.widget.sheets.ProcessImagesPreferenceSheet

@Composable
fun WatermarkingContent(
    component: WatermarkingComponent,
) {
    AutoContentBasedColors(model = component.selectedUri)
    val imagePicker = rememberImagePicker { uris: List<Uri> ->
        component.setUris(
            uris = (component.uris + uris).distinct(),
            onFailure = AppToastHost::showFailureToast,
        )
    }
    AutoFilePicker(
        onAutoPick = imagePicker::pickImage,
        // initialUris 只在首次进入时有值;进程重建后用它判断会再弹一次选择器,
        // 把用户正在编辑的图片覆盖掉。
        isPickedAlready = component.uris.isNotEmpty(),
    )
    val immersiveModeState = rememberImmersiveModeState()
    val activity = LocalComponentActivity.current
    val wideLayout = LocalConfiguration.current.screenWidthDp >= 840
    var activePanel by rememberSaveable { mutableStateOf<WatermarkPanel?>(null) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var onlyCurrent by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showLocationDialog by rememberSaveable { mutableStateOf(false) }
    var isComparing by rememberSaveable { mutableStateOf(false) }
    var editSheetData by remember { mutableStateOf(listOf<Uri>()) }
    val defaultText = stringResource(R.string.watermark_default_text)
    val defaultStamp = stringResource(R.string.watermark_default_stamp)
    val warnIfHiddenWillBeLost = {
        if (component.document.hiddenWatermark.enabled && !component.imageFormat.keepsHiddenWatermark()) {
            AppToastHost.showToast(R.string.watermark_hidden_lossy_warning)
        }
    }
    val onPresetApplied = { preset: WatermarkPreset ->
        val needsLogo = preset.document.elements.any { element ->
            val image = element.content as? WatermarkContent.Image
            image != null && image.imageData.isBlank()
        }
        if (needsLogo) activePanel = WatermarkPanel.PROPERTIES
    }
    val requestLocation = {
        activity.requestPermissions(
            permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            permissionRequest = PermissionRequest.LOCATION,
            onSuccess = component::refreshDynamicData,
            onFailed = { AppToastHost.showFailureToast(R.string.watermark_location_permission_required) },
        )
    }
    LaunchedEffect(component.selectedUri) { isComparing = false }

    BaseScreen(
        title = {
            Column {
                Text(stringResource(R.string.watermarking))
                if (component.document.hiddenWatermark.enabled) {
                    Text(
                        text = stringResource(R.string.watermark_hidden_enabled),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        onGoBack = {
            if (component.haveChanges) showExitDialog = true else component.onGoBack()
        },
        backgroundImage = {
            Spacer(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        },
        supportGlassEffect = false,
        showNavigationBarsPadding = false,
        immersiveModeState = immersiveModeState,
        actions = {
            ShareButton(
                enabled = component.internalBitmap != null,
                onShare = {
                    warnIfHiddenWillBeLost()
                    component.shareBitmaps(AppToastHost::showConfetti)
                },
                onCopy = {
                    warnIfHiddenWillBeLost()
                    component.cacheCurrentImage(Clipboard::copy)
                },
                onEdit = { component.cacheImages { editSheetData = it } },
            )
            EnhancedIconButton(
                enabled = component.internalBitmap != null,
                onClick = { showResetDialog = true },
            ) {
                Icon(Icons.Rounded.ImageReset, contentDescription = stringResource(R.string.reset_image))
            }
            EnhancedIconButton(onClick = { immersiveModeState.enterImmersive() }) {
                Icon(
                    MaterialIcons.Outlined.Fullscreen,
                    contentDescription = stringResource(R.string.watermark_immersive),
                )
            }
        },
        content = {
            if (component.uris.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    ImageNotPickedWidget(onPickImage = imagePicker::pickImage)
                }
            } else {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    WatermarkCanvas(
                        bitmap = component.internalBitmap,
                        frame = component.document.frame,
                        resolvedElements = component.resolvedElements,
                        selectedElementId = component.selectedElementId,
                        isLoading = component.isImageLoading || (component.isResolving && component.resolvedElements.isEmpty()),
                        isComparing = isComparing,
                        canUndo = component.canUndo,
                        canRedo = component.canRedo,
                        immersiveModeState = immersiveModeState,
                        onCompareChange = { isComparing = it },
                        onUndo = component::undo,
                        onRedo = component::redo,
                        onSelectElement = component::selectElement,
                        onTransformStart = component::beginCompoundEdit,
                        onTransformChange = { id, transform ->
                            component.updateElement(id, recordHistory = false) { it.copy(transform = transform) }
                        },
                        onTransformEnd = { id, transform ->
                            component.updateElement(id, recordHistory = false) { it.copy(transform = transform) }
                            component.endCompoundEdit()
                        },
                        onDeleteElement = { id ->
                            component.selectElement(id)
                            component.removeSelectedElement()
                        },
                        onEditElement = { activePanel = WatermarkPanel.PROPERTIES },
                        onAddElement = { kind ->
                            val label = when (kind) {
                                WatermarkKind.TEXT -> defaultText
                                WatermarkKind.STAMP -> defaultStamp
                                else -> ""
                            }
                            val added = component.selectOrAddElement(kind, label)
                            activePanel = WatermarkPanel.PROPERTIES
                            if (added && kind == WatermarkKind.WEATHER) requestLocation()
                        },
                        imageIndex = component.uris.indexOf(component.selectedUri) + 1,
                        imageCount = component.uris.size,
                        onPreviousImage = component::selectLeftUri,
                        onNextImage = component::selectRightUri,
                        hasElements = component.document.elements.isNotEmpty(),
                        hiddenOnly = component.document.hiddenWatermark.enabled &&
                            component.document.elements.none { it.transform.visible },
                        createVisuals = { width, height, bandHeight ->
                            component.createPreviewVisuals(width, height, bandHeight)
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    if (wideLayout && activePanel != null) {
                        Column(
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        ) {
                            WatermarkPanelHost(
                                panel = activePanel!!,
                                component = component,
                                onPresetApplied = onPresetApplied,
                                onRequestLocation = requestLocation,
                                onPickImages = imagePicker::pickImage,
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = immersiveModeState.isUiVisible) {
                    WatermarkBottomBar(
                        activePanel = activePanel,
                        onPanelClick = { panel -> activePanel = if (activePanel == panel) null else panel },
                        onExportClick = { showExport = true },
                    )
                }
            }
        },
    )

    if (!wideLayout && activePanel != null) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = { activePanel = null },
            title = activePanel!!.title(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                WatermarkPanelHost(
                    panel = activePanel!!,
                    component = component,
                    onPresetApplied = onPresetApplied,
                    onRequestLocation = requestLocation,
                    onPickImages = imagePicker::pickImage,
                )
            }
        }
    }

    EnhancedModalBottomSheet(
        visible = showExport,
        onDismiss = { showExport = false },
        title = stringResource(R.string.export),
    ) {
        ExportPanel(
            imageCount = component.uris.size,
            onlyCurrent = onlyCurrent,
            keepExif = component.keepExif,
            stripLocationMetadata = component.stripLocationMetadata,
            currentHiddenWatermark = component.currentHiddenWatermark,
            hiddenWatermark = component.document.hiddenWatermark,
            supportsHiddenWatermark = component.document.elements.any {
                it.content is WatermarkContent.Text || it.content is WatermarkContent.Image
            },
            imageFormat = component.imageFormat,
            quality = component.quality,
            onOnlyCurrentChange = { onlyCurrent = it },
            onKeepExifChange = component::toggleKeepExif,
            onStripLocationChange = component::toggleStripLocationMetadata,
            onHiddenWatermarkChange = component::updateHiddenWatermark,
            onHiddenRejected = {
                AppToastHost.showFailureToast(R.string.watermark_hidden_needs_content)
            },
            onFormatChange = component::setImageFormat,
            onQualityChange = component::setQuality,
            onSave = {
                showExport = false
                component.saveBitmaps(
                    oneTimeSaveLocationUri = null,
                    onlyCurrent = onlyCurrent,
                    onResult = component::parseSaveResults,
                )
            },
            onChooseLocation = {
                // 两个 sheet 同时挂在同一屏会叠加遮罩,先收起导出面板。
                showExport = false
                showLocationDialog = true
            },
            modifier = Modifier.navigationBarsPadding().padding(16.dp),
        )
    }

    ProcessImagesPreferenceSheet(
        uris = editSheetData,
        visible = editSheetData.isNotEmpty(),
        onDismiss = { editSheetData = emptyList() },
        onNavigate = component.onNavigate,
    )
    OneTimeSaveLocationSelectionDialog(
        visible = showLocationDialog,
        onDismiss = { showLocationDialog = false },
        onSaveRequest = { uri ->
            component.saveBitmaps(
                oneTimeSaveLocationUri = uri,
                onlyCurrent = onlyCurrent,
                onResult = component::parseSaveResults,
            )
        },
        formatForFilenameSelection = component.getFormatForFilenameSelection(),
    )
    ResetDialog(
        visible = showResetDialog,
        onDismiss = { showResetDialog = false },
        title = stringResource(R.string.watermark_reset_title),
        text = stringResource(R.string.watermark_reset_sub),
        onReset = component::resetDocument,
    )
    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving,
    )
    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
    )
}
