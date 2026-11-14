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

package com.shifenmiao.app.watermarking.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageScaler
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.Metadata
import com.t8rin.imagetoolbox.core.domain.image.clearAttributes
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.saving.model.onSuccess
import com.t8rin.imagetoolbox.core.domain.utils.ListUtils.leftFrom
import com.t8rin.imagetoolbox.core.domain.utils.ListUtils.rightFrom
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.shifenmiao.app.watermarking.domain.HiddenWatermark
import com.shifenmiao.app.watermarking.domain.editor.WatermarkEditSession
import com.shifenmiao.app.watermarking.domain.export.WatermarkMetadataWriter
import com.shifenmiao.app.watermarking.domain.model.GeoPoint
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.LocationSnapshot
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
import com.shifenmiao.app.watermarking.domain.model.defaultWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPresetStore
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkDetector
import com.shifenmiao.app.watermarking.domain.render.WatermarkContentResolver
import com.shifenmiao.app.watermarking.domain.render.WatermarkRenderer
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.wanbaohe.core.weather.domain.repository.CurrentLocationProvider
import com.wanbaohe.core.weather.domain.repository.CurrentLocationSnapshot
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WatermarkingComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val contentResolver: WatermarkContentResolver,
    private val visualFactory: WatermarkVisualFactory<Bitmap>,
    private val watermarkRenderer: WatermarkRenderer<Bitmap>,
    private val hiddenWatermarkDetector: HiddenWatermarkDetector<Bitmap>,
    private val presetStore: WatermarkPresetStore,
    private val currentLocationProvider: CurrentLocationProvider,
    private val metadataWriter: WatermarkMetadataWriter,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {
    private val editSession = WatermarkEditSession()
    private val contentDrafts = mutableMapOf<WatermarkKind, WatermarkContent>()
    private var resolutionJob: Job? = null
    private var deviceLocation: LocationSnapshot? = null

    private val _document = mutableStateOf(editSession.document)
    val document: WatermarkDocument by _document

    private val _selectedElementId = mutableStateOf(document.elements.firstOrNull()?.id)
    val selectedElementId: String? by _selectedElementId

    private val _resolvedElements = mutableStateOf<List<ResolvedWatermarkElement>>(emptyList())
    val resolvedElements: List<ResolvedWatermarkElement> by _resolvedElements

    private val _canUndo = mutableStateOf(false)
    val canUndo: Boolean by _canUndo
    private val _canRedo = mutableStateOf(false)
    val canRedo: Boolean by _canRedo

    private val _customPresets = mutableStateOf<List<WatermarkPreset>>(emptyList())
    val presets: List<WatermarkPreset>
        get() = BuiltinWatermarkPresets.entries + _customPresets.value

    private val _internalBitmap = mutableStateOf<Bitmap?>(null)
    val internalBitmap: Bitmap? by _internalBitmap
    private val _selectedUri = mutableStateOf(Uri.EMPTY)
    val selectedUri: Uri by _selectedUri
    private val _uris = mutableStateOf<List<Uri>>(emptyList())
    val uris: List<Uri> by _uris

    private val _keepExif = mutableStateOf(false)
    val keepExif: Boolean by _keepExif
    private val _stripLocationMetadata = mutableStateOf(false)
    val stripLocationMetadata: Boolean by _stripLocationMetadata
    private val _imageFormat = mutableStateOf<ImageFormat>(ImageFormat.Default)
    val imageFormat: ImageFormat by _imageFormat
    private val _quality = mutableStateOf<Quality>(Quality.Base())
    val quality: Quality by _quality

    private val _isSaving = mutableStateOf(false)
    val isSaving: Boolean by _isSaving
    private val _done = mutableIntStateOf(0)
    val done: Int by _done
    private val _left = mutableIntStateOf(-1)
    val left: Int by _left

    private val _currentHiddenWatermark = mutableStateOf<HiddenWatermark?>(null)
    val currentHiddenWatermark: HiddenWatermark? by _currentHiddenWatermark
    private val _needsDeviceLocation = mutableStateOf(false)
    val needsDeviceLocation: Boolean by _needsDeviceLocation
    private val _weatherAvailable = mutableStateOf(true)
    val weatherAvailable: Boolean by _weatherAvailable
    private val _isResolving = mutableStateOf(false)
    val isResolving: Boolean by _isResolving
    private val _isDynamicDataLoading = mutableStateOf(false)
    val isDynamicDataLoading: Boolean by _isDynamicDataLoading
    private val _locationLoadFailed = mutableStateOf(false)
    val locationLoadFailed: Boolean by _locationLoadFailed

    val selectedElement
        get() = document.elements.firstOrNull { it.id == selectedElementId }
            ?: document.elements.firstOrNull()

    val hasVisibleElements: Boolean
        get() = document.elements.any { it.transform.visible }

    init {
        debounce {
            loadCustomPresets()
            initialUris?.takeIf(List<Uri>::isNotEmpty)?.let { setUris(it) }
        }
    }

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    fun loadCustomPresets() {
        componentScope.launch {
            _customPresets.update { runCatching { presetStore.listCustomPresets() }.getOrDefault(emptyList()) }
        }
    }

    fun addElement(kind: WatermarkKind) {
        val content = contentDrafts[kind] ?: defaultContent(kind)
        val default = defaultWatermarkElement(content)
        val shouldStack = kind != WatermarkKind.TEXT && kind != WatermarkKind.IMAGE
        val element = default.copy(
            transform = default.transform.copy(
                centerY = if (shouldStack) {
                    (0.82f - document.elements.size * 0.08f).coerceAtLeast(0.15f)
                } else default.transform.centerY,
            ),
            layout = default.layout.copy(
                isRepeated = kind == WatermarkKind.TEXT && document.elements.isEmpty(),
            )
        )
        applyDocument(
            document.copy(elements = document.elements + element),
        )
        _selectedElementId.update { element.id }
    }

    fun selectElement(id: String?) {
        _selectedElementId.update { id }
    }

    fun changeSelectedKind(kind: WatermarkKind) {
        val selected = selectedElement ?: return
        contentDrafts[selected.content.kind] = selected.content
        val content = contentDrafts[kind] ?: defaultContent(kind)
        updateSelectedElement(resolve = true) {
            it.copy(content = content)
        }
    }

    fun updateSelectedContent(content: WatermarkContent) {
        contentDrafts[content.kind] = content
        updateSelectedElement(resolve = true) { it.copy(content = content) }
    }

    fun updateSelectedElement(
        resolve: Boolean = false,
        recordHistory: Boolean = true,
        transform: (com.shifenmiao.app.watermarking.domain.model.WatermarkElement) ->
        com.shifenmiao.app.watermarking.domain.model.WatermarkElement,
    ) {
        val id = selectedElement?.id ?: return
        updateElement(id, resolve, recordHistory, transform)
    }

    fun updateElement(
        id: String,
        resolve: Boolean = false,
        recordHistory: Boolean = true,
        transform: (com.shifenmiao.app.watermarking.domain.model.WatermarkElement) ->
        com.shifenmiao.app.watermarking.domain.model.WatermarkElement,
    ) {
        val newDocument = document.copy(
            elements = document.elements.map { element ->
                if (element.id == id) transform(element) else element
            }
        )
        if (recordHistory) editSession.apply(newDocument) else editSession.updateTransient(newDocument)
        publishDocument(resolve)
    }

    fun beginCompoundEdit() = editSession.beginCompoundEdit()

    fun endCompoundEdit() {
        editSession.endCompoundEdit()
        updateHistoryFlags()
        registerChanges()
    }

    fun duplicateSelectedElement() {
        val selected = selectedElement ?: return
        val copy = selected.copy(
            id = UUID.randomUUID().toString(),
            transform = selected.transform.copy(
                centerX = (selected.transform.centerX + 0.04f).coerceIn(0f, 1f),
                centerY = (selected.transform.centerY + 0.04f).coerceIn(0f, 1f),
            ),
        )
        val index = document.elements.indexOfFirst { it.id == selected.id }
        val elements = document.elements.toMutableList().apply { add(index + 1, copy) }
        applyDocument(document.copy(elements = elements))
        _selectedElementId.update { copy.id }
    }

    fun removeSelectedElement() {
        val selected = selectedElement ?: return
        val elements = document.elements.filterNot { it.id == selected.id }
        applyDocument(document.copy(elements = elements))
        _selectedElementId.update { elements.lastOrNull()?.id }
    }

    fun moveSelectedElement(offset: Int) {
        val selected = selectedElement ?: return
        val index = document.elements.indexOfFirst { it.id == selected.id }
        val target = (index + offset).coerceIn(0, document.elements.lastIndex)
        if (index == target) return
        val elements = document.elements.toMutableList().apply {
            removeAt(index)
            add(target, selected)
        }
        applyDocument(document.copy(elements = elements))
    }

    fun applyPreset(preset: WatermarkPreset) {
        applyPresetDocument(preset.document)
    }

    private fun applyPresetDocument(document: WatermarkDocument) {
        applyDocument(document)
        _selectedElementId.update { document.elements.lastOrNull()?.id }
    }

    fun saveCurrentAsPreset(name: String) {
        if (name.isBlank()) return
        componentScope.launch {
            val preset = WatermarkPreset(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                document = document,
            )
            runCatching { presetStore.savePreset(preset) }
                .onSuccess { saved ->
                    _customPresets.update { current -> listOf(saved) + current }
                }
        }
    }

    fun deleteCustomPreset(id: String) {
        componentScope.launch {
            runCatching { presetStore.deletePreset(id) }
                .onSuccess { _customPresets.update { current -> current.filterNot { it.id == id } } }
        }
    }

    fun undo() {
        if (editSession.undo()) publishDocument(resolve = true)
    }

    fun redo() {
        if (editSession.redo()) publishDocument(resolve = true)
    }

    fun resetDocument() {
        applyPresetDocument(WatermarkDocument())
    }

    fun updateHiddenWatermark(params: HiddenWatermarkParams) {
        applyDocument(document.copy(hiddenWatermark = params))
    }

    fun refreshDynamicData() {
        componentScope.launch {
            _isDynamicDataLoading.update { true }
            _locationLoadFailed.update { false }
            val location = withContext(ioDispatcher) { currentLocationProvider.currentLocation() }
            deviceLocation = location?.toDomain()
            _locationLoadFailed.update { location == null }
            resolveCurrentDocument()
            _isDynamicDataLoading.update { false }
        }
    }

    private fun applyDocument(document: WatermarkDocument) {
        editSession.apply(document)
        publishDocument(resolve = true)
    }

    private fun publishDocument(resolve: Boolean) {
        val current = documentFromSession()
        _document.update { current }
        _resolvedElements.update { resolved ->
            resolved.mapNotNull { item ->
                current.elements.firstOrNull { it.id == item.element.id }?.let { element ->
                    item.copy(element = element)
                }
            }
        }
        updateHistoryFlags()
        registerChanges()
        if (resolve) resolveCurrentDocument()
    }

    private fun documentFromSession(): WatermarkDocument = editSession.document

    private fun updateHistoryFlags() {
        _canUndo.update { editSession.canUndo }
        _canRedo.update { editSession.canRedo }
    }

    private fun resolveCurrentDocument() {
        val uri = selectedUri.takeIf { it != Uri.EMPTY } ?: return
        resolutionJob?.cancel()
        val requestedDocument = documentFromSession()
        val requestedLocation = deviceLocation
        resolutionJob = componentScope.launch {
            _isResolving.update { true }
            val resolution = withContext(defaultDispatcher) {
                contentResolver.resolve(uri.toString(), requestedDocument, requestedLocation)
            }
            ensureActive()
            _resolvedElements.update { resolution.elements }
            _needsDeviceLocation.update { resolution.needsDeviceLocation }
            _weatherAvailable.update { resolution.weatherAvailable }
            _isResolving.update { false }
        }
    }

    suspend fun createPreviewVisuals(
        canvasWidth: Int,
        canvasHeight: Int,
    ): Map<String, WatermarkVisual<Bitmap>> = buildMap {
        resolvedElements.forEach { resolved ->
            if (!resolved.element.transform.visible) return@forEach
            visualFactory.create(resolved, canvasWidth, canvasHeight)?.let { visual ->
                put(resolved.element.id, visual)
            }
        }
    }

    fun setUris(
        uris: List<Uri>,
        onFailure: (Throwable) -> Unit = {},
    ) {
        _uris.update { uris }
        if (uris.isEmpty()) {
            _selectedUri.update { Uri.EMPTY }
            _internalBitmap.update { null }
            _resolvedElements.update { emptyList() }
        } else {
            updateSelectedUri(uris.first(), onFailure)
        }
    }

    fun updateSelectedUri(
        uri: Uri,
        onFailure: (Throwable) -> Unit = {},
    ) {
        componentScope.launch {
            _selectedUri.update { uri }
            _isImageLoading.update { true }
            val imageData = runCatching {
                imageGetter.getImage(uri.toString(), originalSize = false)
            }.getOrElse {
                onFailure(it)
                null
            }
            if (imageData == null) {
                _isImageLoading.update { false }
                return@launch
            }
            _internalBitmap.update { imageScaler.scaleUntilCanShow(imageData.image) }
            _imageFormat.update { imageData.imageInfo.imageFormat }
            _currentHiddenWatermark.update { hiddenWatermarkDetector.detect(imageData.image) }
            _isImageLoading.update { false }
            resolveCurrentDocument()
        }
    }

    fun updateUrisSilently(removedUri: Uri) {
        val remaining = uris.filterNot { it == removedUri }
        _uris.update { remaining }
        if (selectedUri == removedUri) {
            if (remaining.isEmpty()) setUris(emptyList()) else updateSelectedUri(remaining.first())
        }
    }

    fun selectLeftUri() {
        val index = uris.indexOf(selectedUri)
        if (index >= 0) uris.leftFrom(index).let(::updateSelectedUri)
    }

    fun selectRightUri() {
        val index = uris.indexOf(selectedUri)
        if (index >= 0) uris.rightFrom(index).let(::updateSelectedUri)
    }

    fun setQuality(quality: Quality) {
        _quality.update { quality }
        registerChanges()
    }

    fun setImageFormat(imageFormat: ImageFormat) {
        _imageFormat.update { imageFormat }
        registerChanges()
    }

    fun toggleKeepExif(value: Boolean) {
        _keepExif.update { value }
        if (!value) _stripLocationMetadata.update { false }
        registerChanges()
    }

    fun toggleStripLocationMetadata(value: Boolean) {
        _stripLocationMetadata.update { value }
        registerChanges()
    }

    private suspend fun renderUri(uri: Uri): RenderedImage? {
        val imageData = imageGetter.getImage(uri.toString(), originalSize = true) ?: return null
        val resolution = contentResolver.resolve(uri.toString(), documentFromSession(), deviceLocation)
        val bitmap = watermarkRenderer.render(
            image = imageData.image,
            resolution = resolution,
            hiddenWatermark = documentFromSession().hiddenWatermark,
        ) ?: return null
        val metadata = imageData.metadata?.let { metadata ->
            if (keepExif) {
                resolution.photoContext.dateTime?.let { dateTime ->
                    metadata.setAttribute(MetadataTag.DatetimeOriginal, dateTime)
                }
                metadata.sanitizedMetadata()
            } else null
        }
        return RenderedImage(
            bitmap = bitmap,
            imageInfo = ImageInfo(
                imageFormat = imageFormat,
                width = bitmap.width,
                height = bitmap.height,
                quality = quality,
            ),
            metadata = metadata,
        )
    }

    private fun Metadata.sanitizedMetadata(): Metadata = apply {
        if (stripLocationMetadata) {
            clearAttributes(MetadataTag.entries.filter { it.key.startsWith("GPS") })
        }
    }

    fun saveBitmaps(
        oneTimeSaveLocationUri: String?,
        onlyCurrent: Boolean = false,
        onResult: (List<SaveResult>) -> Unit,
    ) {
        savingJob = componentScope.launch {
            val targets = if (onlyCurrent) listOf(selectedUri) else uris
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { targets.size }
            val results = mutableListOf<SaveResult>()
            targets.forEachIndexed { index, uri ->
                ensureActive()
                val rendered = renderUri(uri)
                if (rendered == null) {
                    results.add(SaveResult.Error.Exception(Throwable()))
                    _done.update { it + 1 }
                    return@forEachIndexed
                }
                val saveResult = fileController.save(
                    saveTarget = ImageSaveTarget(
                        imageInfo = rendered.imageInfo,
                        originalUri = uri.toString(),
                        sequenceNumber = if (targets.size > 1) index + 1 else null,
                        metadata = if (stripLocationMetadata) null else rendered.metadata,
                        data = imageCompressor.compressAndTransform(
                            image = rendered.bitmap,
                            imageInfo = rendered.imageInfo,
                        ),
                    ),
                    keepOriginalMetadata = keepExif && !stripLocationMetadata,
                    oneTimeSaveLocationUri = oneTimeSaveLocationUri,
                )
                if (saveResult is SaveResult.Success && keepExif && stripLocationMetadata) {
                    saveResult.fileUri?.let { outputUri ->
                        rendered.metadata?.let { metadata -> metadataWriter.writeMetadata(outputUri, metadata) }
                    }
                }
                results.add(saveResult)
                rendered.bitmap.recycle()
                _done.update { it + 1 }
            }
            onResult(results.onSuccess(::registerSave))
            _isSaving.update { false }
            _left.update { -1 }
        }
    }

    fun shareBitmaps(onComplete: () -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { uris.size }
            shareProvider.shareImages(
                uris = uris.map(Uri::toString),
                imageLoader = { uri ->
                    renderUri(uri.toUri())?.let { rendered ->
                        rendered.bitmap to rendered.imageInfo
                    }
                },
                onProgressChange = { progress ->
                    if (progress == -1) {
                        onComplete()
                        _isSaving.update { false }
                        _done.update { 0 }
                    } else _done.update { progress }
                },
            )
            _isSaving.update { false }
            _left.update { -1 }
        }
    }

    fun cacheCurrentImage(onComplete: (Uri) -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.update { true }
            renderUri(selectedUri)?.let { rendered ->
                shareProvider.cacheImage(
                    image = rendered.bitmap,
                    imageInfo = rendered.imageInfo.copy(originalUri = selectedUri.toString()),
                )?.let { uri -> onComplete(uri.toUri()) }
            }
            _isSaving.update { false }
        }
    }

    fun cacheImages(onComplete: (List<Uri>) -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { uris.size }
            val cached = mutableListOf<Uri>()
            uris.forEach { uri ->
                ensureActive()
                renderUri(uri)?.let { rendered ->
                    shareProvider.cacheImage(
                        image = rendered.bitmap,
                        imageInfo = rendered.imageInfo.copy(originalUri = uri.toString()),
                    )?.let { cached.add(it.toUri()) }
                }
                _done.update { it + 1 }
            }
            onComplete(cached)
            _isSaving.update { false }
            _left.update { -1 }
        }
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.update { false }
        _left.update { -1 }
    }

    fun getFormatForFilenameSelection(): ImageFormat? =
        if (uris.size == 1) imageFormat else null

    private data class RenderedImage(
        val bitmap: Bitmap,
        val imageInfo: ImageInfo,
        val metadata: Metadata?,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): WatermarkingComponent
    }
}

private fun CurrentLocationSnapshot.toDomain() = LocationSnapshot(
    coordinates = GeoPoint(latitude, longitude),
    altitudeMeters = altitudeMeters,
    accuracyMeters = accuracyMeters,
    verticalAccuracyMeters = verticalAccuracyMeters,
    capturedAt = capturedAt,
)

private fun defaultContent(kind: WatermarkKind): WatermarkContent = when (kind) {
    WatermarkKind.TEXT -> WatermarkContent.Text()
    WatermarkKind.IMAGE -> WatermarkContent.Image()
    WatermarkKind.STAMP -> WatermarkContent.Stamp()
    WatermarkKind.TIMESTAMP -> WatermarkContent.Timestamp()
    WatermarkKind.ALTITUDE -> WatermarkContent.Altitude()
    WatermarkKind.WEATHER -> WatermarkContent.Weather()
    WatermarkKind.COORDINATES -> WatermarkContent.Coordinates()
}
