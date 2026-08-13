package com.wanbaohe.markuplayers.presentation.screenLogic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageScaler
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import com.wanbaohe.markuplayers.domain.history.LayerHistory
import com.wanbaohe.markuplayers.domain.model.LayerBaseRemap
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.export.toExportFormat
import com.wanbaohe.markuplayers.presentation.render.IMAGE_LAYER_BASE_WIDTH_RATIO
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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
    private val markupLayersApplier: MarkupLayersApplier<Bitmap>
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialUri?.let {
                setUri(
                    uri = it,
                    onFailure = {}
                )
            }
        }
    }

    private val history = LayerHistory()

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _uri: MutableState<Uri?> = mutableStateOf(null)
    val uri: Uri? by _uri

    val hasImage: Boolean get() = _bitmap.value != null

    private val _layers: MutableState<List<MarkupLayer>> = mutableStateOf(emptyList())
    val layers: List<MarkupLayer> by _layers

    private val _selectedLayerId: MutableState<String?> = mutableStateOf(null)
    val selectedLayerId: String? by _selectedLayerId

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

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    private var mergeJob: Job? by smartJob {
        _isSaving.update { false }
    }

    private var baseTransformJob: Job? by smartJob {
        _isSaving.update { false }
    }

    // ---------------- 基础调节(亮度/对比度/饱和度) ----------------

    /**
     * 基础调节参数,不进图层 undo 历史;预览经 colorFilter 实时生效,
     * 导出时在画图层之前烘焙进全分辨率位图。
     */
    private val _baseAdjustments: MutableState<BaseAdjustments> = mutableStateOf(BaseAdjustments())
    val baseAdjustments: BaseAdjustments by _baseAdjustments

    fun updateBaseAdjustments(adjustments: BaseAdjustments) {
        if (adjustments == _baseAdjustments.value) return
        _baseAdjustments.value = adjustments
        registerChanges()
    }

    fun resetBaseAdjustments() = updateBaseAdjustments(BaseAdjustments())

    // ---------------- 裁剪 / 旋转 / 翻转(作用于底图) ----------------

    /**
     * 确认裁剪旋转变换:把 90° 步进 + 自由旋转 + 翻转 + 裁剪框合成一次变换,
     * 作用于全分辨率原图,缓存后替换底图(保留图层与历史,不走 [setUri])。
     *
     * 图层结算:自由旋转角为 0 时按同一仿射精确重映射([LayerBaseRemap]);
     * 非 0 时先把可见图层烘焙进底图再变换并清空图层列表。图层变更只记一次
     * undo 快照(底图本身的变化不可撤销,快照用于恢复变换前的图层列表)。
     */
    fun applyBaseTransform(
        rotationSteps: Int,
        freeRotation: Float,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
        cropRect: NormalizedRect,
    ) {
        val currentUri = _uri.value ?: return
        val oldSize = _sourceSize.value ?: return
        val steps = ((rotationSteps % 4) + 4) % 4
        val isIdentity = steps == 0 && freeRotation == 0f &&
            !flipHorizontal && !flipVertical && cropRect == NormalizedRect.Full
        if (isIdentity) return

        baseTransformJob = componentScope.launch {
            _isSaving.value = true
            val outcome = runCatching {
                withContext(defaultDispatcher) {
                    var source = imageGetter.getImage(data = currentUri)
                        ?: error("source image is null")
                    // 自由旋转下精确重映射图层成本高:先烘焙可见图层,确认后清空
                    val bakeLayers = freeRotation != 0f && _layers.value.isNotEmpty()
                    if (bakeLayers) {
                        source = markupLayersApplier.applyToImage(
                            image = source,
                            layers = _layers.value.filter { it.transform.visible }
                        )
                    }
                    val transformed = source.transformed(
                        degrees = steps * 90f + freeRotation,
                        flipHorizontal = flipHorizontal,
                        flipVertical = flipVertical
                    )
                    transformed.cropped(cropRect) to bakeLayers
                }
            }.getOrNull()
            val (result, baked) = outcome ?: run {
                AppToastHost.showFailureToast(R.string.markup_base_transform_failed)
                _isSaving.value = false
                return@launch
            }
            val cachedUri = shareProvider.cacheImage(
                image = result,
                imageInfo = ImageInfo(
                    width = result.width,
                    height = result.height,
                    imageFormat = ImageFormat.Png.Lossless
                )
            )?.toUri()
            if (cachedUri == null) {
                AppToastHost.showFailureToast(R.string.markup_base_transform_failed)
                _isSaving.value = false
                return@launch
            }

            if (_layers.value.isNotEmpty()) history.snapshot(_layers.value)
            _uri.value = cachedUri
            _sourceSize.value = IntSize(result.width, result.height)
            _imageFormat.update { ImageFormat.Png.Lossless }
            _exportSettings.update {
                it.copy(format = ImageFormat.Png.Lossless.toExportFormat())
            }
            _layers.value = if (baked) {
                emptyList()
            } else {
                LayerBaseRemap.remap(
                    layers = _layers.value,
                    oldWidth = oldSize.width,
                    oldHeight = oldSize.height,
                    rotationSteps = steps,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical,
                    cropRect = cropRect
                )
            }
            _selectedLayerId.value = null
            if (baked) AppToastHost.showToast(R.string.markup_layers_merged)
            updateBitmap(result)
            onLayersChanged()
            _isSaving.value = false
        }
    }

    /** 翻转 + 旋转合成一次矩阵变换;恒等时返回原位图 */
    private fun Bitmap.transformed(
        degrees: Float,
        flipHorizontal: Boolean,
        flipVertical: Boolean,
    ): Bitmap {
        val normalizedDegrees = ((degrees % 360f) + 360f) % 360f
        if (normalizedDegrees == 0f && !flipHorizontal && !flipVertical) return this
        val matrix = Matrix().apply {
            postScale(
                if (flipHorizontal) -1f else 1f,
                if (flipVertical) -1f else 1f,
                width / 2f,
                height / 2f
            )
            postRotate(degrees)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /** 按归一化裁剪框抠图;框即整图时返回原位图 */
    private fun Bitmap.cropped(rect: NormalizedRect): Bitmap {
        if (rect == NormalizedRect.Full) return this
        val left = (rect.left * width).roundToInt().coerceIn(0, width - 1)
        val top = (rect.top * height).roundToInt().coerceIn(0, height - 1)
        val cropWidth = (rect.width * width).roundToInt().coerceIn(1, width - left)
        val cropHeight = (rect.height * height).roundToInt().coerceIn(1, height - top)
        if (left == 0 && top == 0 && cropWidth == width && cropHeight == height) return this
        return Bitmap.createBitmap(this, left, top, cropWidth, cropHeight)
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

    // ---------------- 图层操作(修改前一律先记快照) ----------------

    fun addLayer(layer: MarkupLayer) {
        history.snapshot(_layers.value)
        _layers.update { it + layer }
        _selectedLayerId.value = layer.id
        onLayersChanged()
    }

    fun updateLayer(
        id: String,
        transform: (MarkupLayer) -> MarkupLayer
    ) {
        if (_layers.value.none { it.id == id }) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.map { if (it.id == id) transform(it) else it } }
        onLayersChanged()
    }

    fun updateLayerTransform(
        id: String,
        transform: LayerTransform
    ) = updateLayer(id) { it.copy(transform = transform) }

    fun removeLayer(id: String) {
        if (_layers.value.none { it.id == id }) return
        history.snapshot(_layers.value)
        _layers.update { list -> list.filterNot { it.id == id } }
        if (_selectedLayerId.value == id) _selectedLayerId.value = null
        onLayersChanged()
    }

    fun duplicateLayer(id: String) {
        val source = _layers.value.find { it.id == id } ?: return
        history.snapshot(_layers.value)
        val copy = source.copy(id = java.util.UUID.randomUUID().toString())
        _layers.update { list ->
            list.toMutableList().apply { add(list.indexOf(source) + 1, copy) }
        }
        _selectedLayerId.value = copy.id
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
        registerChanges()
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
        registerChanges()
    }

    /** 混合模式,本期仅 [LayerBlendMode.Normal],先留接口 */
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
     */
    fun mergeLayerDown(id: String) {
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
        val size = _sourceSize.value
            ?: _bitmap.value?.let { IntSize(it.width, it.height) }
        if (size == null || size.width <= 0 || size.height <= 0) return

        mergeJob = componentScope.launch {
            _isSaving.value = true
            val cachedUri = runCatching {
                val base = Bitmap.createBitmap(
                    size.width,
                    size.height,
                    Bitmap.Config.ARGB_8888
                )
                val baked = markupLayersApplier.applyToImage(
                    image = base,
                    layers = listOf(lowerLayer, layer)
                )
                shareProvider.cacheImage(
                    image = baked,
                    imageInfo = ImageInfo(
                        width = baked.width,
                        height = baked.height,
                        imageFormat = ImageFormat.Png.Lossless
                    )
                )
            }.getOrNull()
            if (cachedUri == null) {
                AppToastHost.showFailureToast(R.string.markup_merge_failed)
                _isSaving.value = false
                return@launch
            }
            history.snapshot(_layers.value)
            val mergedLayer = MarkupLayer(
                type = LayerType.Image(imageData = cachedUri.toUri()),
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
            onLayersChanged()
            _isSaving.value = false
        }
    }

    fun reorderLayers(layers: List<MarkupLayer>) {
        if (layers == _layers.value) return
        history.snapshot(_layers.value)
        _layers.value = layers
        onLayersChanged()
    }

    fun clearLayers() {
        if (_layers.value.isEmpty()) return
        history.snapshot(_layers.value)
        _layers.value = emptyList()
        _selectedLayerId.value = null
        onLayersChanged()
    }

    fun selectLayer(id: String?) {
        _selectedLayerId.value = id
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
        val preEditLayers: List<MarkupLayer>,
        val createdLayerId: String?,
    )

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
        val targetId: String
        if (layerId == null) {
            val layer = MarkupLayer(type = LayerType.Text.Default.copy(text = ""))
            addLayer(layer)
            targetId = layer.id
        } else {
            val layer = _layers.value.find { it.id == layerId }
            if (layer == null || layer.type !is LayerType.Text) return
            targetId = layerId
        }
        editSession = LayerEditSession(
            preEditLayers = _layers.value,
            createdLayerId = targetId.takeIf { layerId == null }
        )
        _editSessionLayerId.value = targetId
        _selectedLayerId.value = targetId
    }

    /** 会话内图层修改:直接改值不入历史,确认/取消时统一结算 */
    fun updateLayerInEditSession(
        id: String,
        transform: (MarkupLayer) -> MarkupLayer
    ) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list -> list.map { if (it.id == id) transform(it) else it } }
        registerChanges()
    }

    /** 会话内删除图层(不入历史):随后接 [commitLayerEditSession],undo 一次即可恢复 */
    fun removeLayerInEditSession(id: String) {
        if (_layers.value.none { it.id == id }) return
        _layers.update { list -> list.filterNot { it.id == id } }
        registerChanges()
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
        registerChanges()
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
            history.snapshot(session.preEditLayers)
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
        val previous = history.undo(_layers.value) ?: return
        _layers.value = previous
        pruneSelection()
        onLayersChanged()
    }

    fun redo() {
        val next = history.redo(_layers.value) ?: return
        _layers.value = next
        pruneSelection()
        onLayersChanged()
    }

    private fun pruneSelection() {
        if (_selectedLayerId.value !in _layers.value.map { it.id }) {
            _selectedLayerId.value = null
        }
    }

    private fun onLayersChanged() {
        _canUndo.value = history.canUndo
        _canRedo.value = history.canRedo
        registerChanges()
    }

    // ---------------- 图片加载 ----------------

    fun setUri(
        uri: Uri,
        onFailure: (Throwable) -> Unit,
    ) {
        componentScope.launch {
            _layers.value = emptyList()
            _selectedLayerId.value = null
            _baseAdjustments.value = BaseAdjustments()
            history.clear()
            _canUndo.value = false
            _canRedo.value = false
            _isImageLoading.value = true

            _uri.value = uri
            imageGetter.getImageAsync(
                uri = uri.toString(),
                originalSize = true,
                onGetImage = { data ->
                    _sourceSize.value = IntSize(data.image.width, data.image.height)
                    _imageFormat.update { data.imageInfo.imageFormat }
                    _exportSettings.update {
                        it.copy(format = data.imageInfo.imageFormat.toExportFormat())
                    }
                    updateBitmap(data.image)
                },
                onFailure = onFailure
            )
        }
    }

    private fun updateBitmap(bitmap: Bitmap?) {
        componentScope.launch {
            _isImageLoading.value = true
            _bitmap.value = imageScaler.scaleUntilCanShow(bitmap)
            _isImageLoading.value = false
        }
    }

    override fun resetState() {
        _bitmap.value = null
        _uri.value = null
        _sourceSize.value = null
        _layers.value = emptyList()
        _selectedLayerId.value = null
        _activeToolId.value = null
        _baseAdjustments.value = BaseAdjustments()
        endLayerEditSession()
        history.clear()
        _canUndo.value = false
        _canRedo.value = false
        registerChangesCleared()
    }

    // ---------------- 导出(原图重绘,不再截图) ----------------

    private suspend fun renderResultBitmap(): Bitmap? {
        val source = _uri.value?.let { imageGetter.getImage(data = it) } ?: return null
        return markupLayersApplier.applyToImage(
            image = source.withBaseAdjustments(_baseAdjustments.value),
            layers = _layers.value.filter { it.transform.visible }
        )
    }

    private fun resultImageInfo(bitmap: Bitmap): ImageInfo =
        _exportSettings.value.toImageInfo(
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height
        )

    fun updateExportSettings(settings: ExportSettings) {
        _exportSettings.value = settings
    }

    private fun onRenderFailed() {
        AppToastHost.showFailureToast(R.string.markup_export_failed_no_image)
    }

    fun saveBitmap(
        oneTimeSaveLocationUri: String?,
        onComplete: (saveResult: SaveResult) -> Unit,
    ) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            val rendered = renderResultBitmap()
            if (rendered == null) {
                onRenderFailed()
                _isSaving.value = false
                return@launch
            }
            val imageInfo = resultImageInfo(rendered)
            val saveResult = fileController.save(
                saveTarget = ImageSaveTarget(
                    imageInfo = imageInfo,
                    originalUri = _uri.value.toString(),
                    sequenceNumber = null,
                    data = imageCompressor.compressAndTransform(
                        image = rendered,
                        imageInfo = imageInfo
                    )
                ),
                keepOriginalMetadata = _saveExif.value,
                oneTimeSaveLocationUri = oneTimeSaveLocationUri
            ).onSuccess(::registerSave)
            onComplete(saveResult)
            if (saveResult is SaveResult.Success && _exportSettings.value.shareAfterSave) {
                shareProvider.shareImage(
                    image = rendered,
                    imageInfo = imageInfo,
                    onComplete = {}
                )
            }
            _isSaving.value = false
        }
    }

    fun shareBitmap(onComplete: () -> Unit) {
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
        registerChanges()
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
