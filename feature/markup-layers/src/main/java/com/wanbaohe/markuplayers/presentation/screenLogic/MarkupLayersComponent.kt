package com.wanbaohe.markuplayers.presentation.screenLogic

import android.content.ContentResolver
import android.graphics.Bitmap
import android.os.Build
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.common.recent.RecentAccessRepository
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.network.repository.BaiduImageProcessRepository
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoader
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageScaler
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.filters.domain.FilterProvider
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.widget.FilterTemplateCreationSheetComponent
import com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters.AddFiltersSheetComponent
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import com.t8rin.imagetoolbox.core.utils.filename
import com.t8rin.logger.makeLog
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.data.render.DrawLayerExportRenderer
import com.wanbaohe.markuplayers.data.render.STICKER_EXPORT_BASE_RATIO
import com.wanbaohe.markuplayers.data.render.StickerLayerExportRenderer
import com.wanbaohe.markuplayers.data.render.LayerBitmapLoader
import com.wanbaohe.markuplayers.data.render.LayerEffectsProcessor
import com.wanbaohe.markuplayers.data.image.StudioImageReader
import com.wanbaohe.markuplayers.data.image.BaseImageTransformer
import com.wanbaohe.markuplayers.data.recipe.MarkupExportEncoder
import com.wanbaohe.markuplayers.data.recipe.MarkupBatchProcessor
import com.wanbaohe.markuplayers.data.project.MarkupProjectStore
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import com.wanbaohe.markuplayers.domain.history.LayerHistory
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.domain.project.MarkupProjectSummary
import com.wanbaohe.markuplayers.domain.recipe.RecipeStore
import com.wanbaohe.markuplayers.domain.recipe.MarkupRecipe
import com.wanbaohe.markuplayers.domain.recipe.BatchProgress
import com.wanbaohe.markuplayers.domain.recipe.BatchReport
import com.wanbaohe.markuplayers.domain.recipe.MarkupBatchCancelledException
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import com.wanbaohe.markuplayers.domain.render.ImageRenderLimitException
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.wanbaohe.markuplayers.domain.model.AiImageOp
import com.wanbaohe.markuplayers.domain.model.AiGenerationTarget
import com.wanbaohe.markuplayers.domain.model.AiImageResult
import com.wanbaohe.markuplayers.domain.model.BaseTransformSpec
import com.wanbaohe.markuplayers.domain.model.LayerBaseRemap
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.domain.model.isBitmapFilterable
import com.wanbaohe.markuplayers.domain.model.toImageProcessRect
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.export.ExportResolution
import com.wanbaohe.markuplayers.presentation.export.toExportFormat
import com.wanbaohe.markuplayers.presentation.editor.CanvasBackground
import com.wanbaohe.markuplayers.presentation.render.IMAGE_LAYER_BASE_WIDTH_RATIO
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 图层创作页面组件。
 *
 * 图层列表即唯一事实来源,所有修改操作(增/删/改/排序)都先向 [history]
 * 记录快照再变更,undo/redo 走 [LayerHistory] 的整体快照恢复。
 * 保存/分享不再走 Compose 截图,而是取全分辨率原图交由
 * [markupLayersApplier] 按原图尺寸重绘。
 */
class MarkupLayersComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialUri: Uri?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val markupLayersApplier: MarkupLayersApplier<Bitmap>,
    private val drawLayerExportRenderer: DrawLayerExportRenderer,
    private val stickerLayerExportRenderer: StickerLayerExportRenderer,
    private val imageProcessRepository: BaiduImageProcessRepository,
    private val imageGenerationLoader: ImageGenerationLoader,
    private val imageGenerationManager: ImageGenerationManager,
    private val filterProvider: FilterProvider<Bitmap>,
    private val recentAccessRepository: RecentAccessRepository,
    private val projectStore: MarkupProjectStore,
    private val studioImageReader: StudioImageReader,
    private val memoryBudget: ImageMemoryBudget,
    private val effectsProcessor: LayerEffectsProcessor,
    private val layerBitmapLoader: LayerBitmapLoader,
    private val baseImageTransformer: BaseImageTransformer,
    private val exportEncoder: MarkupExportEncoder,
    private val recipeStore: RecipeStore,
    private val batchProcessor: MarkupBatchProcessor,
    addFiltersSheetComponentFactory: AddFiltersSheetComponent.Factory,
    filterTemplateCreationSheetComponentFactory: FilterTemplateCreationSheetComponent.Factory
) : BaseComponent(dispatchersHolder, componentContext) {

    // AddFiltersSheet 子组件:随本组件创建,生命周期一致
    val addFiltersSheetComponent: AddFiltersSheetComponent = addFiltersSheetComponentFactory(
        componentContext = componentContext.childContext(key = "markupAddFiltersSheet")
    )

    val filterTemplateCreationSheetComponent: FilterTemplateCreationSheetComponent =
        filterTemplateCreationSheetComponentFactory(
            componentContext = componentContext.childContext(
                key = "markupFilterTemplateCreationSheet"
            )
        )

    init {
        debounce {
            observeRecentProjects()
            observeProjects()
            refreshRecipes()
            componentScope.launch {
                imageGenerationManager.observeActiveConfig().collect { config ->
                    _canAiEdit.value = imageGenerationManager.getProviderDescriptors()
                        .firstOrNull { it.providerId == config?.providerId }?.supportsEditing == true
                }
            }
            lifecycle.doOnStop {
                if (hasImage && haveChanges) saveProject()
            }
            initialUri?.let {
                setUri(
                    uri = it,
                    onFailure = AppToastHost::showFailureToast
                )
            }
        }
    }

    private val documentHistory = LayerHistory()
    private val history = object {
        val canUndo get() = documentHistory.canUndo
        val canRedo get() = documentHistory.canRedo
        fun snapshot(layers: List<MarkupLayer>) {
            if (isCompoundEdit) return
            endEffectsChange()
            endLayerTransformChange()
            documentHistory.snapshot(captureDocument().copy(layers = layers))
            refreshHistoryFlags()
        }
        fun clear() = documentHistory.clear()
    }

    private var projectId: String? = null
    private val _projectName = mutableStateOf("")
    val projectName: String by _projectName
    private val _projects = mutableStateOf<List<MarkupProjectSummary>>(emptyList())
    val projects: List<MarkupProjectSummary> by _projects
    private val _isProjectSaving = mutableStateOf(false)
    val isProjectSaving: Boolean by _isProjectSaving
    private val _projectSaveError = mutableStateOf<String?>(null)
    val projectSaveError: String? by _projectSaveError
    private var documentRevision = 0L
    private var autosaveJob: Job? = null
    private val projectWriteMutex = Mutex()
    private val _originalUri = mutableStateOf<String?>(null)
    private val _sourceUri = mutableStateOf<String?>(null)
    private val _baseTransforms = mutableStateOf<List<BaseTransformSpec>>(emptyList())
    private val _baseImageHistory = mutableStateOf<List<String>>(emptyList())
    // 背景(底图)可见性与 z 序位置,随文档快照进 undo 历史
    private val _baseVisible = mutableStateOf(true)
    val baseVisible: Boolean by _baseVisible
    private val _baseOrder = mutableStateOf(0)
    val baseOrder: Int by _baseOrder
    private val _aiGenerationTarget = mutableStateOf(AiGenerationTarget.NewLayer)
    val aiGenerationTarget: AiGenerationTarget by _aiGenerationTarget
    private val _canAiEdit = mutableStateOf(false)
    val canAiEdit: Boolean by _canAiEdit
    private val _pendingAiResult = mutableStateOf<AiImageResult?>(null)
    val pendingAiResult: AiImageResult? by _pendingAiResult
    val aiEditImageUri: String?
        get() = when (aiGenerationTarget) {
            AiGenerationTarget.NewLayer -> null
            AiGenerationTarget.SelectedLayer -> (selectedLayer?.type as? LayerType.Image)
                ?.imageData?.toString()
            AiGenerationTarget.Background -> _uri.value?.toString()
        }
    val aiImageHistory: List<String>
        get() = when (aiGenerationTarget) {
            AiGenerationTarget.NewLayer -> emptyList()
            AiGenerationTarget.SelectedLayer -> selectedLayer?.imageHistory.orEmpty()
            AiGenerationTarget.Background -> _baseImageHistory.value
        }
    val originalSourceUri: Uri? get() = _sourceUri.value?.toUri()
    val canOverwriteOriginal: Boolean
        get() = _sourceUri.value?.let { source ->
            !source.startsWith(appContext.filesDir.absolutePath) &&
                !source.startsWith(appContext.cacheDir.absolutePath) &&
                !source.contains(appContext.fileProviderAuthority)
        } == true
    private var imageLoadingJob: Job? = null
    private var taskGeneration = 0L
    private val _taskLabel = mutableStateOf<String?>(null)
    val taskLabel: String? by _taskLabel
    private val _selectedLayerIds = mutableStateOf<Set<String>>(emptySet())
    val selectedLayerIds: Set<String> by _selectedLayerIds
    private var layerTransformTransactionActive = false
    private var isCompoundEdit = false
    private val _maskTargetBitmap = mutableStateOf<Bitmap?>(null)
    val maskTargetBitmap: Bitmap? by _maskTargetBitmap
    private val _isComparing = mutableStateOf(false)
    val isComparing: Boolean by _isComparing
    private val _originalPreviewBitmap = mutableStateOf<Bitmap?>(null)
    val originalPreviewBitmap: Bitmap? by _originalPreviewBitmap
    private val _compositePreview = mutableStateOf<Bitmap?>(null)
    val compositePreviewBitmap: Bitmap? by _compositePreview
    private var compositePreviewJob: Job? = null
    private var compositePreviewRevision = -1L
    private val _recipes = mutableStateOf<List<MarkupRecipe>>(emptyList())
    val recipes: List<MarkupRecipe> by _recipes
    private val _recipeError = mutableStateOf<String?>(null)
    val recipeError: String? by _recipeError
    private val _batchProgress = mutableStateOf<BatchProgress?>(null)
    val batchProgress: BatchProgress? by _batchProgress
    private val _batchReport = mutableStateOf<BatchReport?>(null)
    val batchReport: BatchReport? by _batchReport
    private val _batchImageCount = mutableIntStateOf(0)
    val batchImageCount: Int by _batchImageCount
    private var batchImages: List<String> = emptyList()
    private var batchJob: Job? = null

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _uri: MutableState<Uri?> = mutableStateOf(null)
    val uri: Uri? by _uri

    // 空白画布底图:无 uri 场景下的全分辨率底图,随 setUri/resetState/applyBaseTransform 清理
    private val _blankBaseBitmap: MutableState<Bitmap?> = mutableStateOf(null)

    val hasImage: Boolean get() = _bitmap.value != null

    private val _layers: MutableState<List<MarkupLayer>> = mutableStateOf(emptyList())
    val layers: List<MarkupLayer> by _layers

    private val _selectedLayerId: MutableState<String?> = mutableStateOf(null)
    val selectedLayerId: String? by _selectedLayerId

    /** 首页快捷任务:图片打开后要直接执行的 AI 操作,UI 消费一次即清除 */
    private val _pendingAiOp = mutableStateOf<AiImageOp?>(null)
    val pendingAiOp: AiImageOp? by _pendingAiOp

    fun requestAiOpOnOpen(op: AiImageOp?) {
        _pendingAiOp.value = op
    }

    fun consumePendingAiOp() {
        _pendingAiOp.value = null
    }

    private val _activeToolId: MutableState<String?> = mutableStateOf(null)
    val activeToolId: String? by _activeToolId

    private val _imageFormat = mutableStateOf(ImageFormat.Default)
    val imageFormat by _imageFormat

    private val _saveExif: MutableState<Boolean> = mutableStateOf(false)
    val saveExif: Boolean by _saveExif

    // 导出设置(格式/质量/分辨率/导出后分享),由导出设置面板编辑
    private val _exportSettings = mutableStateOf(ExportSettings())
    val exportSettings by _exportSettings

    // 原图全分辨率尺寸(供导出面板展示各档位实际像素)
    private val _sourceSize: MutableState<IntSize?> = mutableStateOf(null)
    val sourceSize: IntSize? by _sourceSize

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _canUndo: MutableState<Boolean> = mutableStateOf(false)
    val canUndo: Boolean by _canUndo

    private val _canRedo: MutableState<Boolean> = mutableStateOf(false)
    val canRedo: Boolean by _canRedo

    private var savingJob: Job? = null
    private var mergeJob: Job? = null
    private var baseTransformJob: Job? = null

    // ---------------- 基础调节(亮度/对比度/饱和度) ----------------

    /**
     * 调节/滤镜的作用目标:当前选中图层;未选中任何图层时 = 背景图。
     * 调节(亮度/对比度/饱和度)所有图层类型与背景图都支持,实时生效;
     * 滤镜仅位图类图层([isBitmapFilterable])与背景图支持,文字/形状需先光栅化。
     */
    val selectedLayer: MarkupLayer?
        get() = _layers.value.firstOrNull { it.id == _selectedLayerId.value }

    /** 当前目标的调节参数(选中图层 → 图层自身;否则背景图) */
    val targetAdjustments: BaseAdjustments
        get() = selectedLayer?.adjustments ?: _baseAdjustments.value

    /** 当前目标的滤镜(选中图层 → 图层自身;否则背景图) */
    val targetFilter: UiFilter<*>?
        get() = targetEffects.lastOrNull()?.filter

    val targetEffects: List<LayerEffect>
        get() = selectedLayer?.effectiveEffects ?: _baseEffects.value

    val targetMask: LayerMask?
        get() {
            val target = selectedLayer
            return if (target == null) _baseMask.value else target.mask
        }

    val canMaskTarget: Boolean
        get() = selectedLayer?.let { it.type.isBitmapFilterable && !it.transform.locked } ?: hasImage

    private val _baseEffects = mutableStateOf<List<LayerEffect>>(emptyList())
    private val _baseMask = mutableStateOf<LayerMask?>(null)

    /** 当前选中且需先光栅化才能用滤镜的图层(文字/形状);无或其他类型为 null */
    val rasterizeRequiredLayer: MarkupLayer?
        get() = selectedLayer?.takeIf { !it.type.isBitmapFilterable }

    /**
     * 背景图的调节参数;预览经底图 Picture 的 colorFilter
     * 实时生效(仅底图),导出时烘焙进全分辨率底图。选中图层时调节走图层字段
     * (见 [updateBaseAdjustments] 分流),本状态保持不变。
     */
    private val _baseAdjustments: MutableState<BaseAdjustments> = mutableStateOf(BaseAdjustments())
    val baseAdjustments: BaseAdjustments
        get() = if (isComparing) BaseAdjustments() else _baseAdjustments.value

    /**
     * 更新调节参数,按目标分流:选中图层 → 写入图层 adjustments(transient,
     * 拖动开始的快照由 [beginAdjustmentsChange] 记录,整段拖动 = 一步 undo);
     * 无选中 → 写背景图状态(拖动起点的整文档快照同样覆盖它,一步 undo 回退)。
     */
    fun updateBaseAdjustments(adjustments: BaseAdjustments) {
        val target = selectedLayer
        if (target != null) {
            updateLayerAdjustmentsTransient(target.id, adjustments)
            return
        }
        if (adjustments == _baseAdjustments.value) return
        _baseAdjustments.value = adjustments
        registerDocumentChange()
    }

    /**
     * 调节拖动开始:记一次整文档历史快照(配合 [updateBaseAdjustments]
     * 的 transient 写入,整段滑杆拖动 = 一步 undo;目标为背景图时同样覆盖)。
     */
    fun beginAdjustmentsChange() {
        history.snapshot(_layers.value)
        refreshHistoryFlags()
    }

    /** 图层调节拖动中:直接改值不入历史(快照已在 [beginAdjustmentsChange] 记录) */
    private fun updateLayerAdjustmentsTransient(
        id: String,
        adjustments: BaseAdjustments
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list ->
            list.map { if (it.id == id) it.copy(adjustments = adjustments) else it }
        }
        registerDocumentChange()
    }

    /** 重置当前目标的调节:图层走正常 updateLayer(入历史),背景图直接清零 */
    fun resetBaseAdjustments() {
        val target = selectedLayer
        if (target != null) {
            if (target.adjustments.isNeutral) return
            updateLayer(target.id) { it.copy(adjustments = BaseAdjustments()) }
        } else {
            if (_baseAdjustments.value.isNeutral) return
            history.snapshot(_layers.value)
            updateBaseAdjustments(BaseAdjustments())
        }
    }

    // ---------------- 滤镜(目标分流:选中位图类图层 → 图层;无选中 → 背景图) ----------------

    private val _selectedFilter: MutableState<UiFilter<*>?> = mutableStateOf(null)
    val selectedFilter: UiFilter<*>? get() = _baseEffects.value.lastOrNull()?.filter

    // 背景滤镜预览缓存(仅底图):对展示底图应用滤镜后的结果,随底图/滤镜变化异步重算
    private val _filterPreviewBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val filterPreviewBitmap: Bitmap? by _filterPreviewBitmap

    private var filterPreviewJob: Job? by smartJob()

    /**
     * 图层滤镜位图缓存:layerId → 过滤后的图层内容位图(按底图全分辨率预算,
     * 预览缩放显示、导出直接复用,保证所见即所得且不重复过滤大图)。
     * 有效性按「内容(type)+ 滤镜」判定,任一变化即失效重算(见 [syncLayerFilterCache])。
     */
    private class LayerFilterCacheEntry(
        val type: LayerType,
        val effects: List<LayerEffect>,
        val mask: LayerMask?,
        val size: IntegerSize,
        val bitmap: Bitmap,
    )

    private val layerFilterCache = mutableStateMapOf<String, LayerFilterCacheEntry>()

    private var layerFilterJob: Job? by smartJob()
    private var effectsChangeJob: Job? = null
    private var effectsTransactionActive = false

    /** 画布展示用底图(仅底图,不含图层):背景滤镜预览优先,背景调节经底图 colorFilter 叠加 */
    val displayBitmap: Bitmap?
        get() = if (_isComparing.value) _originalPreviewBitmap.value
        else _filterPreviewBitmap.value ?: _bitmap.value

    /**
     * 选择滤镜,按目标分流:选中位图类图层 → 写入图层 filter(经 [updateLayer] 入 undo
     * 历史);选中文字/形状图层时不直接赋值(应由 UI 先走光栅化确认,见
     * [rasterizeRequiredLayer]/[rasterizeLayer]),但清除滤镜(filter = null)仍然允许;
     * 无选中 → 写背景图滤镜状态(随整文档快照进 undo)。
     */
    fun selectFilter(filter: UiFilter<*>?) {
        setTargetEffects(filter?.let { listOf(LayerEffect(it)) }.orEmpty())
    }

    fun addEffect(filter: UiFilter<*>) {
        setTargetEffects(targetEffects + LayerEffect(filter))
    }

    fun removeEffect(index: Int) {
        require(index in targetEffects.indices) { "Effect index is out of bounds" }
        setTargetEffects(targetEffects.filterIndexed { position, _ -> position != index })
    }

    fun moveEffect(index: Int, delta: Int) {
        val effects = targetEffects
        val destination = index + delta
        require(index in effects.indices && destination in effects.indices) {
            "Effect position is out of bounds"
        }
        setTargetEffects(effects.toMutableList().apply {
            add(destination, removeAt(index))
        })
    }

    fun replaceEffect(index: Int, filter: UiFilter<*>) {
        require(index in targetEffects.indices) { "Effect index is out of bounds" }
        beginEffectsChange()
        writeTargetEffects(targetEffects.mapIndexed { position, effect ->
            if (position == index) effect.copy(filter = filter) else effect
        })
        finishEffectsChangeAfterIdle()
    }

    fun beginEffectsChange() {
        if (effectsTransactionActive) return
        history.snapshot(_layers.value)
        effectsTransactionActive = true
        refreshHistoryFlags()
    }

    fun endEffectsChange() {
        effectsChangeJob?.cancel()
        effectsChangeJob = null
        effectsTransactionActive = false
    }

    fun updateEffectStrengthTransient(index: Int, strength: Float) {
        require(index in targetEffects.indices && strength.isFinite() && strength in 0f..1f) {
            "Invalid effect strength"
        }
        beginEffectsChange()
        writeTargetEffects(targetEffects.mapIndexed { position, effect ->
            if (position == index) effect.copy(strength = strength) else effect
        })
        finishEffectsChangeAfterIdle()
    }

    private fun finishEffectsChangeAfterIdle() {
        effectsChangeJob?.cancel()
        effectsChangeJob = componentScope.launch {
            delay(500)
            effectsTransactionActive = false
        }
    }

    private fun setTargetEffects(effects: List<LayerEffect>) {
        if (effects == targetEffects) return
        if (selectedLayer?.type?.isBitmapFilterable == false && effects.isNotEmpty()) {
            AppToastHost.showFailureToast(R.string.markup_rasterize_message)
            return
        }
        endEffectsChange()
        history.snapshot(_layers.value)
        writeTargetEffects(effects)
    }

    private fun writeTargetEffects(effects: List<LayerEffect>) {
        val target = selectedLayer
        if (target != null) {
            _layers.update { layers ->
                layers.map {
                    if (it.id == target.id) it.copy(filter = null, effects = effects) else it
                }
            }
        } else {
            _baseEffects.value = effects
            _selectedFilter.value = effects.lastOrNull()?.filter
            updateFilterPreview()
        }
        onLayersChanged()
    }

    fun updateTargetMask(mask: LayerMask?) {
        if (!canMaskTarget) {
            AppToastHost.showFailureToast(R.string.studio_mask_target_required)
            return
        }
        if (mask == targetMask) return
        history.snapshot(_layers.value)
        val target = selectedLayer
        if (target == null) {
            _baseMask.value = mask
            updateFilterPreview()
        } else {
            _layers.update { list ->
                list.map { if (it.id == target.id) it.copy(mask = mask) else it }
            }
        }
        onLayersChanged()
    }

    fun prepareMaskTarget() {
        if (!canMaskTarget) {
            AppToastHost.showFailureToast(R.string.studio_mask_target_required)
            return
        }
        imageLoadingJob = launchImageTask(R.string.studio_task_mask, ImageTask.Loading) {
            val target = selectedLayer
            val size = _bitmap.value?.let { IntegerSize(it.width, it.height) }
                ?: error("No image is open")
            _maskTargetBitmap.value = when (val type = target?.type) {
                null -> studioImageReader.load(captureDocument().baseUri, size)
                is LayerType.Image -> imageGetter.getImage(data = type.imageData, size = size)
                is LayerType.Sticker -> stickerLayerExportRenderer.decodeBitmap(
                    type, size.width * STICKER_EXPORT_BASE_RATIO
                )
                is LayerType.Draw -> drawLayerExportRenderer.renderStrokesBitmap(
                    type.strokes, size.width, size.height
                )
                else -> error("This layer does not support a bitmap mask")
            }?.let(::softwareBitmap) ?: error("Could not load the mask target")
        }
    }

    fun setComparing(comparing: Boolean) {
        _isComparing.value = comparing
    }

    /** 蒙版选区预览位图(透明白色,alpha=选区),供蒙版工具 sheet 叠加显示 */
    suspend fun previewTargetMask(mask: LayerMask, width: Int, height: Int): Bitmap =
        effectsProcessor.maskAlphaPreview(mask, width, height)

    /** 滤镜 → 变换(供缩略图 coil transformation 与导出烘焙复用同一入口) */
    fun filterTransformation(filter: UiFilter<*>): Transformation<Bitmap> =
        filterProvider.filterToTransformation(filter)

    private fun updateFilterPreview() {
        val effects = _baseEffects.value
        val mask = _baseMask.value
        val base = _bitmap.value
        filterPreviewJob = null
        if ((effects.isEmpty() && mask == null) || base == null) {
            _filterPreviewBitmap.value = null
            return
        }
        filterPreviewJob = componentScope.launch {
            try {
                val filtered = effectsProcessor.apply(base, effects, mask)
                ensureActive()
                if (_bitmap.value === base && _baseEffects.value == effects && _baseMask.value == mask) {
                    _filterPreviewBitmap.value = filtered
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                reportProcessingFailure(failure)
            }
        }
    }

    private suspend fun Bitmap.withSelectedFilter(): Bitmap {
        return effectsProcessor.apply(this, _baseEffects.value, _baseMask.value)
    }

    /**
     * 取图层过滤后的内容位图(命中缓存且「内容+滤镜」均未变时返回),供预览渲染器使用;
     * 未命中返回 null,由 [syncLayerFilterCache] 异步补齐后触发重组。
     */
    fun layerFilteredBitmap(layer: MarkupLayer): Bitmap? {
        val entry = layerFilterCache[layer.id] ?: return null
        return entry
            .takeIf {
                it.effects == layer.effectiveEffects && it.mask == layer.mask && it.type == layer.type
            }
            ?.bitmap
    }

    /**
     * 同步图层滤镜缓存:剔除失效条目(图层删除/内容变更/滤镜变更),
     * 串行(smartJob)补齐缺失条目。图层提交级变更([onLayersChanged])统一走这里,
     * 按条目有效性精确失效,undo/redo 后旧值不匹配同样会按需重算。
     */
    private fun syncLayerFilterCache() {
        val layers = _layers.value
        val base = _bitmap.value ?: return
        val processedLayers = layers.count { it.effectiveEffects.isNotEmpty() || it.mask != null }
        val scale = sqrt(
            (LAYER_PREVIEW_CACHE_BYTES.toDouble() /
                (maxOf(1, processedLayers) * 4L * base.width * base.height)).coerceAtMost(1.0)
        )
        val previewSize = IntegerSize(
            (base.width * scale).roundToInt().coerceAtLeast(1),
            (base.height * scale).roundToInt().coerceAtLeast(1)
        )
        val byId = layers.associateBy { it.id }
        layerFilterCache.keys.toList().forEach { id ->
            val layer = byId[id]
            val entry = layerFilterCache[id]
            val valid = layer != null && entry != null &&
                entry.effects == layer.effectiveEffects && entry.mask == layer.mask &&
                entry.type == layer.type && entry.size == previewSize
            if (!valid) layerFilterCache.remove(id)
        }
        val pending = layers.filter { layer ->
            (layer.effectiveEffects.isNotEmpty() || layer.mask != null) &&
                layer.type.isBitmapFilterable &&
                layerFilteredBitmap(layer) == null
        }
        layerFilterJob = null
        if (pending.isEmpty()) return
        layerFilterJob = componentScope.launch {
            pending.forEach { layer ->
                try {
                    val bitmap = layerBitmapLoader.load(
                        layer, previewSize.width, previewSize.height
                    ) ?: error("Could not process the image layer")
                    ensureActive()
                    val current = _layers.value.firstOrNull { it.id == layer.id }
                    if (current?.type == layer.type &&
                        current.effectiveEffects == layer.effectiveEffects &&
                        current.mask == layer.mask
                    ) {
                        layerFilterCache[layer.id] = LayerFilterCacheEntry(
                            type = layer.type,
                            effects = layer.effectiveEffects,
                            mask = layer.mask,
                            size = previewSize,
                            bitmap = bitmap
                        )
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Exception) {
                    reportProcessingFailure(failure)
                }
            }
        }
    }

    // ---------------- 画布背景(操作台显示,会话级不持久化) ----------------

    /** 编辑器画布背景:默认(主题 surface 色)或纯色;仅影响画布显示,不参与导出,故不记 changes */
    private val _canvasBackground: MutableState<CanvasBackground> =
        mutableStateOf(CanvasBackground.Default)
    val canvasBackground: CanvasBackground by _canvasBackground

    fun setCanvasBackground(background: CanvasBackground) {
        if (_canvasBackground.value == background) return
        _canvasBackground.value = background
        registerDocumentChange()
    }

    // ---------------- 空白画布(无 uri 的内存底图) ----------------

    /**
     * 以纯色/透明底图开始创作:不产生 uri,预览/导出/分享全链路走内存底图
     * (见 [loadBaseBitmap])。[backgroundColor] 为 null 时透明,默认 PNG;
     * 否则以该颜色填充,默认 JPG。
     */
    fun startWithBlankCanvas(
        width: Int,
        height: Int,
        backgroundColor: Int?
    ) {
        if (isBusy) return
        _pendingAiOp.value = null
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            memoryBudget.requireCanRender(width, height)
            val transparent = backgroundColor == null
            val base = withContext(defaultDispatcher) {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    if (!transparent) eraseColor(backgroundColor)
                }
            }
            val path = projectStore.storeBitmap(base)
            val preview = studioImageReader.load(path, memoryBudget.previewSize(width, height))
            ensureActive()
            val format = if (transparent) ImageFormat.Png.Lossless else ImageFormat.Jpg
            beginProject(
                MarkupDocument(
                    baseUri = path,
                    width = width,
                    height = height,
                    layers = emptyList(),
                    originalUri = path,
                    exportSettings = ExportSettings(format = format)
                ),
                preview
            )
        }
    }

    private suspend fun loadBaseBitmap(): Bitmap? {
        val document = captureDocument()
        memoryBudget.requireCanRender(document.width, document.height)
        return studioImageReader.load(
            document.baseUri, IntegerSize(document.width, document.height)
        )
    }

    // ---------------- 裁剪 / 旋转 / 翻转(作用于底图) ----------------

    /**
     * 确认裁剪旋转变换:把 90° 步进 + 自由旋转 + 翻转 + 裁剪框合成一次变换,
     * 作用于全分辨率原图,缓存后替换底图(保留图层与历史,不走 [setUri])。
     *
     * 图层和蒙版按同一仿射变换重映射,底图资产与参数均记录到文档历史。
     */
    fun applyBaseTransform(
        rotationSteps: Int,
        freeRotation: Float,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
    ) {
        if (isBusy) return
        val oldSize = _sourceSize.value ?: return
        val steps = ((rotationSteps % 4) + 4) % 4
        val isIdentity = steps == 0 && freeRotation == 0f &&
            !flipHorizontal && !flipVertical && cropRect == NormalizedRect.Full
        if (isIdentity) return

        baseTransformJob = launchImageTask(
            R.string.studio_task_transforming, ImageTask.Saving
        ) {
            require(freeRotation.isFinite()) { "Invalid rotation" }
            val source = loadBaseBitmap() ?: error("No image is open")
            val result = baseImageTransformer.apply(
                source,
                BaseTransformSpec(
                    rotationSteps = steps,
                    freeRotation = freeRotation,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical,
                    cropRect = cropRect
                )
            )
            val path = projectStore.storeBitmap(result)
            val preview = studioImageReader.load(
                path, memoryBudget.previewSize(result.width, result.height)
            )
            val transformedLayers = LayerBaseRemap.remap(
                layers = _layers.value,
                oldWidth = oldSize.width,
                oldHeight = oldSize.height,
                rotationSteps = steps,
                flipHorizontal = flipHorizontal,
                flipVertical = flipVertical,
                cropRect = cropRect,
                freeRotation = freeRotation,
                outputWidth = result.width,
                outputHeight = result.height
            )
            val transformedMask = _baseMask.value?.let {
                LayerBaseRemap.remapMask(
                    mask = it,
                    oldWidth = oldSize.width,
                    oldHeight = oldSize.height,
                    rotationSteps = steps,
                    freeRotation = freeRotation,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical,
                    cropRect = cropRect,
                    outputWidth = result.width,
                    outputHeight = result.height
                )
            }
            ensureActive()
            history.snapshot(_layers.value)
            _uri.value = path.toUri()
            _blankBaseBitmap.value = null
            _sourceSize.value = IntSize(result.width, result.height)
            _imageFormat.update { ImageFormat.Png.Lossless }
            _exportSettings.update {
                it.copy(format = ImageFormat.Png.Lossless.toExportFormat())
            }
            _layers.value = transformedLayers
            _baseMask.value = transformedMask
            _baseTransforms.update {
                it + BaseTransformSpec(
                    rotationSteps = steps,
                    freeRotation = freeRotation,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical,
                    cropRect = cropRect
                )
            }
            _selectedLayerId.value = null
            _selectedLayerIds.value = emptySet()
            updateBitmap(preview)
            onLayersChanged()
        }
    }

    /** 把基础调节烘焙进位图;全 0 时返回原位图 */
    private fun Bitmap.withBaseAdjustments(adjustments: BaseAdjustments): Bitmap {
        if (adjustments.isNeutral) return this
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(adjustments.toColorMatrixValues()))
        }
        Canvas(output).drawBitmap(this, 0f, 0f, paint)
        return output
    }

    // ---------------- AI 图像处理(作用于底图,图层全部保留) ----------------

    private val _isAiProcessing: MutableState<Boolean> = mutableStateOf(false)
    val isAiProcessing: Boolean by _isAiProcessing

    private var aiProcessJob: Job? = null

    /**
     * AI 图像处理:取全分辨率底图([loadBaseBitmap])调 [BaiduImageProcessRepository],
     * 成功后结果图缓存并替换底图(与 [applyBaseTransform] 同路径直改 _uri/_sourceSize/
     * _bitmap,图层列表不动,归一化坐标天然适配;抠图结果为透明底 PNG)。
     * 处理中重复调用直接忽略;[cancelAiProcessing] 取消进行中的请求。
     * 成功结果先预览,采用后再记录文档历史。
     *
     * 登录与积分预检由调用方(UI 层 ActionUtils.ensureLoginAndCheckPoints)完成。
     * [pointsCost] 记在预览结果上,用户采用后才扣;取消预览或处理失败不扣。
     */
    fun processAiImage(
        op: AiImageOp,
        rect: NormalizedRect? = null,
        pointsCost: Int = 0,
    ) {
        if (isBusy) return
        val target = selectedLayer
        if (target != null && target.type !is LayerType.Image) {
            AppToastHost.showFailureToast(R.string.studio_ai_image_target_required)
            return
        }
        aiProcessJob = launchImageTask(R.string.studio_task_ai, ImageTask.Ai) {
            val path = (target?.type as? LayerType.Image)?.imageData?.let(::assetReference)
                ?: captureDocument().baseUri
            val source = imageGetter.getImage(data = path, size = IntegerSize(3000, 3000))
                ?.let(::softwareBitmap) ?: error("Could not load the AI input")
            val result = imageProcessRepository.process(
                op = op.processOp,
                bitmap = source,
                rect = rect?.toImageProcessRect(),
                maxResultPixels = memoryBudget.maxWorkingPixels
            ).getOrThrow()
            memoryBudget.requireCanRender(result.width, result.height)
            val resultPath = projectStore.storeBitmap(result)
            ensureActive()
            _pendingAiResult.value = AiImageResult(
                imagePath = resultPath,
                title = appContext.getString(op.nameRes),
                targetLayerId = target?.id,
                replaceBackground = target == null,
                width = result.width,
                height = result.height,
                pointsCost = pointsCost.coerceAtLeast(0)
            )
        }
    }

    fun cancelAiProcessing() {
        cancelImageTasks()
    }

    fun prepareAiGeneration() {
        _aiGenerationTarget.value = if (selectedLayer?.type is LayerType.Image && canAiEdit) {
            AiGenerationTarget.SelectedLayer
        } else {
            AiGenerationTarget.NewLayer
        }
    }

    fun setAiGenerationTarget(target: AiGenerationTarget) {
        if (target != AiGenerationTarget.NewLayer && !canAiEdit) {
            AppToastHost.showFailureToast(R.string.studio_ai_edit_unsupported)
            return
        }
        if (target == AiGenerationTarget.SelectedLayer && selectedLayer?.type !is LayerType.Image) {
            AppToastHost.showFailureToast(R.string.studio_ai_image_target_required)
            return
        }
        _aiGenerationTarget.value = target
    }

    fun discardAiResult() {
        _pendingAiResult.value = null
    }

    fun acceptAiResult(asNewLayer: Boolean = false) {
        val result = _pendingAiResult.value ?: return
        if (asNewLayer || (!result.replaceBackground && result.targetLayerId == null)) {
            commitAddedLayer(MarkupLayer(type = LayerType.Image(result.imagePath)))
            _pendingAiResult.value = null
            chargeAcceptedAiResult(result)
            return
        }
        if (result.targetLayerId != null) {
            if (!replaceImageLayer(result.targetLayerId, result.imagePath)) return
            _pendingAiResult.value = null
            chargeAcceptedAiResult(result)
            return
        }
        if (isBusy) return
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            val preview = studioImageReader.load(
                result.imagePath, memoryBudget.previewSize(result.width, result.height)
            )
            ensureActive()
            history.snapshot(_layers.value)
            _baseImageHistory.update { (it + captureDocument().baseUri).distinct().takeLast(50) }
            _uri.value = result.imagePath.toUri()
            _sourceSize.value = IntSize(result.width, result.height)
            _exportSettings.update { it.copy(format = ImageFormat.Png.Lossless) }
            _imageFormat.value = ImageFormat.Png.Lossless
            _baseMask.value = null
            updateBitmap(preview)
            onLayersChanged()
            _pendingAiResult.value = null
            chargeAcceptedAiResult(result)
        }
    }

    /** 预览被采用后才扣积分。贴纸生成没有预览步骤,仍在落图层时扣。 */
    private fun chargeAcceptedAiResult(result: AiImageResult) {
        if (result.pointsCost <= 0) return
        BaseUtils.consumePoints(
            degree = result.pointsCost,
            desc = result.title,
            source = AI_POINTS_SOURCE,
            showToast = true
        )
    }

    fun revertAiImage(uri: String) {
        if (aiGenerationTarget == AiGenerationTarget.SelectedLayer) {
            val layer = selectedLayer ?: return
            if (uri !in layer.imageHistory) {
                AppToastHost.showFailureToast(R.string.studio_image_read_failed)
                return
            }
            replaceImageLayer(layer.id, uri)
        } else if (aiGenerationTarget == AiGenerationTarget.Background) {
            if (uri !in _baseImageHistory.value) {
                AppToastHost.showFailureToast(R.string.studio_image_read_failed)
                return
            }
            imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
                val (size, preview) = studioImageReader.preview(uri)
                history.snapshot(_layers.value)
                _baseImageHistory.update { ((it - uri) + captureDocument().baseUri).takeLast(50) }
                _uri.value = uri.toUri()
                _sourceSize.value = IntSize(size.width, size.height)
                updateBitmap(preview)
                onLayersChanged()
            }
        }
    }

    private fun replaceImageLayer(id: String, path: String): Boolean {
        val target = _layers.value.firstOrNull { it.id == id }
        val image = target?.type as? LayerType.Image
        if (target == null || image == null) {
            AppToastHost.showFailureToast(R.string.studio_ai_image_target_required)
            return false
        }
        updateLayer(id) {
            it.copy(
                type = LayerType.Image(path),
                imageHistory = ((it.imageHistory - path) + assetReference(image.imageData))
                    .distinct().takeLast(50)
            )
        }
        return true
    }

    /** 替换图片图层素材(相册/文件),保留位置、变换、效果与蒙版;原图进图层历史可回退 */
    fun replaceImageLayerAsset(id: String, source: Uri) {
        if (!hasImage || isBusy) return
        val layer = _layers.value.firstOrNull { it.id == id }
        if (layer?.type !is LayerType.Image || layer.transform.locked) return
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            val path = projectStore.importAsset(source.toString())
            ensureActive()
            replaceImageLayer(id, path)
        }
    }

    // ---------------- AI 生成图片(文生图;选中图片图层时图生图编辑) ----------------

    /**
     * AI 生成图片:登录与积分预检由调用方(UI 层)完成;复用 [isAiProcessing]
     * 防二次并驱动 LoadingDialog(不做占位图层)。
     * 新建:成功 addLayer(Image 图层,自动进 undo 历史);编辑:选中 Image 图层的
     * imageData 转 base64 data URI 作输入图,成功 updateLayer 换图(undo 即回退)。
     * 仅非缓存结果记积分,采用预览后才扣;失败、取消或关掉预览不扣。
     */
    fun generateImageLayer(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            AppToastHost.showFailureToast(R.string.markup_ai_generate_empty)
            return
        }
        if (isBusy) return
        val target = aiGenerationTarget
        val editLayer = selectedLayer?.takeIf { target == AiGenerationTarget.SelectedLayer }
        val inputUri = aiEditImageUri
        aiProcessJob = launchImageTask(R.string.studio_task_ai, ImageTask.Ai) {
            if (target != AiGenerationTarget.NewLayer) {
                check(canAiEdit) { appContext.getString(R.string.studio_ai_edit_unsupported) }
                check(inputUri != null) { "No AI editing target is selected" }
            }
            val size = _sourceSize.value ?: error("No image is open")
            val image = imageGenerationLoader.load(
                ImageGenerationRequest(
                    prompt = trimmed,
                    inputImages = inputUri?.let { listOf(encodeImageAsDataUri(it)) }.orEmpty(),
                    outputSize = qwenOutputSize(size.width, size.height)
                )
            ).getOrThrow()
            _taskLabel.value = appContext.getString(R.string.studio_task_ai_download)
            val path = projectStore.importAsset(image.file.absolutePath)
            val (dimensions, _) = studioImageReader.preview(path)
            ensureActive()
            _pendingAiResult.value = AiImageResult(
                imagePath = path,
                title = appContext.getString(R.string.markup_ai_generate_title),
                targetLayerId = editLayer?.id,
                replaceBackground = target == AiGenerationTarget.Background,
                width = dimensions.width,
                height = dimensions.height,
                pointsCost = if (image.fromCache) 0 else aiImageProcessPointsCost()
            )
        }
    }

    // ---------------- AI 生成贴纸(共享贴纸弹层 AI tab) ----------------

    /** AI 生成贴纸进行中(弹层内 loading;不复用 isAiProcessing,避免触发全屏 LoadingDialog) */
    private val _isGeneratingSticker: MutableState<Boolean> = mutableStateOf(false)
    val isGeneratingSticker: Boolean by _isGeneratingSticker

    private var generateStickerJob: Job? = null

    /**
     * AI 生成贴纸:文生图(提示词追加贴纸风格描述,正方形输出),成功落 Sticker 图层
     *(addLayer 自动进 undo 历史),[onSuccess] 供弹层关闭。
     * 登录与积分预检由调用方(UI 层 ActionUtils.ensureLoginAndCheckPoints)完成;
     * 仅非缓存结果(!fromCache)扣积分,失败/取消不扣。
     */
    fun generateSticker(prompt: String, onSuccess: () -> Unit = {}) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            AppToastHost.showFailureToast(R.string.markup_ai_generate_empty)
            return
        }
        if (_isGeneratingSticker.value || isBusy) return
        val currentProject = projectId
        generateStickerJob = componentScope.launch {
            _isGeneratingSticker.value = true
            try {
                val image = imageGenerationLoader.load(
                    ImageGenerationRequest(
                        prompt = "$trimmed, $STICKER_PROMPT_STYLE_SUFFIX",
                        outputSize = STICKER_OUTPUT_SIZE
                    )
                ).getOrThrow()
                val path = projectStore.importAsset(image.file.absolutePath)
                ensureActive()
                check(projectId == currentProject) { "The image project was closed" }
                commitAddedLayer(
                    MarkupLayer(
                        type = LayerType.Sticker(StickerSource.Generated(path))
                    )
                )
                if (!image.fromCache) {
                    BaseUtils.consumePoints(
                        degree = aiImageProcessPointsCost(),
                        desc = appContext.getString(
                            com.t8rin.imagetoolbox.core.resources.R.string.sticker_category_ai
                        ),
                        source = AI_POINTS_SOURCE,
                        showToast = true
                    )
                }
                onSuccess()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                reportProcessingFailure(failure)
            } finally {
                _isGeneratingSticker.value = false
            }
        }
    }

    /** 本地图片文件 → base64 data URI(图生图输入) */
    private suspend fun encodeImageAsDataUri(path: String): String = withContext(defaultDispatcher) {
        val image = imageGetter.getImage(data = path, size = IntegerSize(2048, 2048))
            ?.let(::softwareBitmap) ?: error("Could not load the AI input")
        val output = ByteArrayOutputStream()
        check(image.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not encode the AI input" }
        "data:image/png;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    /** 生成输出尺寸(服务商限制:像素总数 ≤ 2048²、宽高比 ≤ 8:1),按底图等比收窄 */
    private fun qwenOutputSize(width: Int, height: Int): String {
        require(width > 0 && height > 0) { "Invalid generation dimensions" }
        require(maxOf(width, height).toDouble() / minOf(width, height) <= 8.0) {
            "AI generation supports aspect ratios up to 8:1"
        }
        val maxPixels = 2048.0 * 2048.0
        val pixels = width.toDouble() * height
        val scale = if (pixels > maxPixels) sqrt(maxPixels / pixels) else 1.0
        return "${(width * scale).roundToInt()}*${(height * scale).roundToInt()}"
    }

    // ---------------- 图层操作(修改前一律先记快照) ----------------

    fun addLayer(layer: MarkupLayer) {
        if (!hasImage || isBusy) return
        val needsImport = layer.type is LayerType.Image ||
            (layer.type as? LayerType.Sticker)?.source is StickerSource.Generated
        if (needsImport) {
            imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
                commitAddedLayer(importLayerAssets(layer))
            }
        } else {
            commitAddedLayer(layer)
        }
    }

    private suspend fun importLayerAssets(layer: MarkupLayer): MarkupLayer {
        val imported = when (val type = layer.type) {
            is LayerType.Image -> LayerType.Image(
                projectStore.importAsset(assetReference(type.imageData))
            )
            is LayerType.Sticker -> when (val source = type.source) {
                is StickerSource.Generated -> LayerType.Sticker(
                    StickerSource.Generated(projectStore.importAsset(source.path))
                )
                else -> type
            }
            else -> type
        }
        return layer.copy(
            type = imported,
            imageHistory = layer.imageHistory.map { projectStore.importAsset(it) }
        )
    }

    private fun assetReference(data: Any): String = when (data) {
        is String -> data
        is Uri -> data.toString()
        is File -> data.absolutePath
        else -> throw IllegalArgumentException("Unsupported image reference")
    }

    private fun commitAddedLayer(layer: MarkupLayer) {
        history.snapshot(_layers.value)
        _layers.update { it + layer }
        _selectedLayerId.value = layer.id
        _selectedLayerIds.value = setOf(layer.id)
        onLayersChanged()
    }

    fun updateLayer(
        id: String,
        transform: (MarkupLayer) -> MarkupLayer
    ) {
        val existing = _layers.value.firstOrNull { it.id == id } ?: return
        val updated = transform(existing)
        if (updated == existing) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.map { if (it.id == id) updated else it } }
        onLayersChanged()
    }

    fun updateLayerTransform(
        id: String,
        transform: LayerTransform
    ) {
        if (!layerTransformTransactionActive) history.snapshot(_layers.value)
        updateLayerTransformTransient(id, transform)
        endLayerTransformChange()
        refreshHistoryFlags()
    }

    /** 画布手势拖动开始:整段手势只记一次历史快照,配合 [updateLayerTransformTransient] 使用 */
    fun beginLayerTransformChange() {
        history.snapshot(_layers.value)
        layerTransformTransactionActive = true
        refreshHistoryFlags()
    }

    fun endLayerTransformChange() {
        layerTransformTransactionActive = false
    }

    /** 画布手势拖动中:直接改值不入历史(快照已在 [beginLayerTransformChange] 记录) */
    fun updateLayerTransformTransient(
        id: String,
        transform: LayerTransform
    ) {
        val primary = _layers.value.firstOrNull { it.id == id } ?: return
        if (primary.transform.locked) return
        val old = primary.transform
        val deltaScale = transform.scale / old.scale
        val deltaRotation = transform.rotation - old.rotation
        val radians = Math.toRadians(deltaRotation.toDouble())
        val size = _sourceSize.value ?: return
        val linked = if (id in selectedLayerIds) selectedLayerIds else setOf(id)
        _layers.update { list ->
            list.map { layer ->
                if (layer.id == id) {
                    layer.copy(transform = transform)
                } else if (layer.id in linked && !layer.transform.locked) {
                    val dx = (layer.transform.centerX - old.centerX) * size.width
                    val dy = (layer.transform.centerY - old.centerY) * size.height
                    val x = (dx * cos(radians) - dy * sin(radians)) * deltaScale
                    val y = (dx * sin(radians) + dy * cos(radians)) * deltaScale
                    layer.copy(transform = layer.transform.copy(
                        centerX = transform.centerX + x.toFloat() / size.width,
                        centerY = transform.centerY + y.toFloat() / size.height,
                        scale = layer.transform.scale * deltaScale,
                        rotation = layer.transform.rotation + deltaRotation
                    ))
                } else layer
            }
        }
        registerDocumentChange()
    }

    fun snapLayerTransform(transform: LayerTransform): LayerTransform {
        val snappedRotation = (transform.rotation / 15f).roundToInt() * 15f
        return transform.copy(
            centerX = if (abs(transform.centerX - 0.5f) < 0.015f) 0.5f else transform.centerX,
            centerY = if (abs(transform.centerY - 0.5f) < 0.015f) 0.5f else transform.centerY,
            rotation = if (abs(transform.rotation - snappedRotation) < 2f) {
                snappedRotation
            } else transform.rotation
        )
    }

    fun renameLayer(id: String, name: String) {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "Layer name must not be empty" }
        updateLayer(id) { it.copy(name = normalized) }
    }

    fun alignLayer(id: String, horizontal: Boolean, position: Float) {
        require(position.isFinite() && position in 0f..1f) { "Invalid alignment position" }
        val layer = _layers.value.firstOrNull { it.id == id && !it.transform.locked } ?: return
        updateLayerTransform(id, layer.transform.run {
            if (horizontal) copy(centerX = position) else copy(centerY = position)
        })
    }

    fun nudgeLayer(id: String, dx: Int, dy: Int) {
        val layer = _layers.value.firstOrNull { it.id == id && !it.transform.locked } ?: return
        val size = _sourceSize.value ?: return
        updateLayerTransform(id, layer.transform.copy(
            centerX = layer.transform.centerX + dx.toFloat() / size.width,
            centerY = layer.transform.centerY + dy.toFloat() / size.height
        ))
    }

    fun flipLayer(id: String, horizontal: Boolean) {
        updateLayer(id) { layer ->
            if (horizontal) layer.copy(transform = layer.transform.copy(
                flipHorizontal = !layer.transform.flipHorizontal
            )) else layer.copy(transform = layer.transform.copy(
                flipVertical = !layer.transform.flipVertical
            ))
        }
    }

    fun toggleLayerSelection(id: String) {
        val layer = _layers.value.firstOrNull { it.id == id && !it.transform.locked } ?: return
        endLayerTransformChange()
        _selectedLayerIds.update { if (id in it) it - id else it + id }
        _selectedLayerId.value = if (id in selectedLayerIds) id else selectedLayerIds.lastOrNull()
        _maskTargetBitmap.value = null
    }

    fun groupSelectedLayers() {
        if (selectedLayerIds.size < 2) return
        val group = UUID.randomUUID().toString()
        history.snapshot(_layers.value)
        _layers.update { list ->
            list.map { if (it.id in selectedLayerIds) it.copy(groupId = group) else it }
        }
        onLayersChanged()
    }

    fun ungroupSelectedLayers() {
        if (selectedLayerIds.isEmpty()) return
        history.snapshot(_layers.value)
        _layers.update { list ->
            list.map { if (it.id in selectedLayerIds) it.copy(groupId = null) else it }
        }
        onLayersChanged()
    }

    fun removeSelectedLayers() {
        val ids = selectedLayerIds
        if (ids.isEmpty()) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.filterNot { it.id in ids && !it.transform.locked } }
        pruneSelection()
        onLayersChanged()
    }

    fun duplicateSelectedLayers() {
        val groups = selectedLayerIds.mapNotNull { id ->
            _layers.value.firstOrNull { it.id == id }?.groupId
        }.distinct().associateWith { UUID.randomUUID().toString() }
        val copies = _layers.value.filter { it.id in selectedLayerIds && !it.transform.locked }
            .associate { layer ->
                layer.id to layer.copy(
                    id = UUID.randomUUID().toString(),
                    groupId = layer.groupId?.let(groups::get)
                )
            }
        if (copies.isEmpty()) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.flatMap { layer ->
            copies[layer.id]?.let { listOf(layer, it) } ?: listOf(layer)
        } }
        _selectedLayerIds.value = copies.values.map { it.id }.toSet()
        _selectedLayerId.value = selectedLayerIds.firstOrNull()
        onLayersChanged()
    }

    fun removeLayer(id: String) {
        if (_layers.value.none { it.id == id }) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.filterNot { it.id == id } }
        if (_selectedLayerId.value == id) _selectedLayerId.value = null
        _selectedLayerIds.update { it - id }
        onLayersChanged()
    }

    fun duplicateLayer(id: String) {
        val source = _layers.value.find { it.id == id } ?: return
        history.snapshot(_layers.value)
        val copy = source.copy(id = UUID.randomUUID().toString(), groupId = null)
        _layers.update { list ->
            list.toMutableList().apply { add(list.indexOf(source) + 1, copy) }
        }
        _selectedLayerId.value = copy.id
        _selectedLayerIds.value = setOf(copy.id)
        onLayersChanged()
    }

    fun toggleLayerVisible(id: String) = updateLayer(id) {
        it.copy(transform = it.transform.copy(visible = !it.transform.visible))
    }

    fun toggleLayerLocked(id: String) = updateLayer(id) {
        it.copy(transform = it.transform.copy(locked = !it.transform.locked))
    }

    fun setLayerAlpha(
        id: String,
        alpha: Float
    ) = updateLayer(id) {
        it.copy(transform = it.transform.copy(alpha = alpha.coerceIn(0f, 1f)))
    }

    /** 透明度拖动开始:整段拖动只记一次历史快照,配合 [updateLayerAlphaTransient] 使用 */
    fun beginLayerAlphaChange() {
        history.snapshot(_layers.value)
        onLayersChanged()
    }

    /** 透明度拖动中:直接改值不入历史(快照已在 [beginLayerAlphaChange] 记录) */
    fun updateLayerAlphaTransient(
        id: String,
        alpha: Float
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(transform = it.transform.copy(alpha = alpha.coerceIn(0f, 1f)))
                } else it
            }
        }
        registerDocumentChange()
    }

    /** 文字层框尺寸拖动手势中间帧:直接改 widthRatio 不入历史(快照已在 [beginLayerTransformChange] 记录) */
    fun updateTextWidthRatioTransient(
        id: String,
        widthRatio: Float
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list ->
            list.map { layer ->
                if (layer.id == id && layer.type is LayerType.Text) {
                    layer.copy(
                        type = layer.type.copy(widthRatio = widthRatio.coerceIn(0.05f, 1f))
                    )
                } else layer
            }
        }
        registerDocumentChange()
    }

    /** 形状图层参数更新(形状种类/样式/颜色等离散修改),正常入历史 */
    fun updateShapeSpec(
        id: String,
        spec: ShapeSpec
    ) = updateLayer(id) { layer ->
        (layer.type as? LayerType.Shape)
            ?.let { layer.copy(type = it.copy(spec = spec)) }
            ?: layer
    }

    /** 形状参数拖动开始:整段拖动只记一次历史快照,配合 [updateShapeSpecTransient] 使用 */
    fun beginShapeSpecChange() {
        history.snapshot(_layers.value)
        onLayersChanged()
    }

    /** 形状参数拖动中:直接改值不入历史(快照已在 [beginShapeSpecChange] 记录) */
    fun updateShapeSpecTransient(
        id: String,
        spec: ShapeSpec
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list ->
            list.map { layer ->
                if (layer.id == id && layer.type is LayerType.Shape) {
                    layer.copy(type = LayerType.Shape(spec))
                } else layer
            }
        }
        registerDocumentChange()
    }

    fun setLayerBlendMode(
        id: String,
        blendMode: LayerBlendMode
    ) = updateLayer(id) {
        it.copy(transform = it.transform.copy(blendMode = blendMode))
    }

    /** z 序上移一位(列表尾部是最顶层) */
    fun moveLayerUp(id: String) = moveLayer(id, delta = 1)

    /** z 序下移一位 */
    fun moveLayerDown(id: String) = moveLayer(id, delta = -1)

    private fun moveLayer(
        id: String,
        delta: Int
    ) {
        val list = _layers.value
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return
        val target = index + delta
        if (target !in list.indices) return
        history.snapshot(list)
        _layers.update { current ->
            current.toMutableList().apply { add(target, removeAt(index)) }
        }
        onLayersChanged()
    }

    /**
     * 向下合并:把该图层与其下方最近一个可见图层按原图尺寸烘焙成一张透明底
     * PNG,缓存后以图片图层替换原两个图层。烘焙图像素已含位置信息,故新图层
     * transform 保持居中;scale 取 1/[IMAGE_LAYER_BASE_WIDTH_RATIO] 抵消图片图层
     * 的基础宽度比例,使烘焙图恰好铺满画布。下方没有可合并图层时 toast 提示。
     * 各图层自身的滤镜/调节随合并烘焙进结果位图,合并后图层不再携带。
     */
    fun mergeLayerDown(id: String) {
        if (isBusy) return
        val list = _layers.value
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return
        val layer = list[index]
        if (layer.transform.locked) return
        val lowerLayer = (index - 1 downTo 0)
            .map { list[it] }
            .firstOrNull { it.transform.visible }
        if (lowerLayer == null) {
            AppToastHost.showToast(R.string.markup_merge_no_layer_below)
            return
        }
        if (lowerLayer.transform.locked) {
            AppToastHost.showFailureToast(R.string.studio_merge_locked)
            return
        }
        if (layer.transform.blendMode != LayerBlendMode.Normal ||
            lowerLayer.transform.blendMode != LayerBlendMode.Normal
        ) {
            AppToastHost.showFailureToast(R.string.studio_merge_blended)
            return
        }
        val size = _sourceSize.value
            ?: _bitmap.value?.let { IntSize(it.width, it.height) }
        if (size == null || size.width <= 0 || size.height <= 0) return

        mergeJob = launchImageTask(R.string.studio_task_rendering, ImageTask.Saving) {
            memoryBudget.requireCanRender(size.width, size.height)
            val base = withContext(defaultDispatcher) {
                Bitmap.createBitmap(
                    size.width,
                    size.height,
                    Bitmap.Config.ARGB_8888
                )
            }
            val baked = markupLayersApplier.applyToImage(
                image = base,
                layers = listOf(lowerLayer, layer),
                filteredLayers = emptyMap()
            )
            val path = projectStore.storeBitmap(baked)
            ensureActive()
            history.snapshot(_layers.value)
            val mergedLayer = MarkupLayer(
                type = LayerType.Image(imageData = path),
                transform = LayerTransform(scale = 1f / IMAGE_LAYER_BASE_WIDTH_RATIO)
            )
            _layers.update { current ->
                val upperIndex = current.indexOfFirst { it.id == id }
                val lowerIndex = current.indexOfFirst { it.id == lowerLayer.id }
                if (upperIndex <= lowerIndex) {
                    current
                } else {
                    current.toMutableList().apply {
                        removeAt(upperIndex)
                        removeAt(lowerIndex)
                        add(lowerIndex, mergedLayer)
                    }
                }
            }
            _selectedLayerId.value = mergedLayer.id
            _selectedLayerIds.value = setOf(mergedLayer.id)
            onLayersChanged()
        }
    }

    private var rasterizeJob: Job? = null

    /**
     * 光栅化图层:把该图层(文字/形状等矢量图层)按底图全分辨率画到等底图尺寸的
     * 透明位图(带其位置/缩放/旋转;alpha 不烘焙、由新图层 transform 沿用,避免双重
     * 生效;调节保留在图层字段上不烘焙),缓存后以图片图层原位替换,替换前记一次
     * 历史快照(可撤销)。与 [mergeLayerDown] 同理,scale 取
     * 1/[IMAGE_LAYER_BASE_WIDTH_RATIO] 抵消图片图层基础宽度比例,使烘焙图铺满画布。
     * 图层 id 保持不变,选中态/编辑会话引用不受影响。
     * 完成后回调 [onRasterized](如滤镜流程:光栅化后再应用滤镜);失败 toast 提示。
     */
    fun rasterizeLayer(
        layerId: String,
        onRasterized: () -> Unit = {}
    ) {
        if (isBusy) return
        val layer = _layers.value.find { it.id == layerId } ?: return
        // 位图类图层无需光栅化(该入口面向文字/形状),涂鸦虽可光栅化但无必要
        if (layer.type.isBitmapFilterable) return
        if (layer.transform.locked) return
        val size = _sourceSize.value
            ?: _bitmap.value?.let { IntSize(it.width, it.height) }
        if (size == null || size.width <= 0 || size.height <= 0) return

        rasterizeJob = launchImageTask(R.string.studio_task_rendering, ImageTask.Saving) {
            memoryBudget.requireCanRender(size.width, size.height)
            val path = run {
                val base = withContext(defaultDispatcher) {
                    Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                }
                // alpha 不烘焙(新图层 transform 沿用);调节不烘焙(保留在图层字段,
                // 光栅化后仍可实时调节),避免双重生效
                val bakeLayer = layer.copy(
                    transform = layer.transform.copy(alpha = 1f, blendMode = LayerBlendMode.Normal),
                    adjustments = BaseAdjustments()
                )
                val baked = markupLayersApplier.applyToImage(
                    image = base,
                    layers = listOf(bakeLayer)
                )
                projectStore.storeBitmap(baked)
            }
            ensureActive()
            history.snapshot(_layers.value)
            val imageLayer = layer.copy(
                type = LayerType.Image(imageData = path),
                transform = layer.transform.copy(
                    centerX = 0.5f,
                    centerY = 0.5f,
                    scale = 1f / IMAGE_LAYER_BASE_WIDTH_RATIO,
                    rotation = 0f,
                    flipHorizontal = false,
                    flipVertical = false
                )
            )
            _layers.update { list ->
                list.map { if (it.id == layerId) imageLayer else it }
            }
            onLayersChanged()
            isCompoundEdit = true
            try {
                onRasterized()
            } finally {
                isCompoundEdit = false
            }
        }
    }

    fun reorderLayers(layers: List<MarkupLayer>) {
        reorderLayersAndBase(layers, _baseOrder.value)
    }

    /** 图层 z 序 + 背景位置一次性提交(背景在面板里与图层一起拖拽排序),只记一次历史快照 */
    fun reorderLayersAndBase(layers: List<MarkupLayer>, baseOrder: Int) {
        val coercedOrder = baseOrder.coerceIn(0, layers.size)
        if (layers == _layers.value && coercedOrder == _baseOrder.value) return
        history.snapshot(_layers.value)
        _layers.value = layers
        _baseOrder.value = coercedOrder
        onLayersChanged()
    }

    /** 背景(底图)显隐:隐藏后画布/导出均为透明底,图层照常渲染,可撤销 */
    fun toggleBaseVisible() {
        history.snapshot(_layers.value)
        _baseVisible.value = !_baseVisible.value
        onLayersChanged()
    }

    /**
     * 删除背景:底图替换为同尺寸透明底(画布尺寸与图层保留),原底图进资产历史,
     * 整体走一次文档快照可撤销。参考 [startWithBlankCanvas] 的透明底图链路。
     */
    fun removeBaseBackground() {
        if (!hasImage || isBusy) return
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            val size = _sourceSize.value ?: error("No image dimensions are available")
            memoryBudget.requireCanRender(size.width, size.height)
            val blank = withContext(defaultDispatcher) {
                Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            }
            val path = try {
                projectStore.storeBitmap(blank)
            } finally {
                blank.recycle()
            }
            val preview = studioImageReader.load(path, memoryBudget.previewSize(size.width, size.height))
            ensureActive()
            history.snapshot(_layers.value)
            _baseImageHistory.update { (it + captureDocument().baseUri).distinct().takeLast(50) }
            _uri.value = path.toUri()
            _blankBaseBitmap.value = null
            _baseMask.value = null
            _baseEffects.value = emptyList()
            _baseAdjustments.value = BaseAdjustments()
            _selectedFilter.value = null
            _baseVisible.value = true
            _imageFormat.value = ImageFormat.Png.Lossless
            _exportSettings.update { it.copy(format = ImageFormat.Png.Lossless) }
            updateBitmap(preview)
            onLayersChanged()
        }
    }

    fun clearLayers() {
        if (_layers.value.isEmpty()) return
        history.snapshot(_layers.value)
        _layers.value = emptyList()
        _selectedLayerId.value = null
        onLayersChanged()
    }

    fun selectLayer(id: String?) {
        endEffectsChange()
        endLayerTransformChange()
        val target = _layers.value.firstOrNull { it.id == id }
        _selectedLayerId.value = target?.id
        _selectedLayerIds.value = if (target == null) {
            emptySet()
        } else if (target.groupId != null) {
            _layers.value.filter { it.groupId == target.groupId && !it.transform.locked }
                .map { it.id }.toSet()
        } else setOf(target.id)
        _maskTargetBitmap.value = null
    }

    fun setActiveTool(id: String?) {
        _activeToolId.value = id
    }

    // ---------------- 图层编辑会话(文字工具页:整个会话 = 一次历史粒度) ----------------

    /**
     * 编辑会话快照。[preEditLayers] 为进入会话时的完整图层列表;
     * [createdLayerId] 非空表示「新建图层再编辑」流程(快照中已含该新图层)
     */
    private class LayerEditSession(
        val preEditDocument: MarkupDocument,
        val createdLayerId: String?,
    ) {
        val preEditLayers get() = preEditDocument.layers
    }

    private var editSession: LayerEditSession? = null

    private val _editSessionLayerId: MutableState<String?> = mutableStateOf(null)

    /** 正在编辑会话中编辑的图层 id,会话外为 null */
    val editSessionLayerId: String? by _editSessionLayerId

    /** 当前会话是否为「新建文字图层」流程 */
    val isEditSessionNewLayer: Boolean get() = editSession?.createdLayerId != null

    /**
     * 开始文字图层编辑会话。[layerId] 为空时先经 [addLayer] 新建默认空文字图层
     * (自带一次历史快照)再记会话;非空时直接编辑该已有文字图层。
     * 会话期间的一切修改都应走 [updateLayerInEditSession] 等 transient 接口。
     */
    fun beginTextEditSession(layerId: String? = null) {
        val beforeEdit = captureDocument()
        val targetId: String
        if (layerId == null) {
            val layer = MarkupLayer(type = LayerType.Text.Default.copy(text = ""))
            _layers.update { it + layer }
            targetId = layer.id
        } else {
            val layer = _layers.value.find { it.id == layerId }
            if (layer == null || layer.type !is LayerType.Text) return
            targetId = layerId
        }
        editSession = LayerEditSession(
            preEditDocument = beforeEdit,
            createdLayerId = targetId.takeIf { layerId == null }
        )
        _editSessionLayerId.value = targetId
        _selectedLayerId.value = targetId
        _selectedLayerIds.value = setOf(targetId)
    }

    /** 会话内图层修改:直接改值不入历史,确认/取消时统一结算 */
    fun updateLayerInEditSession(
        id: String,
        transform: (MarkupLayer) -> MarkupLayer
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list -> list.map { if (it.id == id) transform(it) else it } }
        registerDocumentChange()
    }

    /** 会话内删除图层(不入历史):随后接 [commitLayerEditSession],undo 一次即可恢复 */
    fun removeLayerInEditSession(id: String) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list -> list.filterNot { it.id == id } }
        registerDocumentChange()
    }

    /** 会话内复制图层(不入历史):副本插入原图层之上,编辑对象切换到副本 */
    fun duplicateLayerInEditSession(id: String) {
        val source = _layers.value.find { it.id == id } ?: return
        val copy = source.copy(id = java.util.UUID.randomUUID().toString())
        _layers.update { list ->
            list.toMutableList().apply { add(list.indexOf(source) + 1, copy) }
        }
        _editSessionLayerId.value = copy.id
        _selectedLayerId.value = copy.id
        registerDocumentChange()
    }

    /**
     * 确认会话:整个会话的变更压成一次历史记录。
     * 新建图层文本为空(或已在会话内删除)时等同放弃:恢复会话前状态并
     * 移除该新图层,不产生新历史。
     */
    fun commitLayerEditSession() {
        val session = editSession ?: return
        val createdId = session.createdLayerId
        if (createdId != null) {
            val createdText = (_layers.value.find { it.id == createdId }
                ?.type as? LayerType.Text)?.text
            if (createdText.isNullOrBlank()) {
                _layers.value = session.preEditLayers.filterNot { it.id == createdId }
                endLayerEditSession()
                pruneSelection()
                onLayersChanged()
                return
            }
        }
        if (_layers.value != session.preEditLayers) {
            documentHistory.snapshot(session.preEditDocument)
        }
        endLayerEditSession()
        onLayersChanged()
    }

    /** 取消会话:恢复会话前状态(新建流程连新图层一并移除),不入历史 */
    fun cancelLayerEditSession() {
        val session = editSession ?: return
        _layers.value = session.preEditLayers.filterNot { it.id == session.createdLayerId }
        endLayerEditSession()
        pruneSelection()
        onLayersChanged()
    }

    private fun endLayerEditSession() {
        editSession = null
        _editSessionLayerId.value = null
    }

    // ---------------- undo / redo ----------------

    fun undo() {
        traverseHistory(undo = true)
    }

    fun redo() {
        traverseHistory(undo = false)
    }

    private fun traverseHistory(undo: Boolean) {
        if (!hasImage || isBusy) return
        endEffectsChange()
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            val current = captureDocument()
            val next = if (undo) documentHistory.undo(current) else documentHistory.redo(current)
            if (next == null) return@launchImageTask
            var restored = false
            try {
                val preview = studioImageReader.load(
                    next.baseUri, memoryBudget.previewSize(next.width, next.height)
                )
                ensureActive()
                applyDocument(next, preview)
                restored = true
                pruneSelection()
                onLayersChanged()
            } finally {
                if (!restored) {
                    if (undo) documentHistory.redo(next) else documentHistory.undo(next)
                }
                refreshHistoryFlags()
            }
        }
    }

    private fun pruneSelection() {
        if (_selectedLayerId.value !in _layers.value.map { it.id }) {
            _selectedLayerId.value = null
        }
        _selectedLayerIds.value = _selectedLayerIds.value.intersect(_layers.value.map { it.id }.toSet())
    }

    private fun onLayersChanged() {
        refreshHistoryFlags()
        registerDocumentChange()
        syncLayerFilterCache()
    }

    private fun refreshHistoryFlags() {
        _canUndo.value = history.canUndo
        _canRedo.value = history.canRedo
    }

    private fun captureDocument(): MarkupDocument {
        val size = _sourceSize.value ?: error("No image dimensions are available")
        return MarkupDocument(
            baseUri = _uri.value?.toString() ?: error("No image asset is open"),
            width = size.width,
            height = size.height,
            layers = _layers.value,
            baseAdjustments = _baseAdjustments.value,
            baseEffects = _baseEffects.value,
            baseMask = _baseMask.value,
            baseTransforms = _baseTransforms.value,
            baseImageHistory = _baseImageHistory.value,
            exportSettings = _exportSettings.value,
            originalUri = _originalUri.value,
            sourceUri = _sourceUri.value,
            saveExif = _saveExif.value,
            workspaceBackground = _canvasBackground.value,
            baseVisible = _baseVisible.value,
            baseOrder = _baseOrder.value
        )
    }

    private fun applyDocument(document: MarkupDocument, preview: Bitmap) {
        documentRevision++
        compositePreviewJob?.cancel()
        filterPreviewJob = null
        layerFilterJob = null
        layerFilterCache.clear()
        _filterPreviewBitmap.value = null
        _compositePreview.value = null
        _maskTargetBitmap.value = null
        _uri.value = document.baseUri.toUri()
        _blankBaseBitmap.value = null
        _sourceSize.value = IntSize(document.width, document.height)
        _layers.value = document.layers
        _baseAdjustments.value = document.baseAdjustments
        _baseEffects.value = document.baseEffects
        _selectedFilter.value = document.baseEffects.lastOrNull()?.filter
        _baseMask.value = document.baseMask
        _baseTransforms.value = document.baseTransforms
        _baseImageHistory.value = document.baseImageHistory
        _exportSettings.value = document.exportSettings
        _imageFormat.value = document.exportSettings.format
        _saveExif.value = document.saveExif
        _originalUri.value = document.originalUri
        _sourceUri.value = document.sourceUri
        _canvasBackground.value = document.workspaceBackground
        _baseVisible.value = document.baseVisible
        _baseOrder.value = document.baseOrder.coerceIn(0, document.layers.size)
        _bitmap.value = softwareBitmap(preview)
        _isComparing.value = false
        updateFilterPreview()
        syncLayerFilterCache()
    }

    private fun registerDocumentChange() {
        registerChanges()
        documentRevision++
        _compositePreview.value = null
        if (hasImage && editSession == null) {
            scheduleAutosave()
            scheduleCompositePreview()
        }
    }

    private fun scheduleAutosave() {
        val id = projectId ?: return
        val document = captureDocument()
        val name = _projectName.value
        val revision = documentRevision
        autosaveJob?.cancel()
        autosaveJob = componentScope.launch {
            delay(750)
            persistProject(id, name, document, revision, interactive = false)
        }
    }

    private suspend fun persistProject(
        id: String,
        name: String,
        document: MarkupDocument,
        revision: Long,
        interactive: Boolean,
    ): Boolean = projectWriteMutex.withLock {
        _isProjectSaving.value = true
        try {
            val thumbnail = _compositePreview.value.takeIf { compositePreviewRevision == revision }
            projectStore.save(id, name, document, thumbnail)
            if (projectId == id && documentRevision == revision) {
                _projectSaveError.value = null
                registerChangesCleared()
            }
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            failure.makeLog(LOG_TAG)
            val message = appContext.getString(
                R.string.studio_project_save_failed, failure.message.orEmpty()
            )
            // 自动保存失败不弹阻断对话框(编辑可继续),只 toast 提示;
            // 手动保存失败才置 projectSaveError 触发重试对话框
            if (interactive && projectId == id) _projectSaveError.value = message
            AppToastHost.showFailureToast(message)
            false
        } finally {
            _isProjectSaving.value = false
        }
    }

    private fun observeProjects() {
        componentScope.launch {
            projectStore.projects.collect { _projects.value = it }
        }
        componentScope.launch {
            try {
                projectStore.refresh()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                AppToastHost.showFailureToast(
                    appContext.getString(R.string.studio_project_load_failed, failure.message.orEmpty())
                )
            }
        }
    }

    fun saveProject(onComplete: () -> Unit = {}) {
        if (!hasImage || editSession != null) return
        val id = projectId ?: return
        val document = captureDocument()
        val revision = documentRevision
        val name = _projectName.value
        autosaveJob?.cancel()
        autosaveJob = componentScope.launch {
            if (persistProject(id, name, document, revision, interactive = true)) onComplete()
        }
    }

    // ---------------- 配方与批处理 ----------------

    private fun refreshRecipes() {
        componentScope.launch {
            try {
                _recipes.value = recipeStore.listRecipes()
                _recipeError.value = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                _recipeError.value = failure.message
            }
        }
    }

    /** 把当前文档(调节/效果/蒙版/图层/导出设置)存为可复用配方 */
    fun saveRecipeFromCurrent(name: String) {
        if (!hasImage || editSession != null) return
        val recipe = try {
            MarkupRecipe.fromDocument(name, captureDocument())
        } catch (failure: Exception) {
            _recipeError.value = failure.message
            return
        }
        componentScope.launch {
            try {
                recipeStore.saveRecipe(recipe)
                refreshRecipes()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                _recipeError.value = failure.message
            }
        }
    }

    fun deleteRecipe(recipe: MarkupRecipe) {
        componentScope.launch {
            try {
                recipeStore.deleteRecipe(recipe.id)
                _recipes.update { list -> list.filterNot { it.id == recipe.id } }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                _recipeError.value = failure.message
            }
        }
    }

    /** 把配方应用到当前项目:一步 undo 可整体撤回 */
    fun applyRecipe(recipe: MarkupRecipe) {
        if (!hasImage || isBusy || editSession != null) return
        history.snapshot(_layers.value)
        val added = recipe.instantiateLayers()
        if (added.isNotEmpty()) {
            _layers.update { it + added }
        }
        _baseAdjustments.value = recipe.adjustments
        _baseEffects.value = recipe.effects
        _baseMask.value = recipe.mask
        _exportSettings.value = recipe.exportSettings
        _imageFormat.value = recipe.exportSettings.format
        updateFilterPreview()
        onLayersChanged()
    }

    fun setBatchImages(uris: List<Uri>) {
        batchImages = uris.map { it.toString() }
        _batchImageCount.value = batchImages.size
        _batchReport.value = null
    }

    fun runBatch(recipe: MarkupRecipe) {
        if (batchJob?.isActive == true || batchImages.isEmpty()) return
        _batchReport.value = null
        _recipeError.value = null
        batchJob = componentScope.launch {
            try {
                val report = batchProcessor.process(recipe, batchImages) { progress ->
                    _batchProgress.value = progress
                }
                _batchReport.value = report
            } catch (cancelled: MarkupBatchCancelledException) {
                // 用户取消:展示已落盘部分的报告,不再抛出
                _batchReport.value = cancelled.report
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                _recipeError.value = failure.message
            } finally {
                _batchProgress.value = null
            }
        }
    }

    fun cancelBatch() {
        batchJob?.cancel()
    }

    fun renameProject(name: String) {
        val normalized = name.trim()
        require(normalized.isNotEmpty()) { "Project name must not be empty" }
        if (normalized == _projectName.value) return
        _projectName.value = normalized
        registerDocumentChange()
    }

    fun openProject(id: String) {
        if (isBusy) return
        _pendingAiOp.value = null
        imageLoadingJob = launchImageTask(R.string.studio_task_loading, ImageTask.Loading) {
            if (hasImage && haveChanges &&
                !persistProject(projectId ?: error("No project ID"), projectName,
                    captureDocument(), documentRevision, interactive = false)
            ) return@launchImageTask
            val document = projectStore.load(id)
            val preview = studioImageReader.load(
                document.baseUri, memoryBudget.previewSize(document.width, document.height)
            )
            val original = document.originalUri?.let { studioImageReader.preview(it).second } ?: preview
            ensureActive()
            history.clear()
            endLayerEditSession()
            _selectedLayerId.value = null
            _selectedLayerIds.value = emptySet()
            projectId = id
            _projectName.value = projects.firstOrNull { it.id == id }?.name
                ?: appContext.getString(R.string.studio_project_untitled)
            applyDocument(document, preview)
            _originalPreviewBitmap.value = original
            _projectSaveError.value = null
            registerChangesCleared()
            refreshHistoryFlags()
        }
    }

    fun deleteProject(id: String) {
        componentScope.launch {
            try {
                projectStore.delete(id)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                failure.makeLog(LOG_TAG)
                AppToastHost.showFailureToast(
                    appContext.getString(R.string.studio_project_save_failed, failure.message.orEmpty())
                )
            }
        }
    }

    fun closeProject() {
        if (isBusy) return
        saveProject(onComplete = ::clearEditorState)
    }

    private enum class ImageTask { Loading, Saving, Ai }

    private val isBusy: Boolean
        get() = _isSaving.value || _isImageLoading.value || _isAiProcessing.value

    private fun launchImageTask(
        label: Int,
        task: ImageTask,
        onFailure: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        cancelImageTasks()
        val generation = ++taskGeneration
        _taskLabel.value = appContext.getString(label)
        _isSaving.value = task == ImageTask.Saving
        _isImageLoading.value = task == ImageTask.Loading
        _isAiProcessing.value = task == ImageTask.Ai
        return componentScope.launch(start = CoroutineStart.DEFAULT) {
            try {
                block()
                ensureActive()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                if (onFailure != null) onFailure(failure) else reportProcessingFailure(failure)
            } finally {
                if (generation == taskGeneration) {
                    _isSaving.value = false
                    _isImageLoading.value = false
                    _isAiProcessing.value = false
                    _taskLabel.value = null
                }
            }
        }
    }

    private fun reportProcessingFailure(failure: Throwable) {
        failure.makeLog(LOG_TAG)
        val message = if (failure is ImageRenderLimitException) {
            appContext.getString(R.string.studio_processing_limit, failure.width, failure.height)
        } else {
            appContext.getString(R.string.studio_processing_failed, failure.message.orEmpty())
        }
        AppToastHost.showFailureToast(message)
    }

    private fun cancelImageTasks() {
        taskGeneration++
        listOf(savingJob, mergeJob, baseTransformJob, aiProcessJob, rasterizeJob,
            imageLoadingJob, generateStickerJob)
            .forEach { it?.cancel() }
        savingJob = null
        mergeJob = null
        baseTransformJob = null
        aiProcessJob = null
        rasterizeJob = null
        imageLoadingJob = null
        generateStickerJob = null
        _isGeneratingSticker.value = false
        _isSaving.value = false
        _isImageLoading.value = false
        _isAiProcessing.value = false
        _taskLabel.value = null
    }

    fun cancelAllImageTasks() {
        cancelImageTasks()
    }

    private fun softwareBitmap(bitmap: Bitmap): Bitmap =
        if (Build.VERSION.SDK_INT >= 26 && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
                ?: error("Could not create a software image")
        } else bitmap

    // ---------------- 首页「最近文件」(最近打开的图片文件,非可续编项目) ----------------

    /**
     * 最近打开/保存的图片文件,供首页「最近文件」横排展示(点击是开新项目,
     * 与上方可续编的项目列表不同)。
     * 数据来自公共最近访问表([RecentAccessRepository]):setUri 打开图片与
     * 保存成功时各记录一次,uri 为唯一键自动去重;失效 uri 由 UI 层加载失败时剔除。
     */
    private val _recentProjects: MutableState<List<RecentAccessEntity>> = mutableStateOf(emptyList())
    val recentProjects: List<RecentAccessEntity> by _recentProjects

    private fun observeRecentProjects() {
        componentScope.launch {
            recentAccessRepository
                .observeByType(RecentAccessRepository.TYPE_FILE, limit = RECENT_OBSERVE_LIMIT)
                .collect { entries ->
                    _recentProjects.update {
                        entries.filter { entity ->
                            IMAGE_EXTENSIONS.any {
                                entity.displayName.lowercase().endsWith(".$it")
                            }
                        }.take(RECENT_PROJECTS_LIMIT)
                    }
                }
        }
    }

    /**
     * 记录一次图片文件访问(最近打开/保存产物),uri 为唯一键自动去重。
     * 相机拍摄/粘贴缓存等临时 uri 直接跳过,其余失败静默(记录只是辅助行为)。
     */
    private fun recordRecentAccess(
        uri: Uri,
        displayName: String? = null
    ) {
        if (uri.isTransientUri()) return
        componentScope.launch(ioDispatcher) {
            runCatching {
                recentAccessRepository.recordAccess(
                    uri = uri.toString(),
                    displayName = displayName?.takeIf { it.isNotBlank() }
                        ?: uri.filename().orEmpty().ifBlank { FALLBACK_IMAGE_NAME },
                    accessType = RecentAccessRepository.TYPE_FILE,
                    pathHint = uri.path
                )
            }
        }
    }

    /** 相机拍摄(app FileProvider)与粘贴缓存(cacheDir 下 file://)等临时 uri 不进最近记录 */
    private fun Uri.isTransientUri(): Boolean =
        toString().contains(appContext.fileProviderAuthority) ||
            (scheme == ContentResolver.SCHEME_FILE &&
                path?.startsWith(appContext.cacheDir.absolutePath) == true)

    // ---------------- 图片加载 ----------------

    fun setUri(
        uri: Uri,
        onFailure: (Throwable) -> Unit,
    ) {
        if (isBusy) {
            _pendingAiOp.value = null
            return
        }
        imageLoadingJob = launchImageTask(
            R.string.studio_task_loading,
            ImageTask.Loading,
            onFailure = { failure ->
                _pendingAiOp.value = null
                onFailure(failure)
            }
        ) {
            if (hasImage && haveChanges &&
                !persistProject(projectId ?: error("No project ID"), projectName,
                    captureDocument(), documentRevision, interactive = false)
            ) {
                _pendingAiOp.value = null
                return@launchImageTask
            }
            val path = projectStore.importAsset(uri.toString())
            val (size, preview) = studioImageReader.preview(path)
            val format = ImageFormat[imageGetter.getExtension(uri.toString())].toExportFormat()
            ensureActive()
            beginProject(
                MarkupDocument(
                    baseUri = path,
                    width = size.width,
                    height = size.height,
                    layers = emptyList(),
                    originalUri = path,
                    sourceUri = uri.toString(),
                    exportSettings = ExportSettings(format = format)
                ),
                preview,
                uri.filename()?.substringBeforeLast('.')
            )
            recordRecentAccess(uri)
        }
    }

    private fun updateBitmap(bitmap: Bitmap?) {
        _bitmap.value = bitmap?.let(::softwareBitmap)
        filterPreviewJob = null
        _filterPreviewBitmap.value = null
        layerFilterJob = null
        layerFilterCache.clear()
        updateFilterPreview()
        syncLayerFilterCache()
    }

    private fun beginProject(document: MarkupDocument, preview: Bitmap, name: String? = null) {
        autosaveJob?.cancel()
        history.clear()
        endLayerEditSession()
        _selectedLayerId.value = null
        _selectedLayerIds.value = emptySet()
        _activeToolId.value = null
        projectId = UUID.randomUUID().toString()
        _projectName.value = name?.takeIf { it.isNotBlank() }
            ?: appContext.getString(R.string.studio_project_untitled)
        _projectSaveError.value = null
        _originalPreviewBitmap.value = preview
        applyDocument(document, preview)
        refreshHistoryFlags()
        registerDocumentChange()
    }

    override fun resetState() {
        if (hasImage && haveChanges) {
            closeProject()
        } else {
            clearEditorState()
        }
    }

    private fun clearEditorState() {
        cancelImageTasks()
        autosaveJob?.cancel()
        autosaveJob = null
        endEffectsChange()
        endLayerTransformChange()
        isCompoundEdit = false
        compositePreviewJob?.cancel()
        compositePreviewJob = null
        compositePreviewRevision = -1L
        documentRevision++
        projectId = null
        _projectName.value = ""
        _projectSaveError.value = null
        _originalUri.value = null
        _sourceUri.value = null
        _originalPreviewBitmap.value = null
        _compositePreview.value = null
        _maskTargetBitmap.value = null
        _isComparing.value = false
        _bitmap.value = null
        _uri.value = null
        _blankBaseBitmap.value = null
        _sourceSize.value = null
        aiProcessJob = null
        _layers.value = emptyList()
        _selectedLayerId.value = null
        _selectedLayerIds.value = emptySet()
        _activeToolId.value = null
        _baseAdjustments.value = BaseAdjustments()
        _selectedFilter.value = null
        _baseEffects.value = emptyList()
        _baseMask.value = null
        _baseTransforms.value = emptyList()
        _baseImageHistory.value = emptyList()
        _baseVisible.value = true
        _baseOrder.value = 0
        _pendingAiResult.value = null
        _aiGenerationTarget.value = AiGenerationTarget.NewLayer
        _filterPreviewBitmap.value = null
        layerFilterJob = null
        layerFilterCache.clear()
        rasterizeJob = null
        _canvasBackground.value = CanvasBackground.Default
        endLayerEditSession()
        history.clear()
        _canUndo.value = false
        _canRedo.value = false
        registerChangesCleared()
    }

    // ---------------- 导出(原图重绘,不再截图) ----------------

    private suspend fun renderDocument(document: MarkupDocument, preview: Boolean): Bitmap {
        val target = if (preview) {
            memoryBudget.previewSize(document.width, document.height)
        } else {
            document.exportSettings.toImageInfo(document.width, document.height).let {
                IntegerSize(it.width, it.height)
            }
        }
        memoryBudget.requireCanRender(target.width, target.height)
        val visibleLayers = document.layers.filter { it.transform.visible }
        val split = document.baseOrder.coerceIn(0, visibleLayers.size)
        val belowBase = visibleLayers.take(split)
        val aboveBase = visibleLayers.drop(split)
        // 背景隐藏时用透明底,不再解码底图;调节/效果/蒙版只作用于真实底图
        val preparedBase = if (document.baseVisible) {
            val source = studioImageReader.load(document.baseUri, target)
            effectsProcessor.apply(source, document.baseEffects, document.baseMask)
                .withBaseAdjustments(document.baseAdjustments)
        } else {
            Bitmap.createBitmap(target.width, target.height, Bitmap.Config.ARGB_8888)
        }
        // baseOrder > 0 时背景不再垫底:下层图层先绘到透明画布,盖上底图,再绘上层图层
        val composed = if (belowBase.isEmpty()) {
            preparedBase
        } else {
            val under = markupLayersApplier.applyToImage(
                image = Bitmap.createBitmap(target.width, target.height, Bitmap.Config.ARGB_8888),
                layers = belowBase,
                filteredLayers = emptyMap()
            )
            Canvas(under).drawBitmap(preparedBase, 0f, 0f, null)
            under
        }
        return markupLayersApplier.applyToImage(
            image = composed,
            layers = aboveBase,
            filteredLayers = emptyMap()
        )
    }

    private suspend fun renderResultBitmap(): Bitmap = renderDocument(captureDocument(), preview = false)

    private fun resultImageInfo(bitmap: Bitmap): ImageInfo =
        _exportSettings.value.toImageInfo(bitmap.width, bitmap.height).copy(
            width = bitmap.width,
            height = bitmap.height
        )

    fun prepareExportPreview() {
        scheduleCompositePreview(immediate = true)
    }

    private fun scheduleCompositePreview(immediate: Boolean = false) {
        if (!hasImage) return
        val document = captureDocument()
        val revision = documentRevision
        compositePreviewJob?.cancel()
        compositePreviewJob = componentScope.launch {
            try {
                if (!immediate) delay(400)
                val preview = renderDocument(document, preview = true)
                if (revision == documentRevision) {
                    _compositePreview.value = preview
                    compositePreviewRevision = revision
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                reportProcessingFailure(failure)
            }
        }
    }

    fun updateExportSettings(settings: ExportSettings) {
        if (settings == _exportSettings.value) return
        _exportSettings.value = settings
        _imageFormat.value = settings.format
        registerDocumentChange()
    }

    private fun onRenderFailed() {
        AppToastHost.showFailureToast(R.string.markup_export_failed_no_image)
    }

    fun saveBitmap(
        oneTimeSaveLocationUri: String?,
        onComplete: (saveResult: SaveResult) -> Unit,
    ) {
        // 快速连点防抖:保存/分享/复制共用 savingJob,进行中直接忽略新请求
        if (_isSaving.value) return
        savingJob = componentScope.launch {
            _isSaving.value = true
            val rendered = renderResultBitmap()
            if (rendered == null) {
                onRenderFailed()
                _isSaving.value = false
                return@launch
            }
            val settings = _exportSettings.value
            // rendered 已是导出分辨率(renderDocument 按 exportSettings 渲染),
            // 编码器只做格式/质量/体积控制,不再二次缩放
            val encoded = try {
                exportEncoder.encodeForExport(
                    image = rendered,
                    settings = settings.copy(resolution = ExportResolution.Original)
                )
            } catch (cancellation: CancellationException) {
                _isSaving.value = false
                throw cancellation
            } catch (failure: Exception) {
                reportProcessingFailure(failure)
                _isSaving.value = false
                return@launch
            }
            val imageInfo = encoded.imageInfo
            val overwrite = !settings.saveCopy && canOverwriteOriginal
            val saveResult = fileController.save(
                saveTarget = ImageSaveTarget(
                    imageInfo = imageInfo,
                    originalUri = if (overwrite) {
                        _sourceUri.value.orEmpty()
                    } else {
                        _uri.value?.toString().orEmpty()
                    },
                    sequenceNumber = null,
                    data = encoded.data,
                    overwriteOriginal = overwrite
                ),
                keepOriginalMetadata = _saveExif.value,
                oneTimeSaveLocationUri = oneTimeSaveLocationUri
            ).onSuccess(::registerSave)
            onComplete(saveResult)
            if (saveResult is SaveResult.Success) {
                // 保存产物记入「最近项目」(覆盖原图时 fileUri 即原 uri,等于刷新其时间戳)
                saveResult.fileUri?.let { recordRecentAccess(it.toUri(), saveResult.fileName) }
                if (_exportSettings.value.shareAfterSave) {
                    shareProvider.shareImage(
                        image = rendered,
                        imageInfo = imageInfo,
                        onComplete = {}
                    )
                }
            }
            _isSaving.value = false
        }
    }

    fun shareBitmap(onComplete: () -> Unit) {
        if (_isSaving.value) return
        savingJob = componentScope.launch {
            _isSaving.value = true
            val rendered = renderResultBitmap()
            if (rendered == null) {
                onRenderFailed()
                _isSaving.value = false
                return@launch
            }
            shareProvider.shareImage(
                image = rendered,
                imageInfo = resultImageInfo(rendered),
                onComplete = onComplete
            )
            _isSaving.value = false
        }
    }

    fun cacheCurrentImage(onComplete: (Uri) -> Unit) {
        if (_isSaving.value) return
        savingJob = componentScope.launch {
            _isSaving.value = true
            val rendered = renderResultBitmap()
            if (rendered == null) {
                onRenderFailed()
                _isSaving.value = false
                return@launch
            }
            shareProvider.cacheImage(
                image = rendered,
                imageInfo = resultImageInfo(rendered)
            )?.let { uri ->
                onComplete(uri.toUri())
            }
            _isSaving.value = false
        }
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    fun setImageFormat(imageFormat: ImageFormat) {
        _imageFormat.value = imageFormat
        registerChanges()
    }

    fun setSaveExif(bool: Boolean) {
        _saveExif.value = bool
        registerDocumentChange()
    }

    fun getFormatForFilenameSelection(): ImageFormat = imageFormat

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): MarkupLayersComponent
    }

}

