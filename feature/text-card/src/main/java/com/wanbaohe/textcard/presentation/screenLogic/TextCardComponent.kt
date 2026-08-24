package com.wanbaohe.textcard.presentation.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.FontCatalog
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.DownloadableFont
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextBlockId
import com.wanbaohe.textcard.domain.model.TextCardLayer
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 底部编辑面板 */
enum class EditorPanel {
    Background, Font, TextStyle, Layers
}

/** 字体行的下载状态 */
sealed interface FontDownloadState {
    data object NotDownloaded : FontDownloadState
    data class Downloading(val progress: Float) : FontDownloadState
    data object Downloaded : FontDownloadState
}

/**
 * 文字卡片页面组件(设计稿「文字卡片」)。
 *
 * [canvas] 为 null 时显示选择画布页,非空时进入编辑页(单 Component 双页面,
 * 同 markup-layers 的 hasImage 模式)。离散修改直接 set,不建历史栈(范围裁剪:
 * 不做撤销/重做)。
 */
class TextCardComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val exportRenderer: TextCardExportRenderer,
    private val fontCatalog: FontCatalog,
) : BaseComponent(dispatchersHolder, componentContext) {

    // ---------------- 画布与卡片状态 ----------------

    private val _canvas: MutableState<CanvasSpec?> = mutableStateOf(null)
    val canvas: CanvasSpec? by _canvas

    /** 选择画布页中选中的规格(未开始制作前) */
    private val _pendingCanvas: MutableState<CanvasSpec> = mutableStateOf(CanvasSpec.Xiaohongshu)
    val pendingCanvas: CanvasSpec by _pendingCanvas

    private val _background: MutableState<BackgroundSpec> = mutableStateOf(GradientPresets.default)
    val background: BackgroundSpec by _background

    private val _backgroundOpacity: MutableState<Float> = mutableStateOf(1f)
    val backgroundOpacity: Float by _backgroundOpacity

    private val _title: MutableState<TextBlock> = mutableStateOf(
        TextBlock(content = appContext.getString(R.string.textcard_default_title), isBold = true)
    )
    val title: TextBlock by _title

    private val _body: MutableState<TextBlock> = mutableStateOf(
        TextBlock(content = appContext.getString(R.string.textcard_default_body))
    )
    val body: TextBlock by _body

    private val _decoration: MutableState<DecorationSpec> = mutableStateOf(DecorationSpec())
    val decoration: DecorationSpec by _decoration

    /** 固定三层,列表顺序即 z 序(底层在前) */
    private val _layers: MutableState<List<TextCardLayer>> =
        mutableStateOf(TextCardLayer.defaultOrder())
    val layers: List<TextCardLayer> by _layers

    private val _activePanel: MutableState<EditorPanel?> = mutableStateOf(null)
    val activePanel: EditorPanel? by _activePanel

    /** 文字设置面板作用的文本块 */
    private val _selectedTextBlock: MutableState<TextBlockId> = mutableStateOf(TextBlockId.Title)
    val selectedTextBlock: TextBlockId by _selectedTextBlock

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    // ---------------- 画布选择 ----------------

    fun selectPendingCanvas(spec: CanvasSpec) {
        _pendingCanvas.value = spec
    }

    /** 「开始制作」:进入编辑页,装饰位置按画布规格初始化(默认右下角) */
    fun startEditing() {
        _canvas.value = _pendingCanvas.value
        _decoration.value = DecorationSpec.defaultFor(_pendingCanvas.value)
        registerChanges()
    }

    /** 编辑页返回:回到选择画布页 */
    fun backToSelection() {
        _canvas.value = null
        _activePanel.value = null
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

    // ---------------- 文字 ----------------

    fun selectTextBlock(id: TextBlockId) {
        _selectedTextBlock.value = id
    }

    fun updateTextBlock(id: TextBlockId, transform: (TextBlock) -> TextBlock) {
        when (id) {
            TextBlockId.Title -> _title.update(transform)
            TextBlockId.Body -> _body.update(transform)
        }
        registerChanges()
    }

    /** 画布内拖动文字块:dx/dy 为归一化增量(X 相对画布宽、Y 相对画布高) */
    fun updateTextBlockOffset(id: TextBlockId, dx: Float, dy: Float) {
        when (id) {
            TextBlockId.Title -> _title.update {
                it.copy(
                    offsetX = (it.offsetX + dx).coerceIn(-1f, 1f),
                    offsetY = (it.offsetY + dy).coerceIn(-1f, 1f)
                )
            }

            TextBlockId.Body -> _body.update {
                it.copy(
                    offsetX = (it.offsetX + dx).coerceIn(-1f, 1f),
                    offsetY = (it.offsetY + dy).coerceIn(-1f, 1f)
                )
            }
        }
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

    /** 字体选择作用于整张卡片(标题 + 正文) */
    fun applyFont(font: FontType?) {
        _title.update { it.copy(font = font) }
        _body.update { it.copy(font = font) }
        registerChanges()
    }

    // ---------------- 装饰 ----------------

    fun updateDecoration(spec: DecorationSpec) {
        if (spec == _decoration.value) return
        _decoration.value = spec
        registerChanges()
    }

    /** 画布内拖动装饰贴纸:dx/dy 为归一化增量(X 相对画布宽、Y 相对画布高) */
    fun updateDecorationOffset(dx: Float, dy: Float) {
        if (_decoration.value.emojiIndex == null) return
        _decoration.update {
            it.copy(
                offsetX = (it.offsetX + dx).coerceIn(-0.5f, 1f),
                offsetY = (it.offsetY + dy).coerceIn(-0.5f, 1f)
            )
        }
        registerChanges()
    }

    // ---------------- 图层(固定三层) ----------------

    fun toggleLayerVisible(index: Int) {
        val layer = _layers.value.getOrNull(index) ?: return
        if (layer.locked) return
        _layers.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copyState(visible = !item.visible, locked = item.locked) else item
            }
        }
        registerChanges()
    }

    fun toggleLayerLocked(index: Int) {
        val layer = _layers.value.getOrNull(index) ?: return
        _layers.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copyState(visible = item.visible, locked = !item.locked) else item
            }
        }
        registerChanges()
    }

    /** 拖拽排序提交:参数为完整新 z 序(底层在前) */
    fun reorderLayers(newOrder: List<TextCardLayer>) {
        if (newOrder == _layers.value || newOrder.size != _layers.value.size) return
        _layers.value = newOrder
        registerChanges()
    }

    // ---------------- 底部面板 ----------------

    fun togglePanel(panel: EditorPanel) {
        setActivePanel(if (_activePanel.value == panel) null else panel)
    }

    fun setActivePanel(panel: EditorPanel?) {
        _activePanel.value = panel
    }

    // ---------------- 字体下载 ----------------

    val downloadableFonts: List<DownloadableFont> get() = fontCatalog.fonts

    private val _fontDownloadStates: MutableState<Map<String, FontDownloadState>> =
        mutableStateOf(emptyMap())
    val fontDownloadStates: Map<String, FontDownloadState> by _fontDownloadStates

    private var fontDownloadJob: Job? by smartJob()

    /** 字体行三态:优先内存中的下载态,其次落盘清单 */
    fun fontState(font: DownloadableFont): FontDownloadState =
        _fontDownloadStates.value[font.id]
            ?: if (fontCatalog.downloadedFont(font) != null) {
                FontDownloadState.Downloaded
            } else FontDownloadState.NotDownloaded

    /** 已下载字体的 FontType.File(供选中时应用) */
    fun downloadedFontType(font: DownloadableFont): FontType.File? =
        fontCatalog.downloadedFont(font)

    fun downloadFont(font: DownloadableFont) {
        if (_fontDownloadStates.value[font.id] is FontDownloadState.Downloading) return
        fontDownloadJob = componentScope.launch {
            _fontDownloadStates.update { it + (font.id to FontDownloadState.Downloading(0f)) }
            val result = fontCatalog.download(font) { progress ->
                _fontDownloadStates.update {
                    it + (font.id to FontDownloadState.Downloading(progress))
                }
            }
            result.onSuccess { fontType ->
                _fontDownloadStates.update { it + (font.id to FontDownloadState.Downloaded) }
                applyFont(fontType)
            }.onFailure { failure ->
                failure.makeLog("TextCardFontDownload")
                _fontDownloadStates.update { it + (font.id to FontDownloadState.NotDownloaded) }
                AppToastHost.showFailureToast(R.string.textcard_font_download_failed)
            }
        }
    }

    // ---------------- 渲染与保存 ----------------

    fun renderState(): TextCardRenderState? {
        val spec = _canvas.value ?: return null
        return TextCardRenderState(
            canvas = spec,
            background = _background.value,
            backgroundOpacity = _backgroundOpacity.value,
            title = _title.value,
            body = _body.value,
            decoration = _decoration.value,
            layers = _layers.value
        )
    }

    /** 保存:离屏重绘 1080 宽 PNG,经 FileController 落盘(模板见 id-photo) */
    fun saveCard(onComplete: (SaveResult) -> Unit) {
        val state = renderState() ?: return
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

            _isSaving.value = false
            onComplete(result)
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
