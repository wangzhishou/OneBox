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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.model.event.PermissionRequest
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
import com.shifenmiao.app.watermarking.domain.model.usesDeviceLocation
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.presentation.components.WatermarkBottomBar
import com.shifenmiao.app.watermarking.presentation.components.WatermarkPanelHost
import com.shifenmiao.app.watermarking.presentation.canvas.WatermarkCanvas
import com.shifenmiao.app.watermarking.presentation.panels.WatermarkExportAdvancedOptions
import com.shifenmiao.app.watermarking.presentation.start.WatermarkStartScreen
import com.shifenmiao.app.watermarking.presentation.start.WatermarkStartTemplate
import com.shifenmiao.app.watermarking.presentation.screenLogic.WatermarkingComponent
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters.AddFiltersSheet
import com.t8rin.imagetoolbox.core.filters.presentation.widget.filterStrip.FilterStrip
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportCapabilities
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportConfig
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportMetadataPolicy
import com.t8rin.imagetoolbox.core.domain.image.export.ImageExportSummary
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.toModel
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.ContextUtils.requestPermissions
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.utils.AutoContentBasedColors
import com.wanbaohe.core.ui.widget.buttons.ShareButton
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.dialogs.ResetDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.export.ImageExportSection
import com.wanbaohe.core.ui.widget.export.ImageExportSheet
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.image.AutoFilePicker
import com.wanbaohe.core.ui.widget.image.ImageNotPickedWidget
import com.wanbaohe.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import com.t8rin.imagetoolbox.core.utils.fileSize
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    var isToolRailVisible by rememberSaveable { mutableStateOf(false) }
    var showExport by rememberSaveable { mutableStateOf(false) }
    var onlyCurrent by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var saveLocationUri by rememberSaveable { mutableStateOf<String?>(null) }
    var isComparing by rememberSaveable { mutableStateOf(false) }
    var editSheetData by remember { mutableStateOf(listOf<Uri>()) }
    var showFilterPanel by rememberSaveable { mutableStateOf(false) }
    var showFullFilterSheet by rememberSaveable { mutableStateOf(false) }
    var selectedStartTemplate by rememberSaveable {
        mutableStateOf(WatermarkStartTemplate.fromPresetId(component.activePresetId))
    }
    val saveLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        saveLocationUri = uri?.toString() ?: saveLocationUri
    }
    val settingsState = LocalSettingsState.current
    val settingsInteractor = LocalSimpleSettingsInteractor.current
    val settingsScope = rememberCoroutineScope()
    val showGuide = component.uris.isEmpty()
    val displayBitmap = component.displayBitmap
    val quickFilters = remember { UiFilter.sortedGroupedEntries.firstOrNull().orEmpty() }
    val exportConfig = ImageExportConfig(
        format = component.imageFormat,
        quality = component.quality.qualityValue,
        metadataPolicy = when {
            !component.keepExif -> ImageExportMetadataPolicy.RemoveAll
            component.stripLocationMetadata -> ImageExportMetadataPolicy.KeepWithoutLocation
            else -> ImageExportMetadataPolicy.KeepAll
        },
    )
    val exportItemCount = if (onlyCurrent || component.privacy.isActive) 1 else component.uris.size
    val exportBitmap = displayBitmap
    val hiddenWatermarkBlocked = component.document.hiddenWatermark.enabled &&
        !component.imageFormat.keepsHiddenWatermark()
    val defaultText = stringResource(R.string.watermark_default_text)
    val defaultStamp = stringResource(R.string.watermark_default_stamp)
    val warnIfHiddenWillBeLost = {
        if (component.document.hiddenWatermark.enabled && !component.imageFormat.keepsHiddenWatermark()) {
            AppToastHost.showToast(R.string.watermark_hidden_lossy_warning)
        }
    }
    val onPresetApplied = { preset: WatermarkPreset ->
        if (preset.id == com.wanbaohe.watermark.template.domain.BuiltinTemplateId.PRIVACY_REDACTION.id) {
            activePanel = WatermarkPanel.PRIVACY
        } else {
            val needsLogo = preset.document.elements.any { element ->
                val image = element.content as? WatermarkContent.Image
                image != null && image.imageData.isBlank()
            }
            activePanel = if (needsLogo) WatermarkPanel.PROPERTIES else null
        }
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
    val applyStartTemplate: (WatermarkStartTemplate) -> Unit = { template ->
        val preset = template.presetId?.let(BuiltinWatermarkPresets::findById)
        if (preset != null) component.applyPreset(preset) else component.applyBlankDocument()
        if (preset?.document?.usesDeviceLocation() == true) requestLocation()
        if (preset != null) onPresetApplied(preset) else activePanel = null
    }
    val startFromTemplate: (WatermarkStartTemplate) -> Unit = { template ->
        applyStartTemplate(template)
        imagePicker.pickImage()
    }
    LaunchedEffect(component.selectedUri) { isComparing = false }
    LaunchedEffect(component.privacy.isActive) {
        if (!component.privacy.isActive && activePanel == WatermarkPanel.PRIVACY) {
            activePanel = WatermarkPanel.PRESETS
        }
    }

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
        // 传 null 才会让 BaseScreen 走默认的 TopAppBarEmoji（主题切换按钮），
        // 传空 lambda 同样会顶掉默认按钮，所以引导页必须显式传 null。
        actions = if (showGuide) {
            null
        } else {
            {
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
                EnhancedIconButton(onClick = { immersiveModeState.enterImmersive() }) {
                    Icon(
                        MaterialIcons.Outlined.Fullscreen,
                        contentDescription = stringResource(R.string.watermark_immersive),
                    )
                }
            }
        },
        content = {
            if (showGuide) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    WatermarkStartScreen(
                        selected = selectedStartTemplate,
                        onSelect = { selectedStartTemplate = it },
                        onStart = { startFromTemplate(selectedStartTemplate) },
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    WatermarkCanvas(
                        bitmap = displayBitmap,
                        frame = component.document.frame,
                        resolvedElements = component.resolvedElements,
                        selectedElementId = component.selectedElementId,
                        isLoading = component.isImageLoading ||
                            (component.isResolving && component.resolvedElements.isEmpty()) ||
                            component.privacy.analysisState.isRunning,
                        isComparing = isComparing,
                        isToolRailVisible = isToolRailVisible,
                        canUndo = component.canUndo,
                        canRedo = component.canRedo,
                        resetEnabled = component.internalBitmap != null,
                        resetDescriptionRes = if (component.activePresetId == null) {
                            R.string.reset_image
                        } else {
                            R.string.watermark_restore_preset
                        },
                        onReset = { showResetDialog = true },
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
                            component.addElement(kind, label)
                            activePanel = WatermarkPanel.PROPERTIES
                            if (kind == WatermarkKind.WEATHER) requestLocation()
                        },
                        imageIndex = component.uris.indexOf(component.selectedUri) + 1,
                        imageCount = component.uris.size,
                        onPreviousImage = component::selectLeftUri,
                        onNextImage = component::selectRightUri,
                        hasElements = component.document.elements.isNotEmpty(),
                        hiddenOnly = component.document.hiddenWatermark.enabled &&
                            component.document.elements.none { it.transform.visible },
                        privacyDocument = if (component.privacy.isActive) {
                            component.privacy.document
                        } else null,
                        selectedPrivacyRegionId = component.privacy.selectedRegionId,
                        isPrivacyDrawing = component.privacy.isDrawing,
                        onSelectPrivacyRegion = component.privacy::selectRegion,
                        onPrivacyBoundsChange = component.privacy::updateRegionBounds,
                        onDeletePrivacyRegion = component.privacy::deleteRegion,
                        onAddPrivacyRegion = component.privacy::addRegion,
                        createPrivacyPreview = component.privacy::renderPreview,
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
                    if (immersiveModeState.isUiVisible && showFilterPanel && !isComparing) {
                        FilterStrip(
                            filters = quickFilters,
                            selectedFilter = component.baseFilters.singleOrNull(),
                            activeFilterCount = component.baseFilters.size,
                            transformation = component::filterTransformation,
                            onFilterSelected = component::selectFilter,
                            onClearFilters = { component.selectFilter(null) },
                            onOpenFullCatalog = { showFullFilterSheet = true },
                            title = stringResource(R.string.watermark_filter_target),
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        )
                    }
                    }
                AnimatedVisibility(visible = immersiveModeState.isUiVisible) {
                    WatermarkBottomBar(
                        activePanel = activePanel,
                        isPrivacyActive = component.privacy.isActive,
                        isToolRailVisible = isToolRailVisible,
                        isFilterPanelVisible = showFilterPanel,
                        onPanelClick = { panel ->
                            when (panel) {
                                WatermarkPanel.TOOLS -> isToolRailVisible = !isToolRailVisible
                                WatermarkPanel.FILTER -> {
                                    showFilterPanel = !showFilterPanel
                                    if (showFilterPanel) activePanel = null
                                }
                                else -> activePanel = if (activePanel == panel) null else panel
                            }
                        },
                        onExportClick = { showExport = true },
                    )
                }
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

    val onFullFilterPicked: (UiFilter<*>) -> Unit = { filter ->
        component.addFilter(filter)
        showFullFilterSheet = false
    }
    AddFiltersSheet(
        component = component.addFiltersSheetComponent,
        filterTemplateCreationSheetComponent = component.filterTemplateCreationSheetComponent,
        visible = showFullFilterSheet,
        onDismiss = { showFullFilterSheet = false },
        previewBitmap = displayBitmap,
        onFilterPicked = onFullFilterPicked,
        onFilterPickedWithParams = onFullFilterPicked,
        canAddTemplates = false,
    )
    ImageExportSheet(
        visible = showExport,
        config = exportConfig,
        capabilities = ImageExportCapabilities(
            formats = ImageFormat.entries,
            supportsSaveLocation = true,
            supportsBackgroundColor = exportBitmap?.hasAlpha() == true,
            metadataPolicies = ImageExportMetadataPolicy.entries,
        ),
        summary = ImageExportSummary(
            itemCount = exportItemCount,
            width = exportBitmap?.width,
            height = exportBitmap?.height,
            sourceSizeBytes = component.selectedUri.fileSize(),
        ),
        estimateBitmap = exportBitmap,
        saveLocation = saveLocationUri?.let {
            Uri.parse(it).lastPathSegment?.substringAfterLast(':')
        },
        onChooseSaveLocation = { saveLocationLauncher.launch(null) },
        onConfigChange = component::updateImageExportConfig,
        onDismiss = { showExport = false },
        onSave = {
            showExport = false
            component.saveBitmaps(
                oneTimeSaveLocationUri = saveLocationUri,
                onlyCurrent = onlyCurrent,
                onResult = component::parseSaveResults,
            )
        },
        onShare = {
            showExport = false
            component.shareBitmaps(
                onlyCurrent = onlyCurrent,
                onComplete = AppToastHost::showConfetti,
            )
        },
        saveEnabled = component.uris.isNotEmpty() && !component.isSaving,
        shareEnabled = component.uris.isNotEmpty() && !component.isSaving,
        blockingMessage = if (hiddenWatermarkBlocked) {
            stringResource(R.string.watermark_hidden_lossy_warning)
        } else {
            null
        },
        onFixBlocking = if (hiddenWatermarkBlocked) {
            { component.setImageFormat(ImageFormat.Png.Lossless) }
        } else {
            null
        },
        backgroundColor = settingsState.backgroundForNoAlphaImageFormats,
        onBackgroundColorChange = { color ->
            settingsScope.launch {
                settingsInteractor.setBackgroundColorForNoAlphaFormats(color.toModel())
            }
        },
        content = {
            if (component.uris.size > 1 && !component.privacy.isActive) {
                ImageExportSection(
                    title = stringResource(R.string.image_export_items, exportItemCount)
                ) {
                    GlassSegmentedButtonRow(
                        options = listOf(true, false),
                        selectedOption = onlyCurrent,
                        onOptionSelected = { onlyCurrent = it },
                        label = { current ->
                            Text(
                                stringResource(
                                    if (current) R.string.watermark_current_image
                                    else R.string.watermark_all_images
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        advancedContent = {
            WatermarkExportAdvancedOptions(
                currentHiddenWatermark = component.currentHiddenWatermark,
                hiddenWatermark = component.document.hiddenWatermark,
                supportsHiddenWatermark = component.document.elements.any {
                    it.content is WatermarkContent.Text || it.content is WatermarkContent.Image
                },
                onHiddenWatermarkChange = component::updateHiddenWatermark,
                onHiddenRejected = {
                    AppToastHost.showFailureToast(R.string.watermark_hidden_needs_content)
                },
            )
        },
    )

    ProcessImagesPreferenceSheet(
        uris = editSheetData,
        visible = editSheetData.isNotEmpty(),
        onDismiss = { editSheetData = emptyList() },
        onNavigate = component.onNavigate,
    )
    ResetDialog(
        visible = showResetDialog,
        onDismiss = { showResetDialog = false },
        title = stringResource(
            if (component.activePresetId == null) R.string.watermark_reset_title
            else R.string.watermark_restore_preset_title
        ),
        text = stringResource(
            if (component.activePresetId == null) R.string.watermark_reset_sub
            else R.string.watermark_restore_preset_sub
        ),
        confirmText = stringResource(
            if (component.activePresetId == null) R.string.reset
            else R.string.watermark_restore_preset
        ),
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

private fun WatermarkingComponent.updateImageExportConfig(config: ImageExportConfig) {
    if (imageFormat != config.format) setImageFormat(config.format)
    val currentQuality = quality
    val updatedQuality = when {
        config.format is ImageFormat.Png.Lossy && currentQuality is Quality.PngLossy -> {
            currentQuality.copy(compressionLevel = config.quality)
        }
        (config.format is ImageFormat.Tif || config.format is ImageFormat.Tiff) &&
            currentQuality is Quality.Tiff -> {
            currentQuality.copy(compressionScheme = config.quality)
        }
        else -> Quality.Base(config.quality)
    }.coerceIn(config.format)
    if (quality != updatedQuality) setQuality(updatedQuality)

    val keepExif = config.metadataPolicy != ImageExportMetadataPolicy.RemoveAll
    val stripLocation = config.metadataPolicy == ImageExportMetadataPolicy.KeepWithoutLocation
    if (this.keepExif != keepExif) toggleKeepExif(keepExif)
    if (stripLocationMetadata != stripLocation) toggleStripLocationMetadata(stripLocation)
}