/** 空白画布单边像素上限,防止自定义尺寸输入过大导致 OOM */
private const val MAX_CANVAS_SIDE = 8192

/**
 * 图层滤镜/蒙版预览位图缓存的总字节预算(所有处理中图层合计):
 * 超出时按 [syncLayerFilterCache] 的面积公式整体缩小缓存分辨率。
 * 以 2048 边预览底图(约 16 MiB/层)为参照,预算约 3 层满尺寸缓存。
 */
private const val LAYER_PREVIEW_CACHE_BYTES = 48L * 1024 * 1024

/** 首页「最近项目」展示条数上限 */
private const val RECENT_PROJECTS_LIMIT = 12

/** 观察最近访问表的拉取条数(过滤图片扩展名后再截取展示上限) */
private const val RECENT_OBSERVE_LIMIT = 50

/** 「最近项目」纳入展示的图片扩展名(小写比较) */
private val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

/** 无法从 uri 解析文件名时的兜底显示名 */
private const val FALLBACK_IMAGE_NAME = "图片"

/** 底图变换(裁剪/旋转/翻转)链路日志 tag */
private const val LOG_TAG = "MarkupBaseTransform"

/** AI 图像处理积分消耗/预检的来源标识 */
private const val AI_POINTS_SOURCE = "markup_ai"

/** AI 生成贴纸:提示词追加的贴纸风格描述(文生图模型对英文风格词更稳定) */
private const val STICKER_PROMPT_STYLE_SUFFIX = "sticker style, clean simple solid background"

/** AI 生成贴纸输出尺寸(正方形,服务商像素限制内的固定值) */
private const val STICKER_OUTPUT_SIZE = "1024*1024"
