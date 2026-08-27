package com.wanbaohe.textcard.presentation.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoader
import com.shifenmiao.interfaces.logging.ImageSaveLogger
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.CustomCanvasStore
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.TextCardPaperRepository
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.ImageElementSpec
import com.wanbaohe.textcard.domain.model.RemotePaper
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 底部编辑面板 */
enum class EditorPanel {
    Basic, Background, TextStyle, Layers
}

/**
 * 文字卡片页面组件(设计稿「文字卡片」)。
 *
 * [canvas] 为 null 时显示选择画布页,非空时进入编辑页(单 Component 双页面,
 * 同 markup-layers 的 hasImage 模式)。离散修改直接 set,不建历史栈(范围裁剪:
 * 不做撤销/重做)。字体选择走全局共享的 PickFontFamilySheet(core/ui)。
 */
class TextCardComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val exportRenderer: TextCardExportRenderer,
    private val paperRepository: TextCardPaperRepository,
    private val customCanvasStore: CustomCanvasStore,
    private val imageSaveLogger: ImageSaveLogger,
    private val imageGenerationLoader: ImageGenerationLoader,
) : BaseComponent(dispatchersHolder, componentContext) {

    // ---------------- 画布与卡片状态 ----------------

    private val _canvas: MutableState<CanvasSpec?> = mutableStateOf(null)
    val canvas: CanvasSpec? by _canvas

    /** 选择画布页中选中的规格(未开始制作前) */
    private val _pendingCanvas: MutableState<CanvasSpec> = mutableStateOf(CanvasSpec.Xiaohongshu)
    val pendingCanvas: CanvasSpec by _pendingCanvas

    /** 上次自定义画布尺寸(持久化记忆,null = 从未自定义) */
    private val _lastCustomCanvas: MutableState<CanvasSpec.Custom?> =
        mutableStateOf(customCanvasStore.lastCustom()?.let { (w, h) ->
            CanvasSpec.Custom(w, h)
        })
    val lastCustomCanvas: CanvasSpec.Custom? by _lastCustomCanvas

    private val _background: MutableState<BackgroundSpec> = mutableStateOf(GradientPresets.default)
    val background: BackgroundSpec by _background

    private val _backgroundOpacity: MutableState<Float> = mutableStateOf(1f)
    val backgroundOpacity: Float by _backgroundOpacity

    /** 文字块列表(任意多条;默认标题 + 正文两块),列表顺序即绘制顺序 */
    private val _textBlocks: MutableState<List<TextBlock>> = mutableStateOf(
        listOf(
            TextBlock(
                content = appContext.getString(R.string.textcard_default_title),
                baseSizeRatio = CardLayout.TITLE_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
                isBold = true
            ),
            TextBlock(
                content = appContext.getString(R.string.textcard_default_body),
                baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
                baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO
            )
        )
    )
    val textBlocks: List<TextBlock> by _textBlocks

    /** 装饰贴纸列表(支持多个;每个装饰一个图层) */
    private val _decorations: MutableState<List<DecorationSpec>> = mutableStateOf(emptyList())
    val decorations: List<DecorationSpec> by _decorations

    /** AI 生成图片元素列表(支持多个;每个图片一个图层) */
    private val _imageElements: MutableState<List<ImageElementSpec>> = mutableStateOf(emptyList())
    val imageElements: List<ImageElementSpec> by _imageElements

    /** AI 生图进行中(生成图片图层弹窗的 loading) */
    private val _isGeneratingImage: MutableState<Boolean> = mutableStateOf(false)
    val isGeneratingImage: Boolean by _isGeneratingImage

    /** 元素图层(文字块/装饰,列表顺序即 z 序、底层在前);背景钉在最底不进列表 */
    private val _elementLayers: MutableState<List<ElementLayer>> =
        mutableStateOf(
            _textBlocks.value.map { ElementLayer(elementId = it.id, kind = ElementLayer.Kind.Text) }
        )
    val elementLayers: List<ElementLayer> by _elementLayers

    /** 背景层显隐(钉在最底,不参与排序) */
    private val _backgroundVisible: MutableState<Boolean> = mutableStateOf(true)
    val backgroundVisible: Boolean by _backgroundVisible

    private val _activePanel: MutableState<EditorPanel?> = mutableStateOf(null)
    val activePanel: EditorPanel? by _activePanel

    /**
     * 「当前选中文字块」单一事实源:画布点选(selectElement 同步)、面板按钮组
     * 切换(selectTextBlock 同步)、字体应用目标(selectedFontTarget)全部读写它。
     */
    private val _selectedTextBlockId: MutableState<String> =
        mutableStateOf(_textBlocks.value.first().id)
    val selectedTextBlockId: String by _selectedTextBlockId

    /** 画布当前选中的元素 id(文字块或装饰),null = 未选中 */
    private val _selectedElementId: MutableState<String?> = mutableStateOf(null)
    val selectedElementId: String? by _selectedElementId

    /** 就地编辑中的文字块 id,null = 非编辑态(编辑内容实时写回对应块) */
    private val _editingTextBlockId: MutableState<String?> = mutableStateOf(null)
    val editingTextBlockId: String? by _editingTextBlockId

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    // ---------------- 画布选择 ----------------

    fun selectPendingCanvas(spec: CanvasSpec) {
        _pendingCanvas.value = spec
    }

    /** 自定义画布确认:钳制 256..4096,持久化并选中该规格 */
    fun selectCustomCanvas(width: Int, height: Int) {
        val safeWidth = width.coerceIn(256, 4096)
        val safeHeight = height.coerceIn(256, 4096)
        val custom = CanvasSpec.Custom(safeWidth, safeHeight)
        customCanvasStore.saveLastCustom(safeWidth, safeHeight)
        _lastCustomCanvas.value = custom
        _pendingCanvas.value = custom
    }

    /** 「开始制作」:进入编辑页;拉取远程纸张(失败静默降级) */
    fun startEditing() {
        _canvas.value = _pendingCanvas.value
        loadRemotePapers()
        registerChanges()
    }

    /** 编辑页返回:回到选择画布页 */
    fun backToSelection() {
        _canvas.value = null
        _activePanel.value = null
        _selectedElementId.value = null
        _editingTextBlockId.value = null
    }

    override fun resetState() = onGoBack()

    // ---------------- 背景 ----------------

    fun updateBackground(spec: BackgroundSpec) {
        if (spec == _background.value) return
        _background.value = spec
        registerChanges()
    }

    fun updateBackgroundOpacity(opacity: Float) {
        _backgroundOpacity.value = opacity.coerceIn(0f, 1f)
        registerChanges()
    }

    // ---------------- 文字(任意多块,按 id 寻址) ----------------

    /** 面板按钮组切换选中块:与画布元素选中同步(选中框高亮同一块) */
    fun selectTextBlock(id: String) {
        if (_textBlocks.value.none { it.id == id }) return
        _selectedTextBlockId.value = id
        _selectedElementId.value = id
    }

    fun selectedTextBlock(): TextBlock? =
        _textBlocks.value.find { it.id == _selectedTextBlockId.value }
            ?: _textBlocks.value.firstOrNull()

    fun updateTextBlock(id: String, transform: (TextBlock) -> TextBlock) {
        if (_textBlocks.value.none { it.id == id }) return
        _textBlocks.update { list -> list.map { if (it.id == id) transform(it) else it } }
        registerChanges()
    }

    /** 新增文字块:正文样式,默认排在现有最后一块基准往下,同步建图层 */
    fun addTextBlock() {
        val last = _textBlocks.value.lastOrNull()
        val block = TextBlock(
            content = appContext.getString(R.string.textcard_new_block_content),
            baseSizeRatio = CardLayout.BODY_BASE_SIZE_RATIO,
            baseTopRatio = ((last?.baseTopRatio ?: CardLayout.BODY_BASE_TOP_RATIO) +
                CardLayout.NEW_BLOCK_STEP_RATIO).coerceAtMost(0.9f)
        )
        _textBlocks.update { it + block }
        _elementLayers.update { it + ElementLayer(block.id, ElementLayer.Kind.Text) }
        _selectedTextBlockId.value = block.id
        _selectedElementId.value = block.id
        registerChanges()
    }

    /** 删除文字块:至少保留一块,同步移除图层 */
    fun removeTextBlock(id: String) {
        if (_textBlocks.value.size <= 1) return
        _textBlocks.update { list -> list.filterNot { it.id == id } }
        _elementLayers.update { list -> list.filterNot { it.elementId == id } }
        if (_selectedTextBlockId.value == id) {
            _selectedTextBlockId.value = _textBlocks.value.first().id
        }
        if (_selectedElementId.value == id) _selectedElementId.value = null
        if (_editingTextBlockId.value == id) _editingTextBlockId.value = null
        registerChanges()
    }

    /** 画布内拖动自定义背景图:仅背景为相册图时生效 */
    fun updateBackgroundImageOffset(dx: Float, dy: Float) {
        val background = _background.value as? BackgroundSpec.Image ?: return
        _background.value = background.copy(
            offsetX = (background.offsetX + dx).coerceIn(-1f, 1f),
            offsetY = (background.offsetY + dy).coerceIn(-1f, 1f)
        )
        registerChanges()
    }

    /** 字体应用目标:当前选中文字块(面板切换与画布点选都汇聚到它) */
    fun selectedFontTarget(): TextBlock? = selectedTextBlock()

    /** 字体作用于当前选中文字块 */
    fun applyFont(font: FontType?) {
        val target = selectedFontTarget() ?: return
        updateTextBlock(target.id) { it.copy(font = font) }
    }

    // ---------------- 元素选中与变换(文字块/装饰通用) ----------------

    /** 画布点按元素=选中;点空白传 null 取消选中。选中文字块时同步为样式面板目标 */
    fun selectElement(id: String?) {
        _selectedElementId.value = id
        if (id != null && _textBlocks.value.any { it.id == id }) {
            _selectedTextBlockId.value = id
        }
    }

    /** 进入就地编辑态(再次点已选中的文字块):选中并标记编辑中 */
    fun beginTextEdit(id: String) {
        if (_textBlocks.value.none { it.id == id }) return
        selectElement(id)
        _editingTextBlockId.value = id
    }

    /**
     * 提交并退出就地编辑:trim 内容,空内容置占位提示文案(不删块)。
     * 编辑中内容实时写回,这里只做收尾。
     */
    fun endTextEdit() {
        val id = _editingTextBlockId.value ?: return
        updateTextBlock(id) { block ->
            val trimmed = block.content.trim()
            if (trimmed.isEmpty()) {
                block.copy(content = appContext.getString(R.string.textcard_empty_text_hint))
            } else {
                block.copy(content = trimmed)
            }
        }
        _editingTextBlockId.value = null
    }

    /**
     * 应用手势后的元素变换(绝对值,预览侧已完成 pan 的缩放/旋转换算与归一化)。
     * 锁定图层忽略。
     */
    fun setElementTransform(
        id: String,
        offsetX: Float,
        offsetY: Float,
        scale: Float,
        rotation: Float,
    ) {
        val layer = _elementLayers.value.find { it.elementId == id }
        if (layer?.locked == true) return
        val safeScale = scale.coerceIn(0.2f, 5f)
        when {
            _textBlocks.value.any { it.id == id } -> updateTextBlock(id) {
                it.copy(
                    offsetX = offsetX.coerceIn(-1f, 1f),
                    offsetY = offsetY.coerceIn(-1f, 1f),
                    scale = safeScale,
                    rotation = rotation
                )
            }

            _decorations.value.any { it.id == id } -> {
                _decorations.update { list ->
                    list.map {
                        if (it.id == id) {
                            it.copy(
                                offsetX = offsetX.coerceIn(-0.5f, 1f),
                                offsetY = offsetY.coerceIn(-0.5f, 1f),
                                scale = safeScale,
                                rotation = rotation
                            )
                        } else it
                    }
                }
                registerChanges()
            }

            _imageElements.value.any { it.id == id } -> {
                _imageElements.update { list ->
                    list.map {
                        if (it.id == id) {
                            it.copy(
                                offsetX = offsetX.coerceIn(-0.5f, 1f),
                                offsetY = offsetY.coerceIn(-0.5f, 1f),
                                scale = safeScale,
                                rotation = rotation
                            )
                        } else it
                    }
                }
                registerChanges()
            }
        }
    }

    /**
     * 文字框尺寸调整(拖 8 向手柄):框宽/最小框高 + 锚点补偿后的位置偏移,绝对值。
     * 只改框尺寸不改字号,文字在新框宽内折行;锁定图层忽略。
     */
    fun setTextBlockBounds(
        id: String,
        widthRatio: Float,
        heightRatio: Float,
        offsetX: Float,
        offsetY: Float,
    ) {
        if (_elementLayers.value.find { it.elementId == id }?.locked == true) return
        updateTextBlock(id) {
            it.copy(
                widthRatio = widthRatio.coerceIn(CardLayout.MIN_TEXT_WIDTH_RATIO, 1f),
                heightRatio = heightRatio.coerceIn(0f, 1f),
                offsetX = offsetX.coerceIn(-1f, 1f),
                offsetY = offsetY.coerceIn(-1f, 1f)
            )
        }
    }

    /** 删除选中元素(文字块至少保留一块,装饰/图片不限),同步移除图层 */
    fun removeElement(id: String) {
        when {
            _textBlocks.value.any { it.id == id } -> removeTextBlock(id)
            _decorations.value.any { it.id == id } -> {
                _decorations.update { list -> list.filterNot { it.id == id } }
                _elementLayers.update { list -> list.filterNot { it.elementId == id } }
                if (_selectedElementId.value == id) _selectedElementId.value = null
                registerChanges()
            }

            _imageElements.value.any { it.id == id } -> {
                _imageElements.update { list -> list.filterNot { it.id == id } }
                _elementLayers.update { list -> list.filterNot { it.elementId == id } }
                if (_selectedElementId.value == id) _selectedElementId.value = null
                registerChanges()
            }
        }
    }

    // ---------------- 装饰 ----------------

    /** 添加装饰:默认落右下角,置顶(图层列表尾部)并选中 */
    fun addDecoration(emojiIndex: Int) {
        val canvas = _canvas.value ?: return
        val decoration = DecorationSpec.defaultPositionFor(canvas, emojiIndex)
        _decorations.update { it + decoration }
        _elementLayers.update { it + ElementLayer(decoration.id, ElementLayer.Kind.Decoration) }
        _selectedElementId.value = decoration.id
        registerChanges()
    }

    /** 清空所有装饰(装饰选择 Sheet 的「无」) */
    fun clearDecorations() {
        if (_decorations.value.isEmpty()) return
        val ids = _decorations.value.map { it.id }.toSet()
        _decorations.value = emptyList()
        _elementLayers.update { list -> list.filterNot { it.elementId in ids } }
        if (_selectedElementId.value in ids) _selectedElementId.value = null
        registerChanges()
    }

    // ---------------- AI 生成图片图层(core:image-generation) ----------------

    private var generateImageJob: Job? by smartJob {
        _isGeneratingImage.update { false }
    }

    /**
     * 生成图片图层:走 image-generation 的活动配置(默认代理通道,无需用户配置),
     * 成功取首图新增为图片元素并选中;失败 toast 展示原因。[onSuccess] 供弹窗自关。
     */
    fun generateImageLayer(prompt: String, onSuccess: () -> Unit = {}) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            AppToastHost.showToast(
                message = appContext.getString(R.string.textcard_generate_image_empty),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                duration = ToastDuration.Short
            )
            return
        }
        if (_isGeneratingImage.value) return
        generateImageJob = componentScope.launch {
            _isGeneratingImage.value = true
            imageGenerationLoader.load(trimmed)
                .onSuccess { image ->
                    addImageElement(image.file.absolutePath)
                    // 仅真实生成成功扣积分；复用本地缓存不重复扣费。
                    if (!image.fromCache) {
                        BaseUtils.consumePoints(
                            degree = aiImageProcessPointsCost(),
                            desc = appContext.getString(R.string.textcard_add_image_layer),
                            source = AI_IMAGE_POINTS_SOURCE,
                            showToast = true
                        )
                    }
                    onSuccess()
                }
                .onFailure { AppToastHost.showFailureToast(throwable = it) }
            _isGeneratingImage.value = false
        }
    }

    fun cancelGeneratingImage() {
        generateImageJob = null
    }

    /** 新增图片元素:默认落画布中心,置顶(图层列表尾部)并选中 */
    private fun addImageElement(uri: String) {
        val canvas = _canvas.value ?: return
        val element = ImageElementSpec.defaultPositionFor(canvas, uri)
        _imageElements.update { it + element }
        _elementLayers.update { it + ElementLayer(element.id, ElementLayer.Kind.Image) }
        _selectedElementId.value = element.id
        registerChanges()
    }

    // ---------------- 图层(元素层,背景钉最底另算) ----------------

    fun toggleBackgroundVisible() {
        _backgroundVisible.update { !it }
        registerChanges()
    }

    fun toggleLayerVisible(elementId: String) {
        updateElementLayer(elementId) {
            if (it.locked) it else it.copy(visible = !it.visible)
        }
    }

    fun toggleLayerLocked(elementId: String) {
        updateElementLayer(elementId) { it.copy(locked = !it.locked) }
    }

    private fun updateElementLayer(elementId: String, transform: (ElementLayer) -> ElementLayer) {
        if (_elementLayers.value.none { it.elementId == elementId }) return
        _elementLayers.update { list ->
            list.map { if (it.elementId == elementId) transform(it) else it }
        }
        registerChanges()
    }

    /** 拖拽排序提交:参数为完整新 z 序(底层在前,仅文字/装饰层) */
    fun reorderLayers(newOrder: List<ElementLayer>) {
        if (newOrder == _elementLayers.value || newOrder.size != _elementLayers.value.size) return
        _elementLayers.value = newOrder
        registerChanges()
    }

    // ---------------- 底部面板 ----------------

    fun togglePanel(panel: EditorPanel) {
        setActivePanel(if (_activePanel.value == panel) null else panel)
    }

    fun setActivePanel(panel: EditorPanel?) {
        _activePanel.value = panel
    }

    // ---------------- 远程纸张(Strapi 可配,失败/无网静默降级) ----------------

    /** 已就绪(图片已下载到本地)的远程纸张,追加在纸张列表尾部 */
    private val _remotePapers: MutableState<List<RemotePaper>> = mutableStateOf(emptyList())
    val remotePapers: List<RemotePaper> by _remotePapers

    private var papersJob: Job? by smartJob()

    fun loadRemotePapers() {
        if (papersJob != null || _remotePapers.value.isNotEmpty()) return
        papersJob = componentScope.launch {
            _remotePapers.value = paperRepository.loadLocalPapers()
        }
    }

    // ---------------- 渲染与保存 ----------------

    fun renderState(): TextCardRenderState? {
        val spec = _canvas.value ?: return null
        return TextCardRenderState(
            canvas = spec,
            background = _background.value,
            backgroundOpacity = _backgroundOpacity.value,
            backgroundVisible = _backgroundVisible.value,
            textBlocks = _textBlocks.value,
            decorations = _decorations.value,
            imageElements = _imageElements.value,
            layers = _elementLayers.value
        )
    }

    /**
     * 保存:离屏重绘 1080 宽 PNG,经 FileController 落盘(模板见 id-photo)。
     * 结果处理不走 parseSaveResult:其内部异步读当前页面名,保存后切页会把
     * 活动记录标成别的页面;这里同步显式记录为本页面(图文卡片)。
     */
    fun saveCard() {
        // 导出长边超上限(2048)时等比缩小渲染,防 OOM;预览仍按原规格
        val state = renderState()?.let { it.copy(canvas = it.canvas.exportScaled()) } ?: return
        if (_isSaving.value) return
        savingJob = componentScope.launch {
            _isSaving.value = true
            val result = runCatching {
                val rendered = withContext(defaultDispatcher) {
                    exportRenderer.render(state)
                }
                val imageInfo = ImageInfo(
                    imageFormat = ImageFormat.Png.Lossless,
                    width = rendered.width,
                    height = rendered.height
                )
                fileController.save(
                    saveTarget = ImageSaveTarget(
                        imageInfo = imageInfo,
                        originalUri = "text_card",
                        sequenceNumber = null,
                        metadata = null,
                        filename = "TextCard_${System.currentTimeMillis()}",
                        data = imageCompressor.compressAndTransform(
                            image = rendered,
                            imageInfo = imageInfo
                        )
                    ),
                    keepOriginalMetadata = false,
                    oneTimeSaveLocationUri = null
                ).onSuccess { success -> registerSave(success) }
            }.getOrElse { SaveResult.Error.Exception(it) }

            handleSaveResult(result)
            _isSaving.value = false
        }
    }

    /** parseSaveResult 的等价行为 + 显式 ActivityLog(页面名固定为图文卡片) */
    private suspend fun handleSaveResult(result: SaveResult) {
        when (result) {
            is SaveResult.Success -> {
                result.message?.let {
                    AppToastHost.showToast(
                        message = it,
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                        duration = ToastDuration.Long
                    )
                }
                AppToastHost.showConfetti()
                runCatching {
                    imageSaveLogger.recordImageSave(
                        screenId = Screen.TextCard.id.toString(),
                        screenName = appContext.getString(
                            com.shifenmiao.core.R.string.text_card
                        ),
                        description = linkifiedSaveMessage(result).orEmpty(),
                        fileUri = result.fileUri.orEmpty(),
                        fileName = result.fileName.orEmpty(),
                        savePath = result.savingPath.orEmpty()
                    )
                }.onFailure { it.makeLog("TextCardSaveLog") }
            }

            is SaveResult.Skipped -> AppToastHost.showToast(
                message = appContext.getString(
                    com.t8rin.imagetoolbox.core.resources.R.string.skipped_saving
                ),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                duration = ToastDuration.Short
            )

            is SaveResult.Error.Exception -> {
                result.throwable.makeLog("TextCardSave")
                AppToastHost.showFailureToast(throwable = result.throwable)
            }

            SaveResult.Error.MissingPermissions -> {
                AppToastHost.showToast(AppToastHost.PERMISSION)
            }
        }
    }

    fun cancelSaving() {
        savingJob = null
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): TextCardComponent
    }
}

/** AI 生成图片积分消耗/预检的来源标识 */
internal const val AI_IMAGE_POINTS_SOURCE = "text_card_ai"
