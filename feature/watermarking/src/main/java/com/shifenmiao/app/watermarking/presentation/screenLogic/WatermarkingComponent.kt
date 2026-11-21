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
import com.arkivanov.decompose.childContext
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
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.transformation.EmptyTransformation
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation
import com.t8rin.imagetoolbox.core.filters.domain.FilterProvider
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.widget.FilterTemplateCreationSheetComponent
import com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters.AddFiltersSheetComponent
import com.t8rin.imagetoolbox.core.resources.R
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
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution
import com.shifenmiao.app.watermarking.domain.model.asPlaceholderColor
import com.shifenmiao.app.watermarking.domain.model.defaultWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.model.previewPlaceholder
import com.shifenmiao.app.watermarking.domain.model.staticResolution
import com.shifenmiao.app.watermarking.domain.preset.BuiltinWatermarkPresets
import com.wanbaohe.watermark.template.domain.BuiltinTemplateId
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPreset
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPresetStore
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkDetector
import com.shifenmiao.app.watermarking.domain.render.WatermarkContentResolver
import com.shifenmiao.app.watermarking.domain.render.WatermarkRenderer
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.wanbaohe.core.weather.domain.repository.CurrentLocationProvider
import com.wanbaohe.core.weather.domain.repository.CurrentLocationSnapshot
import com.wanbaohe.privacy.redaction.domain.render.PrivacyRedactionRenderer
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalyzer
import com.wanbaohe.privacy.redaction.presentation.controller.PrivacyRedactionController
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WatermarkingComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    @Assisted val presetId: String?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val filterProvider: FilterProvider<Bitmap>,
    private val contentResolver: WatermarkContentResolver,
    private val visualFactory: WatermarkVisualFactory<Bitmap>,
    private val watermarkRenderer: WatermarkRenderer<Bitmap>,
    private val hiddenWatermarkDetector: HiddenWatermarkDetector<Bitmap>,
    private val presetStore: WatermarkPresetStore,
    private val currentLocationProvider: CurrentLocationProvider,
    private val metadataWriter: WatermarkMetadataWriter,
    privacyAnalyzer: PrivacyAnalyzer,
    privacyRenderer: PrivacyRedactionRenderer<Bitmap>,
    addFiltersSheetComponentFactory: AddFiltersSheetComponent.Factory,
    filterTemplateCreationSheetComponentFactory: FilterTemplateCreationSheetComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {
    private val editSession = WatermarkEditSession()
    private val contentDrafts = mutableMapOf<WatermarkKind, WatermarkContent>()

    // 滤镜目录子组件与图片创作共用同一套实现，避免水印侧再维护一份筛选/收藏/预览逻辑。
    val addFiltersSheetComponent: AddFiltersSheetComponent =
        addFiltersSheetComponentFactory(componentContext.childContext(key = "watermarkAddFiltersSheet"))
    val filterTemplateCreationSheetComponent: FilterTemplateCreationSheetComponent =
        filterTemplateCreationSheetComponentFactory(
            componentContext.childContext(key = "watermarkFilterTemplateCreationSheet")
        )

    private val _baseFilters = mutableStateOf<List<UiFilter<*>>>(emptyList())
    val baseFilters: List<UiFilter<*>> by _baseFilters
    private val _filteredBitmap = mutableStateOf<Bitmap?>(null)
    /** 画布与导出预览用的底图：应用滤镜后是过滤结果，未应用时就是原图。 */
    val displayBitmap: Bitmap? get() = _filteredBitmap.value ?: internalBitmap
    private var filterJob: Job? = null

    private var resolutionJob: Job? = null
    private var deviceLocation: LocationSnapshot? = null
    val privacy = PrivacyRedactionController(
        analyzer = privacyAnalyzer,
        renderer = privacyRenderer,
        scope = componentScope,
        onChanges = ::registerChanges,
        onHistoryChange = ::updateHistoryFlags,
    )

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
        get() = BuiltinWatermarkPresets.featuredEntries + _customPresets.value

    private val _activePresetId = mutableStateOf<String?>(null)
    val activePresetId: String? by _activePresetId
    private var activePresetDocument: WatermarkDocument? = null

    private val _internalBitmap = mutableStateOf<Bitmap?>(null)
    val internalBitmap: Bitmap? by _internalBitmap
    private val _selectedUri = mutableStateOf(Uri.EMPTY)
    val selectedUri: Uri by _selectedUri
    private val _uris = mutableStateOf<List<Uri>>(emptyList())
    val uris: List<Uri> by _uris

    // 默认保留 EXIF 但剥离定位：拍摄参数对水印用户有价值，经纬度属于隐私。
    // 见《图片水印整合重构·分阶段实施方案》§9 / 验收 §11.8。
    private val _keepExif = mutableStateOf(true)
    val keepExif: Boolean by _keepExif
    private val _stripLocationMetadata = mutableStateOf(true)
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

    val hasVisibleElements: Boolean
        get() = document.elements.any { it.transform.visible }

    init {
        debounce {
            loadCustomPresets()
            initialUris?.takeIf(List<Uri>::isNotEmpty)?.let { setUris(it) }
            // 深链 / 模板直达入口携带 presetId 时，进页面即套用，预览立即可见。
            presetId?.let(::applyPresetById)
        }
    }

    private fun applyPresetById(id: String) {
        BuiltinTemplateId.fromId(id) ?: return
        BuiltinWatermarkPresets.findById(id)?.let(::applyPreset)
    }

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    fun loadCustomPresets() {
        componentScope.launch {
            _customPresets.update { runCatching { presetStore.listCustomPresets() }.getOrDefault(emptyList()) }
        }
    }

    fun addElement(kind: WatermarkKind, label: String = "") {
        val content = contentDrafts[kind] ?: defaultContent(kind, label)
        val default = defaultWatermarkElement(content)
        // 底部会被属性面板挡住，新图层放在画面中上段，叠加以免互相盖住。
        val element = default.copy(
            transform = default.transform.copy(
                centerY = (0.38f + document.elements.size * 0.08f).coerceAtMost(0.62f),
            ),
            layout = default.layout.copy(isRepeated = false),
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

    fun updateSelectedContent(content: WatermarkContent, mergeHistory: Boolean = false) {
        val selected = selectedElement ?: return
        if (selected.transform.locked) return
        contentDrafts[content.kind] = content
        val id = selected.id
        updateSelectedElement(
            resolve = true,
            mergeKey = if (mergeHistory) "$id:${content.kind}" else null,
        ) { it.copy(content = content) }
    }

    fun updateSelectedElement(
        resolve: Boolean = false,
        recordHistory: Boolean = true,
        mergeKey: String? = null,
        transform: (com.shifenmiao.app.watermarking.domain.model.WatermarkElement) ->
        com.shifenmiao.app.watermarking.domain.model.WatermarkElement,
    ) {
        val selected = selectedElement ?: return
        if (selected.transform.locked) return
        updateElement(selected.id, resolve, recordHistory, mergeKey, transform)
    }

    fun updateElement(
        id: String,
        resolve: Boolean = false,
        recordHistory: Boolean = true,
        mergeKey: String? = null,
        transform: (com.shifenmiao.app.watermarking.domain.model.WatermarkElement) ->
        com.shifenmiao.app.watermarking.domain.model.WatermarkElement,
    ) {
        val newDocument = document.copy(
            elements = document.elements.map { element ->
                if (element.id == id) transform(element) else element
            }
        )
        if (recordHistory) editSession.apply(newDocument, mergeKey) else editSession.updateTransient(newDocument)
        publishDocument(resolve)
    }

    fun beginCompoundEdit() = editSession.beginCompoundEdit()

    fun endCompoundEdit() {
        editSession.endCompoundEdit()
        updateHistoryFlags()
        registerChanges()
    }

    fun removeSelectedElement() {
        val selected = selectedElement ?: return
        val elements = document.elements.filterNot { it.id == selected.id }
        applyDocument(document.copy(elements = elements))
        _selectedElementId.update { elements.lastOrNull()?.id }
    }

    fun moveSelectedElement(offset: Int) {
        val selected = selectedElement ?: return
        if (selected.transform.locked) return
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
        activePresetDocument = preset.document
        _activePresetId.update { preset.id }
        if (preset.id == BuiltinTemplateId.PRIVACY_REDACTION.id) {
            privacy.activate(selectedUri.toString(), internalBitmap)
        } else {
            privacy.deactivate()
        }
        applyPresetDocument(preset.document)
    }

    fun disablePrivacyMode() {
        privacy.deactivate()
    }

    private fun applyPresetDocument(document: WatermarkDocument) {
        applyDocument(document)
        _selectedElementId.update { document.elements.lastOrNull()?.id }
    }

    /** 引导页「空白」模板：清掉预设关联，后续重置回到空画布而不是上一个预设。 */
    fun applyBlankDocument() {
        activePresetDocument = null
        _activePresetId.update { null }
        applyPresetDocument(WatermarkDocument(elements = emptyList()))
    }

    /** 快捷滤镜栏：选中一项即替换当前滤镜；「更多」目录可继续叠加。 */
    fun selectFilter(filter: UiFilter<*>?) {
        setBaseFilters(filter?.let(::listOf).orEmpty())
    }

    fun addFilter(filter: UiFilter<*>) {
        setBaseFilters(baseFilters + filter)
    }

    fun filterTransformation(filter: UiFilter<*>): Transformation<Bitmap> =
        filterProvider.filterToTransformation(filter)

    private fun setBaseFilters(filters: List<UiFilter<*>>) {
        if (_baseFilters.value == filters) return
        _baseFilters.value = filters
        registerChanges()
        refreshFilteredPreview()
    }

    private fun refreshFilteredPreview() {
        filterJob?.cancel()
        val base = internalBitmap
        if (base == null || baseFilters.isEmpty()) {
            // 预览位图可能仍被画布持有，交给 GC 回收，不能在这里 recycle。
            _filteredBitmap.value = null
            return
        }
        val requestedFilters = baseFilters
        filterJob = componentScope.launch {
            val result = try {
                applyFiltersTo(base, requestedFilters)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppToastHost.showFailureToast(R.string.watermark_filter_unsupported)
                null
            }
            ensureActive()
            val stale = _internalBitmap.value !== base || _baseFilters.value != requestedFilters
            if (result == null || stale || result === base) {
                if (stale && result !== base) result?.recycle()
                if (result === base) _filteredBitmap.value = null
                return@launch
            }
            _filteredBitmap.value = result
        }
    }

    /**
     * 滤镜按顺序作用到底图，每一步都基于副本，避免变换改写原图；
     * 几何尺寸变化的滤镜在这里拒绝，否则水印坐标与导出尺寸会错位。
     */
    private suspend fun applyFiltersTo(
        image: Bitmap,
        filters: List<UiFilter<*>>,
    ): Bitmap = withContext(defaultDispatcher) {
        var current = image
        var currentOwned = false
        try {
            for (filter in filters) {
                ensureActive()
                val transformation = filterProvider.filterToTransformation(filter)
                check(transformation !is EmptyTransformation<*> || !filter.isVisible) {
                    "Unsupported watermark filter: ${filter::class.simpleName}"
                }
                val input = current.copy(Bitmap.Config.ARGB_8888, true)
                var output: Bitmap? = null
                try {
                    output = transformation.transform(input, IntegerSize(input.width, input.height))
                    check(!output.isRecycled) { "Filter returned a recycled bitmap" }
                    check(output.width == image.width && output.height == image.height) {
                        "Watermark filters must preserve image dimensions"
                    }
                } catch (failure: Exception) {
                    if (output !== input) output?.recycle()
                    input.recycle()
                    throw failure
                }
                val returnedInput = output === input
                if (currentOwned) current.recycle()
                current = output
                currentOwned = true
                if (!returnedInput) input.recycle()
            }
            current
        } catch (failure: Exception) {
            if (currentOwned && current !== image) current.recycle()
            throw failure
        }
    }

    fun saveCurrentAsPreset(name: String, onResult: (Boolean) -> Unit = {}) {
        if (name.isBlank()) {
            onResult(false)
            return
        }
        componentScope.launch {
            val preset = WatermarkPreset(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                document = document,
            )
            val saved = runCatching { presetStore.savePreset(preset) }
                .onSuccess { stored ->
                    _customPresets.update { current -> listOf(stored) + current }
                }
                .isSuccess
            onResult(saved)
        }
    }

    fun deleteCustomPreset(id: String, onResult: (Boolean) -> Unit = {}) {
        componentScope.launch {
            val deleted = runCatching { presetStore.deletePreset(id) }
                .onSuccess { _customPresets.update { current -> current.filterNot { it.id == id } } }
                .isSuccess
            onResult(deleted)
        }
    }

    fun undo() {
        if (privacy.isActive) privacy.undo()
        else if (editSession.undo()) publishDocument(resolve = true)
    }

    fun redo() {
        if (privacy.isActive) privacy.redo()
        else if (editSession.redo()) publishDocument(resolve = true)
    }

    fun resetDocument() {
        setBaseFilters(emptyList())
        val initialPreset = activePresetDocument
        if (initialPreset != null && !privacy.isActive) {
            applyPresetDocument(initialPreset)
            return
        }
        privacy.clearCurrent()
        applyPresetDocument(WatermarkDocument(elements = emptyList()))
    }

    fun updateHiddenWatermark(params: HiddenWatermarkParams) {
        applyDocument(document.copy(hiddenWatermark = params))
    }

    fun refreshDynamicData() {
        componentScope.launch {
            _isDynamicDataLoading.update { true }
            _locationLoadFailed.update { false }
            try {
                val location = try {
                    withContext(ioDispatcher) { currentLocationProvider.currentLocation() }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    null
                }
                deviceLocation = location?.toDomain()
                _locationLoadFailed.update { location == null }
                resolveCurrentDocument()
            } finally {
                _isDynamicDataLoading.update { false }
            }
        }
    }

    private fun applyDocument(document: WatermarkDocument) {
        editSession.apply(document)
        publishDocument(resolve = true)
    }

    private fun publishDocument(resolve: Boolean) {
        val current = documentFromSession()
        _document.update { current }
        // 解析是异步的:这里只把 element 同步成最新值,绝不拿旧快照覆盖刚拖动过的变换。
        _resolvedElements.update { previous ->
            val resolvedById = previous.associateBy { it.element.id }
            current.elements.map { element ->
                resolvedById[element.id]?.copy(element = element) ?: element.staticResolution()
            }
        }
        updateHistoryFlags()
        registerChanges()
        if (resolve) resolveCurrentDocument()
    }

    private fun documentFromSession(): WatermarkDocument = editSession.document

    private fun updateHistoryFlags() {
        _canUndo.update { if (privacy.isActive) privacy.canUndo else editSession.canUndo }
        _canRedo.update { if (privacy.isActive) privacy.canRedo else editSession.canRedo }
    }

    private fun resolveCurrentDocument() {
        val uri = selectedUri.takeIf { it != Uri.EMPTY } ?: return
        resolutionJob?.cancel()
        val requestedDocument = documentFromSession()
        val requestedLocation = deviceLocation
        resolutionJob = componentScope.launch {
            _isResolving.update { true }
            try {
                val resolution = try {
                    withContext(defaultDispatcher) {
                        contentResolver.resolve(uri.toString(), requestedDocument, requestedLocation)
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    null
                }
                ensureActive()
                if (resolution != null) {
                    applyResolution(resolution)
                    _needsDeviceLocation.update { resolution.needsDeviceLocation }
                    _weatherAvailable.update { resolution.weatherAvailable }
                }
            } finally {
                _isResolving.update { false }
            }
        }
    }

    /** 解析结果只提供文本/图片数据,图层本身一律以当前文档为准。 */
    private fun applyResolution(resolution: WatermarkResolution) {
        val current = documentFromSession()
        val incomingById = resolution.elements.associateBy { it.element.id }
        _resolvedElements.update { previous ->
            val previousById = previous.associateBy { it.element.id }
            current.elements.map { element ->
                val incoming = incomingById[element.id]
                when {
                    incoming != null -> incoming.copy(element = element)
                    previousById[element.id] != null -> previousById[element.id]!!.copy(element = element)
                    else -> element.staticResolution()
                }
            }
        }
    }

    suspend fun createPreviewVisuals(
        canvasWidth: Int,
        canvasHeight: Int,
        bandHeight: Int,
    ): Map<String, WatermarkVisual<Bitmap>> = buildMap {
        // bandHeight 由预览画布按「源图带高 × fit」算出，与导出 sizeBasisWidth 同源；
        // BAND 元素字号不随原图宽高比漂移，PHOTO 元素仍以整张画布宽为基准。
        val bandBasis = bandHeight.coerceAtLeast(1)
        resolvedElements.forEach { resolved ->
            if (!resolved.element.transform.visible) return@forEach
            // 没有真实数据时在编辑器里画淡色格式占位，模板版式完整、图层仍可拖动。
            // 只替换预览用的副本；导出走 contentResolver 的原始 resolution，不经过这里。
            val previewResolved = resolved.forPreview()
            // 预设里保存的本地图片可能已被清理,单个图层失败不能拖垮整个预览。
            val sizeBasis = if (resolved.element.anchor == WatermarkAnchor.BAND) {
                bandBasis
            } else {
                canvasWidth
            }
            val visual = try {
                visualFactory.create(previewResolved, canvasWidth, canvasHeight, sizeBasis)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                null
            }
            visual?.let { put(resolved.element.id, it) }
        }
    }

    /**
     * 只对「动态内容且没有真值」的图层做占位替换。
     * 静态文本 / 图片未选图的占位块 / 分隔线一律保持原行为，不受影响。
     */
    private fun ResolvedWatermarkElement.forPreview(): ResolvedWatermarkElement {
        val hasRealValue = hasData && !text.isNullOrBlank() && text != "\u2014"
        if (hasRealValue) return this
        val placeholder = element.content.previewPlaceholder() ?: return this
        return copy(
            element = element.copy(
                style = element.style.copy(
                    color = element.style.color.asPlaceholderColor(),
                    backgroundColor = 0,
                ),
            ),
            text = placeholder,
        )
    }

    fun setUris(
        uris: List<Uri>,
        onFailure: (Throwable) -> Unit = {},
    ) {
        _uris.update { uris }
        if (uris.isEmpty()) {
            _selectedUri.update { Uri.EMPTY }
            _internalBitmap.update { null }
            filterJob?.cancel()
            _filteredBitmap.value = null
            _resolvedElements.update { emptyList() }
            privacy.selectImage("", null)
        } else {
            // 追加图片不应把正在编辑的那张踢回列表第一张,也不必重新解码同一张图。
            val keepCurrent = selectedUri != Uri.EMPTY &&
                uris.contains(selectedUri) &&
                internalBitmap != null
            if (!keepCurrent) updateSelectedUri(uris.first(), onFailure)
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
            refreshFilteredPreview()
            privacy.selectImage(uri.toString(), internalBitmap)
            _imageFormat.update { imageData.imageInfo.imageFormat }
            _currentHiddenWatermark.update { hiddenWatermarkDetector.detect(imageData.image) }
            _isImageLoading.update { false }
            resolveCurrentDocument()
        }
    }

    fun updateUrisSilently(removedUri: Uri) {
        val remaining = uris.filterNot { it == removedUri }
        _uris.update { remaining }
        privacy.removeImage(removedUri.toString())
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
        val filtered = try {
            applyFiltersTo(imageData.image, baseFilters)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            // 预览已在选滤镜时提示过；导出不因单个滤镜失败而整批中断。
            imageData.image
        }
        val redacted = privacy.render(filtered, uri.toString())
        val bitmap = try {
            watermarkRenderer.render(
                image = redacted,
                resolution = resolution,
                hiddenWatermark = documentFromSession().hiddenWatermark,
            )
        } finally {
            if (redacted !== filtered) redacted.recycle()
            if (filtered !== imageData.image) filtered.recycle()
        } ?: return null
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
            val targets = if (onlyCurrent || privacy.isActive) listOf(selectedUri) else uris
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { targets.size }
            val results = mutableListOf<SaveResult>()
            targets.forEachIndexed { index, uri ->
                ensureActive()
                val rendered = try {
                    renderUri(uri)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    results.add(SaveResult.Error.Exception(error))
                    _done.update { it + 1 }
                    return@forEachIndexed
                }
                if (rendered == null) {
                    results.add(
                        SaveResult.Error.Exception(
                            IllegalStateException("Watermark rendering produced no image for $uri")
                        )
                    )
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

    fun shareBitmaps(onComplete: () -> Unit, onlyCurrent: Boolean = true) {
        val targets = if (onlyCurrent || privacy.isActive) listOf(selectedUri) else uris
        if (targets.isEmpty() || targets.first() == Uri.EMPTY) return
        savingJob = componentScope.launch {
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { targets.size }
            shareProvider.shareImages(
                uris = targets.map(Uri::toString),
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
            val targets = if (privacy.isActive) listOf(selectedUri) else uris
            _isSaving.update { true }
            _done.update { 0 }
            _left.update { targets.size }
            val cached = mutableListOf<Uri>()
            targets.forEach { uri ->
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
        if (uris.size == 1 || privacy.isActive) imageFormat else null

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
            presetId: String?,
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

private fun defaultContent(kind: WatermarkKind, label: String = ""): WatermarkContent = when (kind) {
    WatermarkKind.TEXT -> WatermarkContent.Text(label.ifBlank { "Watermark" })
    WatermarkKind.IMAGE -> WatermarkContent.Image()
    WatermarkKind.STAMP -> WatermarkContent.Stamp(label.ifBlank { "Stamp" })
    WatermarkKind.TIMESTAMP -> WatermarkContent.Timestamp()
    WatermarkKind.ALTITUDE -> WatermarkContent.Altitude()
    WatermarkKind.WEATHER -> WatermarkContent.Weather()
    WatermarkKind.COORDINATES -> WatermarkContent.Coordinates()
    WatermarkKind.CAMERA_PARAMS -> WatermarkContent.CameraParams()
    WatermarkKind.DIVIDER -> WatermarkContent.Divider()
}
