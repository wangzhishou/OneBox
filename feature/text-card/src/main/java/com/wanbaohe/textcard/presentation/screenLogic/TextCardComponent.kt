package com.wanbaohe.textcard.presentation.screenLogic

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoader
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
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
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.CustomCanvasStore
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.TextCardPaperRepository
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.CardDrawStroke
import com.wanbaohe.textcard.domain.model.CardShapeKind
import com.wanbaohe.textcard.domain.model.CardStrokePoint
import com.wanbaohe.textcard.domain.model.DEFAULT_DRAW_COLOR
import com.wanbaohe.textcard.domain.model.DEFAULT_DRAW_WIDTH_RATIO
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.DrawElementSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.ImageElementSpec
import com.wanbaohe.textcard.domain.model.ImageElementStatus
import com.wanbaohe.textcard.domain.model.RemotePaper
import com.wanbaohe.textcard.domain.model.ShapeElementSpec
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.sqrt

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

    /** 形状元素列表(对照图片创作形状工具,每个一个图层) */
    private val _shapeElements: MutableState<List<ShapeElementSpec>> = mutableStateOf(emptyList())
    val shapeElements: List<ShapeElementSpec> by _shapeElements

    /** 画笔元素列表(每个绘制会话一个图层) */
    private val _drawElements: MutableState<List<DrawElementSpec>> = mutableStateOf(emptyList())
    val drawElements: List<DrawElementSpec> by _drawElements

    /** AI 生图进行中(生成图片图层弹窗的 loading) */
    private val _isGeneratingImage: MutableState<Boolean> = mutableStateOf(false)
    val isGeneratingImage: Boolean by _isGeneratingImage

    /** AI 生成贴纸进行中(共享贴纸弹层 AI tab 的 loading) */
    private val _isGeneratingSticker: MutableState<Boolean> = mutableStateOf(false)
    val isGeneratingSticker: Boolean by _isGeneratingSticker

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

    /**
     * 页面被压在栈内(如跳转图片创作)时组合销毁,1.5s 后 BaseComponent 回调这里。
     * 绝不能回退导航——navigateBack 弹的是栈顶(被跳转的页面会被立刻误弹回去)。
     * 组件实例仍在栈中,状态保留,返回时原样继续编辑。
     */
    override fun resetState() = Unit

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

    /** 画布内拖动自定义背景图:仅背景为相册图时生效;钳制在过扫余量内,不出边 */
    fun updateBackgroundImageOffset(dx: Float, dy: Float) {
        val background = _background.value as? BackgroundSpec.Image ?: return
        val maxOffset = CardLayout.BACKGROUND_IMAGE_MAX_OFFSET
        _background.value = background.copy(
            offsetX = (background.offsetX + dx).coerceIn(-maxOffset, maxOffset),
            offsetY = (background.offsetY + dy).coerceIn(-maxOffset, maxOffset)
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

            _shapeElements.value.any { it.id == id } -> {
                _shapeElements.update { list ->
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

            _drawElements.value.any { it.id == id } -> {
                _drawElements.update { list ->
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
                widthManuallySet = true,
                heightRatio = heightRatio.coerceIn(0f, 1f),
                offsetX = offsetX.coerceIn(-1f, 1f),
                offsetY = offsetY.coerceIn(-1f, 1f)
            )
        }
    }

    /** 删除选中元素(文字块至少保留一块,装饰/图片/形状/画笔不限),同步移除图层 */
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

            _shapeElements.value.any { it.id == id } -> {
                _shapeElements.update { list -> list.filterNot { it.id == id } }
                _elementLayers.update { list -> list.filterNot { it.elementId == id } }
                if (_selectedElementId.value == id) _selectedElementId.value = null
                registerChanges()
            }

            _drawElements.value.any { it.id == id } -> {
                _drawElements.update { list -> list.filterNot { it.id == id } }
                _elementLayers.update { list -> list.filterNot { it.elementId == id } }
                if (_selectedElementId.value == id) _selectedElementId.value = null
                registerChanges()
            }
        }
    }

    // ---------------- 形状(对照图片创作形状工具) ----------------

    /** 添加形状元素:按种类的默认参数落画布中心,置顶并选中 */
    fun addShapeElement(kind: CardShapeKind, colorArgb: Long, filled: Boolean) {
        val element = ShapeElementSpec.defaultFor(kind).copy(
            colorArgb = colorArgb,
            filled = if (kind == CardShapeKind.Line) false else filled
        )
        _shapeElements.update { it + element }
        _elementLayers.update { it + ElementLayer(element.id, ElementLayer.Kind.Shape) }
        _selectedElementId.value = element.id
        registerChanges()
    }

    // ---------------- 画笔 ----------------

    /** 画布绘制模式:true 时画布叠加采集层,底部换取消/完成操作条 */
    private val _isDrawing: MutableState<Boolean> = mutableStateOf(false)
    val isDrawing: Boolean by _isDrawing

    /** 本次绘制会话已完成的笔画(画布实时预览用) */
    private val _drawStrokes: MutableState<List<CardDrawStroke>> = mutableStateOf(emptyList())
    val drawStrokes: List<CardDrawStroke> by _drawStrokes

    /** 当前画笔颜色/粗细(绘制态可调,默认与图片创作画笔一致) */
    private val _drawBrushColor: MutableState<Long> = mutableStateOf(DEFAULT_DRAW_COLOR)
    val drawBrushColor: Long by _drawBrushColor

    private val _drawBrushWidthRatio: MutableState<Float> =
        mutableStateOf(DEFAULT_DRAW_WIDTH_RATIO)
    val drawBrushWidthRatio: Float by _drawBrushWidthRatio

    fun updateDrawBrushColor(colorArgb: Long) {
        _drawBrushColor.value = colorArgb
    }

    fun updateDrawBrushWidth(widthRatio: Float) {
        _drawBrushWidthRatio.value = widthRatio.coerceIn(0.004f, 0.04f)
    }

    /** 侧栏「画笔」:进入绘制态(清空上次会话残留),取消元素选中避免手势冲突 */
    fun startDrawMode() {
        _isDrawing.value = true
        _drawStrokes.value = emptyList()
        _selectedElementId.value = null
    }

    /** 收笔一笔:追加到本次会话(用当前画笔颜色/粗细) */
    fun addSessionStroke(points: List<CardStrokePoint>) {
        if (points.isEmpty()) return
        _drawStrokes.update {
            it + CardDrawStroke(
                points = points,
                colorArgb = _drawBrushColor.value,
                widthRatio = _drawBrushWidthRatio.value
            )
        }
    }

    /** 「完成」:会话笔画落成画笔图层元素并选中,退出绘制态 */
    fun finishDrawMode() {
        val strokes = _drawStrokes.value
        if (strokes.isNotEmpty()) {
            val element = DrawElementSpec(strokes = strokes)
            _drawElements.update { it + element }
            _elementLayers.update { it + ElementLayer(element.id, ElementLayer.Kind.Draw) }
            _selectedElementId.value = element.id
            registerChanges()
        }
        _isDrawing.value = false
        _drawStrokes.value = emptyList()
    }

    /** 「取消」:丢弃本次会话,退出绘制态 */
    fun cancelDrawMode() {
        _isDrawing.value = false
        _drawStrokes.value = emptyList()
    }

    // ---------------- 装饰 ----------------

    /** 共享贴纸面板的确认动作:emoji(下标)/素材贴纸(assets 路径)/AI 生成(本地文件),默认落左上角,置顶并选中 */
    fun addStickerDecoration(source: StickerSource) {
        val canvas = _canvas.value ?: return
        val decoration = when (source) {
            is StickerSource.Emoji ->
                DecorationSpec.defaultPositionFor(canvas, source.emojiIndex)

            is StickerSource.Asset ->
                DecorationSpec.defaultPositionFor(canvas, 0).copy(
                    emojiIndex = null,
                    assetPath = source.path
                )

            is StickerSource.Generated ->
                DecorationSpec.defaultPositionFor(canvas, 0).copy(
                    emojiIndex = null,
                    imagePath = source.path
                )
        }
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

    /** 当前选中的 Ready 图片元素(非空时,生成弹层为「AI 编辑图片」图生图模式) */
    fun selectedReadyImageElement(): ImageElementSpec? = _imageElements.value
        .firstOrNull { it.id == _selectedElementId.value && it.status == ImageElementStatus.Ready }

    /**
     * 生成图片图层:先落 Loading 占位再异步生成,[onStarted] 在占位落地后回调
     * (弹窗据此立即关闭,不阻塞用户其它操作);生成中重复调用 toast 提示并忽略。
     *
     * 两种模式:
     * - 新建(无选中图片):占位铺满画布、图层垫底(背景之上文字之下,不挡文字),
     *   按画布尺寸指定 outputSize;成功换图,失败标 Error(可删)。
     * - 编辑(选中 Ready 图片):原图保留转 Loading(预览叠加转圈),图生图;
     *   成功换图并把旧图压入 historyUris,失败恢复 Ready 原图不动。
     * 仅真实生成成功扣积分,复用本地缓存(fromCache)不重复扣。
     */
    fun generateImageLayer(prompt: String, onStarted: () -> Unit = {}) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            AppToastHost.showToast(
                message = appContext.getString(R.string.textcard_generate_image_empty),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                duration = ToastDuration.Short
            )
            return
        }
        if (_isGeneratingImage.value) {
            AppToastHost.showToast(
                message = appContext.getString(R.string.textcard_generate_image_running),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                duration = ToastDuration.Short
            )
            return
        }
        val canvas = _canvas.value ?: return
        val editTarget = selectedReadyImageElement()
        val outputSize = canvas.qwenOutputSize()

        // 占位落地:新建 = 铺满画布的 Loading 图层垫底;编辑 = 原图转 Loading
        val placeholder = if (editTarget == null) {
            ImageElementSpec.fullCanvas(uri = "", status = ImageElementStatus.Loading)
        } else null
        if (placeholder != null) {
            _imageElements.update { it + placeholder }
            _elementLayers.update {
                listOf(ElementLayer(placeholder.id, ElementLayer.Kind.Image)) + it
            }
            _selectedElementId.value = placeholder.id
            registerChanges()
        } else if (editTarget != null) {
            updateImageElement(editTarget.id) { it.copy(status = ImageElementStatus.Loading) }
        }
        onStarted()
        val pendingId = placeholder?.id ?: editTarget?.id ?: return

        generateImageJob = componentScope.launch {
            _isGeneratingImage.value = true
            // 编辑模式:本地图片读成 base64 data URI 作为输入图(服务商不接受本地路径)
            val inputImages = if (editTarget != null) {
                runCatching {
                    listOf(withContext(defaultDispatcher) { encodeImageAsDataUri(editTarget.uri) })
                }.getOrElse { error ->
                    updateImageElement(pendingId) { it.copy(status = ImageElementStatus.Ready) }
                    AppToastHost.showFailureToast(throwable = error)
                    _isGeneratingImage.value = false
                    return@launch
                }
            } else emptyList()

            imageGenerationLoader.load(
                ImageGenerationRequest(
                    prompt = trimmed,
                    inputImages = inputImages,
                    outputSize = outputSize
                )
            ).onSuccess { image ->
                updateImageElement(pendingId) {
                    it.copy(
                        uri = image.file.absolutePath,
                        status = ImageElementStatus.Ready,
                        // 编辑模式:旧图压入历史,可在弹层里回退
                        historyUris = if (editTarget != null) it.historyUris + it.uri
                        else it.historyUris
                    )
                }
                if (!image.fromCache) {
                    BaseUtils.consumePoints(
                        degree = aiImageProcessPointsCost(),
                        desc = appContext.getString(R.string.textcard_add_image_layer),
                        source = AI_IMAGE_POINTS_SOURCE,
                        showToast = true
                    )
                }
            }.onFailure { error ->
                updateImageElement(pendingId) {
                    // 新建失败标 Error;编辑失败恢复原图
                    it.copy(
                        status = if (editTarget == null) {
                            ImageElementStatus.Error
                        } else ImageElementStatus.Ready
                    )
                }
                AppToastHost.showFailureToast(throwable = error)
            }
            _isGeneratingImage.value = false
        }
    }

    /** AI 编辑历史回退:把历史版本换回当前图,当前图进历史尾部 */
    fun revertImageElement(id: String, uri: String) {
        updateImageElement(id) { element ->
            if (uri == element.uri || uri !in element.historyUris) {
                return@updateImageElement element
            }
            element.copy(
                uri = uri,
                historyUris = element.historyUris - uri + element.uri
            )
        }
    }

    // ---------------- AI 生成贴纸(共享贴纸弹层 AI tab) ----------------

    private var generateStickerJob: Job? by smartJob {
        _isGeneratingSticker.update { false }
    }

    /**
     * AI 生成贴纸:文生图(提示词追加贴纸风格描述,正方形输出),成功落装饰元素
     *(与素材贴纸同等待遇:选中/拖动/缩放/图层面板/导出),[onSuccess] 供弹层关闭。
     * 登录与积分预检由调用方(UI 层 ActionUtils.ensureLoginAndCheckPoints)完成;
     * 仅非缓存结果(!fromCache)扣积分,失败 toast 不扣。
     */
    fun generateSticker(prompt: String, onSuccess: () -> Unit = {}) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _isGeneratingSticker.value) return
        generateStickerJob = componentScope.launch {
            _isGeneratingSticker.value = true
            imageGenerationLoader.load(
                ImageGenerationRequest(
                    prompt = "$trimmed, $STICKER_PROMPT_STYLE_SUFFIX",
                    outputSize = STICKER_OUTPUT_SIZE
                )
            ).onSuccess { image ->
                addStickerDecoration(StickerSource.Generated(image.file.absolutePath))
                if (!image.fromCache) {
                    BaseUtils.consumePoints(
                        degree = aiImageProcessPointsCost(),
                        desc = appContext.getString(
                            com.t8rin.imagetoolbox.core.resources.R.string.sticker_category_ai
                        ),
                        source = AI_IMAGE_POINTS_SOURCE,
                        showToast = true
                    )
                }
                onSuccess()
            }.onFailure { error ->
                AppToastHost.showFailureToast(throwable = error)
            }
            _isGeneratingSticker.value = false
        }
    }

    /** 本地图片文件 → base64 data URI(图生图输入) */
    private fun encodeImageAsDataUri(path: String): String {
        val file = File(path)
        val mime = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "image/png"
        }
        return "data:$mime;base64," + Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }

    /** 生成输出尺寸(服务商限制:像素总数 512²..2048²、宽高比 ≤ 8:1),按画布等比收窄 */
    private fun CanvasSpec.qwenOutputSize(): String {
        val maxPixels = 2048.0 * 2048.0
        val pixels = width.toDouble() * height
        val scale = if (pixels > maxPixels) sqrt(maxPixels / pixels) else 1.0
        return "${(width * scale).roundToInt()}*${(height * scale).roundToInt()}"
    }

    fun cancelGeneratingImage() {
        generateImageJob = null
    }

    /** 重置元素变换到初始位置(文字回基准位、装饰回左上角、图片回铺满/居中;文字框尺寸不动) */
    fun resetElementTransform(id: String) {
        val canvas = _canvas.value
        when {
            _textBlocks.value.any { it.id == id } -> updateTextBlock(id) {
                it.copy(offsetX = 0f, offsetY = 0f, scale = 1f, rotation = 0f)
            }

            _decorations.value.any { it.id == id } -> {
                val initial = _decorations.value.first { it.id == id }.let { decoration ->
                    canvas?.let {
                        DecorationSpec.defaultPositionFor(it, decoration.emojiIndex ?: 0)
                    }
                }
                _decorations.update { list ->
                    list.map {
                        if (it.id == id) {
                            it.copy(
                                offsetX = initial?.offsetX ?: it.offsetX,
                                offsetY = initial?.offsetY ?: it.offsetY,
                                scale = 1f,
                                rotation = 0f
                            )
                        } else it
                    }
                }
                registerChanges()
            }

            _imageElements.value.any { it.id == id } -> updateImageElement(id) { element ->
                val initial = canvas?.let {
                    ImageElementSpec.defaultPositionFor(it, element.uri)
                }
                element.copy(
                    offsetX = if (element.fullCanvas) 0f else initial?.offsetX ?: element.offsetX,
                    offsetY = if (element.fullCanvas) 0f else initial?.offsetY ?: element.offsetY,
                    scale = 1f,
                    rotation = 0f
                )
            }
        }
    }

    /** 按 id 更新图片元素(占位图层状态机:Loading → Ready/Error) */
    private fun updateImageElement(id: String, transform: (ImageElementSpec) -> ImageElementSpec) {
        if (_imageElements.value.none { it.id == id }) return
        _imageElements.update { list -> list.map { if (it.id == id) transform(it) else it } }
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

    /** 远程纸张刷新中(刷新按钮 loading 态) */
    private val _remotePapersRefreshing = mutableStateOf(false)
    val remotePapersRefreshing: Boolean by _remotePapersRefreshing

    fun loadRemotePapers() {
        if (papersJob != null || _remotePapers.value.isNotEmpty()) return
        papersJob = componentScope.launch {
            _remotePapers.value = paperRepository.loadLocalPapers()
        }
    }

    /** 手动刷新远程纸张(后台新发布纸张后客户端列表不更新时重拉);失败/为空保留现有列表 */
    fun refreshRemotePapers() {
        if (papersJob != null) return
        papersJob = componentScope.launch {
            _remotePapersRefreshing.value = true
            val papers = paperRepository.loadLocalPapers()
            if (papers.isNotEmpty()) _remotePapers.value = papers
            _remotePapersRefreshing.value = false
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
            shapeElements = _shapeElements.value,
            drawElements = _drawElements.value,
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

/** AI 生成贴纸:提示词追加的贴纸风格描述(文生图模型对英文风格词更稳定) */
private const val STICKER_PROMPT_STYLE_SUFFIX = "sticker style, clean simple solid background"

/** AI 生成贴纸输出尺寸(正方形,服务商像素限制内的固定值) */
private const val STICKER_OUTPUT_SIZE = "1024*1024"
