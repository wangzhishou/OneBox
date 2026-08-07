package com.wanbaohe.camera.watermark.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.Metadata
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.image.set
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.ImageBaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.logger.makeLog
import com.wanbaohe.camera.watermark.data.ExifMetadataParser
import com.wanbaohe.camera.watermark.data.WatermarkRenderer
import com.wanbaohe.camera.watermark.data.WatermarkTemplateRepository
import com.wanbaohe.camera.watermark.domain.ExportConfig
import com.wanbaohe.camera.watermark.domain.WatermarkMetadata
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 相机水印 Component
 * 基于 Decompose 架构的导航组件
 * 继承 ImageBaseComponent 复用通用的图片处理逻辑
 */
class CameraWatermarkComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    private val exifParser: ExifMetadataParser,
    private val renderer: WatermarkRenderer,
    private val templateRepository: WatermarkTemplateRepository,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : ImageBaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            // 初始化并加载模板（包括预置模板）
            initAndLoadTemplates()
            // 如果有初始 Uri，则加载
            initialUris?.takeIf { it.isNotEmpty() }?.let { setSelectedUris(it) }
        }

        // 监听组件生命周期，在销毁时记录使用时间
        componentContext.lifecycle.doOnDestroy {
            // 记录当前选中模板的使用时间（下次进入时排在前面）
            if (currentStyle.id > 0) {
                // 使用独立的协程作用域，确保在组件销毁后仍能执行
                // componentScope 在 doOnDestroy 时可能已被取消
                kotlinx.coroutines.GlobalScope.launch(dispatchersHolder.ioDispatcher) {
                    templateRepository.recordTemplateUsage(currentStyle.id)
                }
            }
        }
    }

    // ========== 水印特有状态 ==========

    /** 当前图片的元数据 */
    private val _metadata: MutableState<WatermarkMetadata> = mutableStateOf(WatermarkMetadata.EMPTY)
    val metadata: WatermarkMetadata by _metadata

    /** 当前选择的水印样式 */
    private val _currentStyle: MutableState<WatermarkStyle> = mutableStateOf(WatermarkStyle.DEFAULT)
    val currentStyle: WatermarkStyle by _currentStyle

    /** 是否正在导出 */
    private val _isExporting: MutableState<Boolean> = mutableStateOf(false)
    val isExporting: Boolean by _isExporting

    /** 所有模板列表（从数据库获取，包括预置模板） */
    private val _allTemplates = MutableStateFlow<List<WatermarkStyle>>(emptyList())
    val allTemplates: StateFlow<List<WatermarkStyle>> = _allTemplates.asStateFlow()

    /** 导出配置 */
    private val _exportConfig: MutableState<ExportConfig> = mutableStateOf(ExportConfig())
    val exportConfig: ExportConfig by _exportConfig

    /** 原始图片的 Metadata（用于保存时写回 EXIF） */
    private val _originalMetadata: MutableState<Metadata?> = mutableStateOf(null)

    /** 作者签名（保存到 EXIF 的 Artist 字段） */
    private val _authorSignature: MutableState<String> = mutableStateOf("万宝盒")
    val authorSignature: String by _authorSignature

    /** 是否保留原始 EXIF */
    private val _keepOriginalExif: MutableState<Boolean> = mutableStateOf(true)
    val keepOriginalExif: Boolean by _keepOriginalExif

    /** 是否有未保存的更改 */
    override val haveChanges: Boolean
        get() = previewBitmap != null

    // ========== ImageBaseComponent 抽象方法实现 ==========

    /**
     * 当选中新的 URI 时调用
     */
    override fun onUriSelected(uri: Uri) {
        loadMetadata(uri)
    }

    /**
     * 当所有 URI 被清空时调用
     */
    override fun onAllUrisCleared() {
        super.onAllUrisCleared()
        _metadata.update { WatermarkMetadata.EMPTY }
    }

    // ========== 配置方法 ==========

    /**
     * 设置作者签名
     */
    fun setAuthorSignature(signature: String) {
        _authorSignature.update { signature }
    }

    /**
     * 设置是否保留原始 EXIF
     */
    fun setKeepOriginalExif(keep: Boolean) {
        _keepOriginalExif.update { keep }
    }

    // ========== 模板管理 ==========

    /**
     * 初始化并加载模板
     */
    private fun initAndLoadTemplates() {
        componentScope.launch {
            // 确保预置模板已初始化
            templateRepository.initPresetsIfNeeded()
            // 监听模板列表变化
            templateRepository.getRecentTemplates(50).collect { templates ->
                _allTemplates.value = templates
                // 如果当前样式不在列表中，选择第一个
                if (templates.isNotEmpty() && templates.none { it.id == currentStyle.id }) {
                    _currentStyle.update { templates.first() }
                }
            }
        }
    }

    // ========== 图片管理扩展 ==========

    /**
     * 添加图片（批量选择时追加）
     */
    override fun addUris(uris: List<Uri>) {
        val isFirstAdd = _selectedUris.value.isEmpty()
        super.addUris(uris)
        if (isFirstAdd && uris.isNotEmpty()) {
            loadMetadata(uris.first())
        }
    }

    /**
     * 移除图片
     */
    override fun removeUri(uri: Uri) {
        super.removeUri(uri)
        if (_selectedUris.value.isEmpty()) {
            _metadata.update { WatermarkMetadata.EMPTY }
        }
    }

    /**
     * 清空所有图片
     */
    fun clearAll() {
        clearAllUris()
        _metadata.update { WatermarkMetadata.EMPTY }
    }

    // ========== 元数据 ==========

    /**
     * 加载图片的 EXIF 元数据
     */
    private fun loadMetadata(uri: Uri) {
        debouncedImageCalculation(
            delay = 100L,
            action = {
                // 解析自定义的 WatermarkMetadata（用于水印显示）
                _metadata.update { exifParser.parseFromUri(uri) }
                // 获取原始 Metadata（用于保存时写回 EXIF）
                val imageData = imageGetter.getImage(uri.toString(), originalSize = false)
                _originalMetadata.update { imageData?.metadata }
            },
            onFinish = {
                updatePreview()
            }
        )
    }

    // ========== 样式 ==========

    /**
     * 设置水印样式
     */
    fun setStyle(style: WatermarkStyle) {
        _currentStyle.update { style }
        updatePreview()
        registerChanges()
    }

    /**
     * 选择预设样式
     */
    fun selectPreset(preset: WatermarkStyle) {
        _currentStyle.update { preset }
        updatePreview()
        registerChanges()
    }

    /**
     * 从历史记录选择样式（不立即更新使用时间，避免列表闪烁）
     */
    fun selectFromHistory(style: WatermarkStyle) {
        _currentStyle.update { style }
        // 注意：不在这里更新使用时间，改为在保存成功后更新
        // 这样可以避免列表因重新排序而闪烁
        updatePreview()
        registerChanges()
    }

    /**
     * 保存或更新模板
     */
    fun saveOrUpdateTemplate(style: WatermarkStyle) {
        componentScope.launch {
            val savedStyle = templateRepository.saveOrUpdate(style)
            // 更新当前样式
            _currentStyle.update { savedStyle }
            // 更新预览
            updatePreview()
        }
    }

    /**
     * 删除模板
     */
    fun deleteTemplate(id: Long) {
        componentScope.launch {
            templateRepository.deleteTemplate(id)
            // 如果删除的是当前选中的模板，选择第一个
            if (currentStyle.id == id) {
                _allTemplates.value.firstOrNull()?.let {
                    _currentStyle.update { it }
                }
            }
        }
    }

    /**
     * 批量删除模板
     */
    fun batchDeleteTemplates(ids: List<Long>) {
        componentScope.launch {
            templateRepository.deleteTemplates(ids)
            // 如果删除的包含当前选中的模板，选择第一个
            if (ids.contains(currentStyle.id)) {
                _allTemplates.value.firstOrNull()?.let {
                    _currentStyle.update { it }
                }
            }
        }
    }

    /**
     * 恢复预置模板为默认值（不删除用户新增的模板）
     */
    fun resetPresetsToDefaults() {
        componentScope.launch {
            templateRepository.resetPresetsOnly()
            // 选择第一个模板
            _allTemplates.value.firstOrNull()?.let {
                _currentStyle.update { it }
            }
        }
    }

    // ========== 预览 ==========

    /**
     * 更新预览图
     */
    private fun updatePreview() {
        val uri = _selectedUris.value.getOrNull(currentIndex) ?: return
        componentScope.launch(defaultDispatcher) {
            updatePreviewBitmap(renderer.render(uri, metadata, currentStyle))
        }
    }

    // ========== 导出配置 ==========

    /**
     * 更新导出配置
     */
    fun updateExportConfig(config: ExportConfig) {
        _exportConfig.update { config }
    }

    /**
     * 设置导出格式
     */
    fun setExportFormat(format: ImageFormat) {
        _exportConfig.update { exportConfig.copy(format = format) }
    }

    /**
     * 设置导出质量
     */
    fun setExportQuality(quality: Int) {
        _exportConfig.update { exportConfig.copy(quality = quality.coerceIn(1, 100)) }
    }

    /**
     * 准备带作者签名的 Metadata
     */
    private fun prepareMetadataWithSignature(): Metadata? {
        return _originalMetadata.value?.let { metadata ->
            // 设置作者签名
            if (authorSignature.isNotEmpty()) {
                metadata[MetadataTag.Artist] = authorSignature
                metadata[MetadataTag.Copyright] = "© $authorSignature"
                metadata[MetadataTag.Software] = "万宝盒"
            }
            metadata
        }
    }

    // ========== 保存实现 ==========

    /**
     * 导出当前图片
     */
    override fun saveBitmap(
        oneTimeSaveLocationUri: String?,
        onComplete: (SaveResult) -> Unit,
    ) {
        val bitmap = previewBitmap ?: return
        val uri = _selectedUris.value.getOrNull(currentIndex) ?: return

        savingJob = componentScope.launch {
            startSaving()

            val imageInfo = ImageInfo(
                imageFormat = exportConfig.format,
                width = bitmap.width,
                height = bitmap.height,
                quality = exportConfig.quality.toQuality()
            )

            // 准备带作者签名的 Metadata
            val metadataWithSignature = if (keepOriginalExif) {
                prepareMetadataWithSignature()
            } else {
                null
            }

            val result = fileController.save(
                saveTarget = ImageSaveTarget(
                    imageInfo = imageInfo,
                    originalUri = uri.toString(),
                    sequenceNumber = null,
                    metadata = metadataWithSignature,
                    data = imageCompressor.compressAndTransform(
                        image = bitmap,
                        imageInfo = imageInfo
                    )
                ),
                keepOriginalMetadata = keepOriginalExif,
                oneTimeSaveLocationUri = oneTimeSaveLocationUri
            ).onSuccess(::registerSave)

            finishSaving()
            onComplete(result)

            // 注意：不在这里记录使用时间，改为在组件销毁时统一记录
            // 避免保存过程中列表因重新排序而闪烁
        }
    }

    /**
     * 批量导出所有图片
     */
    override fun saveAllBitmaps(
        onProgress: (Int, Int) -> Unit,
        onComplete: (List<SaveResult>) -> Unit
    ) {
        savingJob = componentScope.launch {
            val results = mutableListOf<SaveResult>()
            val total = _selectedUris.value.size

            // 使用批量保存开始方法
            startBatchSaving(total)

            _selectedUris.value.forEachIndexed { index, uri ->
                // 使用新的批量进度更新方法
                updateBatchProgress(index, total)
                onProgress(index + 1, total)

                // 获取当前图片的 metadata
                val imageData = imageGetter.getImage(uri.toString(), originalSize = false)
                val currentMetadata = if (keepOriginalExif) {
                    imageData?.metadata?.let { meta ->
                        if (authorSignature.isNotEmpty()) {
                            meta[MetadataTag.Artist] = authorSignature
                            meta[MetadataTag.Copyright] = "© $authorSignature"
                            meta[MetadataTag.Software] = "万宝盒"
                        }
                        meta
                    }
                } else null

                val bitmap = renderer.render(uri, metadata, currentStyle)
                if (bitmap != null) {
                    val imageInfo = ImageInfo(
                        imageFormat = exportConfig.format,
                        width = bitmap.width,
                        height = bitmap.height,
                        quality = exportConfig.quality.toQuality()
                    )

                    val result = fileController.save(
                        saveTarget = ImageSaveTarget(
                            imageInfo = imageInfo,
                            originalUri = uri.toString(),
                            sequenceNumber = index,
                            metadata = currentMetadata,
                            data = imageCompressor.compressAndTransform(
                                image = bitmap,
                                imageInfo = imageInfo
                            )
                        ),
                        keepOriginalMetadata = keepOriginalExif,
                        oneTimeSaveLocationUri = null
                    )
                    results.add(result)
                    bitmap.recycle()
                }
            }

            finishSaving()
            onComplete(results)

            // 注意：不在这里记录使用时间，改为在组件销毁时统一记录
            registerSave()
        }
    }

    /**
     * Int 转换为 Quality
     */
    private fun Int.toQuality(): com.t8rin.imagetoolbox.core.domain.image.model.Quality {
        return com.t8rin.imagetoolbox.core.domain.image.model.Quality.Base(this)
    }

    /**
     * 获取用于文件名选择的格式（重写以使用 exportConfig）
     */
    override fun getFormatForFilenameSelection(): ImageFormat = exportConfig.format

    override fun resetState() {
        super.resetState()
        _metadata.update { WatermarkMetadata.EMPTY }
        _currentStyle.update { WatermarkStyle.CLASSIC_WHITE }
        _isExporting.update { false }
        _exportConfig.update { ExportConfig() }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
        ): CameraWatermarkComponent
    }
}

