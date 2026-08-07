package com.wanbaohe.idphoto.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.smarttoolfactory.cropper.settings.CropProperties
import com.smarttoolfactory.cropper.settings.CropType
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.ImageBaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.logger.makeLog
import com.wanbaohe.idphoto.data.IdPhotoSizeRepository
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.wanbaohe.idphoto.domain.IdPhotoExportConfig
import com.wanbaohe.idphoto.domain.IdPhotoSize
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 证件照制作 Component
 * 基于 Decompose 架构的导航组件
 */
class IdPhotoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    private val sizeRepository: IdPhotoSizeRepository,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : ImageBaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            // 初始化并加载尺寸模板
            initAndLoadSizes()
            // 如果有初始 Uri，则加载
            initialUris?.takeIf { it.isNotEmpty() }?.let { setSelectedUris(it) }
        }

        // 监听组件生命周期，在销毁时记录使用时间
        componentContext.lifecycle.doOnDestroy {
            // 记录当前选中尺寸的使用时间（下次进入时排在前面）
            if (currentSize.id > 0) {
                // 使用独立的协程作用域，确保在组件销毁后仍能执行
                // componentScope 在 doOnDestroy 时可能已被取消
                kotlinx.coroutines.GlobalScope.launch(dispatchersHolder.ioDispatcher) {
                    sizeRepository.recordSizeUsage(currentSize.id)
                }
            }
        }
    }

    // ========== 证件照特有状态 ==========

    /** 当前选择的尺寸 */
    private val _currentSize: MutableState<IdPhotoSize> = mutableStateOf(IdPhotoSize.DEFAULT)
    val currentSize: IdPhotoSize by _currentSize

    /** 当前选择的背景色 */
    private val _currentBackground: MutableState<IdPhotoBackground> = mutableStateOf(IdPhotoBackground.DEFAULT)
    val currentBackground: IdPhotoBackground by _currentBackground

    /** 所有尺寸列表（从数据库获取） */
    private val _allSizes = MutableStateFlow<List<IdPhotoSize>>(emptyList())
    val allSizes: StateFlow<List<IdPhotoSize>> = _allSizes.asStateFlow()

    /** 导出配置 */
    private val _exportConfig: MutableState<IdPhotoExportConfig> = mutableStateOf(IdPhotoExportConfig())
    val exportConfig: IdPhotoExportConfig by _exportConfig

    /** 默认裁剪轮廓 */
    private val defaultCropOutline = CropOutlineProperty(
        OutlineType.Rect,
        RectCropShape(id = 0, title = OutlineType.Rect.name)
    )

    /** 裁剪属性 */
    private val _cropProperties: MutableState<CropProperties> = mutableStateOf(
        CropDefaults.properties(
            cropType = CropType.Static,
            cropOutlineProperty = defaultCropOutline,
            aspectRatio = AspectRatio(IdPhotoSize.DEFAULT.aspectRatio),
            fixedAspectRatio = true,
            handleSize = 0f,           // 禁用边角拖动
            middleHandleSize = 0f,     // 禁用中间点拖动
            pannable = true,           // 允许平移
            zoomable = true,           // 允许缩放图片（方便低分辨率照片调整）
            maxZoom = 5f,              // 最大缩放倍数
            fling = true
        )
    )
    val cropProperties: CropProperties by _cropProperties

    /** 裁剪后的 Bitmap */
    private val _croppedBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val croppedBitmap: Bitmap? by _croppedBitmap

    /** 原始图片尺寸（用于分辨率检测） */
    private val _originalImageSize: MutableState<Pair<Int, Int>?> = mutableStateOf(null)
    val originalImageSize: Pair<Int, Int>? by _originalImageSize

    /**
     * 检测图片分辨率是否足够
     * @return null 表示未加载图片，true 表示分辨率足够，false 表示分辨率不足
     */
    val isResolutionSufficient: Boolean?
        get() {
            val imageSize = _originalImageSize.value ?: return null
            val targetSize = currentSize
            // 检查裁剪后是否能达到目标像素（考虑裁剪比例）
            val imageAspect = imageSize.first.toFloat() / imageSize.second
            val targetAspect = targetSize.aspectRatio

            // 计算裁剪后的有效像素
            val effectivePixels = if (imageAspect > targetAspect) {
                // 图片更宽，以高度为基准
                val cropWidth = (imageSize.second * targetAspect).toInt()
                cropWidth * imageSize.second
            } else {
                // 图片更高，以宽度为基准
                val cropHeight = (imageSize.first / targetAspect).toInt()
                imageSize.first * cropHeight
            }

            val targetPixels = targetSize.widthPx * targetSize.heightPx
            return effectivePixels >= targetPixels
        }

    /** 是否有未保存的更改 */
    override val haveChanges: Boolean
        get() = _croppedBitmap.value != null

    // ========== ImageBaseComponent 抽象方法实现 ==========

    override fun onUriSelected(uri: Uri) {
        loadImage(uri)
    }

    override fun onAllUrisCleared() {
        super.onAllUrisCleared()
        _croppedBitmap.update { null }
    }

    // ========== 尺寸管理 ==========

    /**
     * 初始化并加载尺寸
     */
    private fun initAndLoadSizes() {
        componentScope.launch {
            // 确保预置尺寸已初始化
            sizeRepository.initPresetsIfNeeded()
            // 监听尺寸列表变化
            sizeRepository.getRecentSizes(30).collect { sizes ->
                _allSizes.value = sizes
                // 如果当前尺寸不在列表中，选择第一个
                if (sizes.isNotEmpty() && sizes.none { it.id == currentSize.id }) {
                    setSize(sizes.first())
                }
            }
        }
    }

    /**
     * 设置尺寸
     */
    fun setSize(size: IdPhotoSize) {
        _currentSize.update { size }
        // 更新裁剪属性的宽高比
        _cropProperties.update {
            CropDefaults.properties(
                cropType = CropType.Static,
                cropOutlineProperty = defaultCropOutline,
                aspectRatio = AspectRatio(size.aspectRatio),
                fixedAspectRatio = true,
                handleSize = 0f,           // 禁用边角拖动
                middleHandleSize = 0f,     // 禁用中间点拖动
                pannable = true,           // 允许平移
                zoomable = true,           // 允许缩放图片（方便低分辨率照片调整）
                maxZoom = 5f,              // 最大缩放倍数
                fling = true
            )
        }
        registerChanges()
    }

    /**
     * 选择尺寸（不立即更新使用时间，避免列表闪烁）
     */
    fun selectSize(size: IdPhotoSize) {
        setSize(size)
        // 注意：不在这里更新使用时间，改为在保存成功后更新
        // 这样可以避免列表因重新排序而闪烁
    }

    /**
     * 保存或更新尺寸
     */
    fun saveOrUpdateSize(size: IdPhotoSize) {
        componentScope.launch {
            val id = sizeRepository.saveOrUpdate(size)
            // 如果是新增的，更新当前尺寸
            if (size.id == 0L) {
                sizeRepository.getSizeById(id)?.let { saved ->
                    _currentSize.update { saved }
                }
            }
        }
    }

    /**
     * 删除尺寸
     */
    fun deleteSize(id: Long) {
        componentScope.launch {
            sizeRepository.deleteSize(id)
            // 如果删除的是当前选中的尺寸，选择第一个
            if (currentSize.id == id) {
                _allSizes.value.firstOrNull()?.let {
                    _currentSize.update { it }
                }
            }
        }
    }

    /**
     * 批量删除尺寸
     */
    fun batchDeleteSizes(ids: List<Long>) {
        componentScope.launch {
            sizeRepository.deleteSizes(ids)
            // 如果删除的包含当前选中的尺寸，选择第一个
            if (ids.contains(currentSize.id)) {
                _allSizes.value.firstOrNull()?.let {
                    _currentSize.update { it }
                }
            }
        }
    }

    /**
     * 恢复预置尺寸为默认值
     */
    fun resetPresetsToDefaults() {
        componentScope.launch {
            sizeRepository.resetPresetsOnly()
            // 选择第一个尺寸
            _allSizes.value.firstOrNull()?.let {
                _currentSize.update { it }
            }
        }
    }

    // ========== 背景色 ==========

    /**
     * 设置背景色
     */
    fun setBackground(background: IdPhotoBackground) {
        _currentBackground.update { background }
        registerChanges()
    }

    // ========== 图片处理 ==========

    /**
     * 加载图片
     */
    private fun loadImage(uri: Uri) {
        var loadedBitmap: Bitmap? = null
        debouncedImageCalculation(
            delay = 100L,
            action = {
                val imageData = imageGetter.getImage(uri.toString(), originalSize = true)
                loadedBitmap = imageData?.image
                // 记录原始图片尺寸
                loadedBitmap?.let { bmp ->
                    _originalImageSize.update { Pair(bmp.width, bmp.height) }
                }
            },
            onFinish = {
                updatePreviewBitmap(loadedBitmap)
            }
        )
    }

    /**
     * 设置裁剪后的图片
     */
    fun setCroppedBitmap(bitmap: Bitmap?) {
        _croppedBitmap.update { bitmap }
        registerChanges()
    }

    // ========== 导出配置 ==========
    /**
     * 更新导出配置
     */
    fun updateExportConfig(config: IdPhotoExportConfig) {
        _exportConfig.update { config }
    }

    /**
     * 设置导出格式
     */
    fun setExportFormat(format: ImageFormat) {
        _exportConfig.update { exportConfig.copy(format = format) }
    }

    // ========== 保存实现 ==========

    /**
     * 导出当前图片
     */
    override fun saveBitmap(
        oneTimeSaveLocationUri: String?,
        onComplete: (SaveResult) -> Unit,
    ) {
        val bitmap = _croppedBitmap.value ?: return
        val uri = _selectedUris.value.getOrNull(currentIndex) ?: return

        savingJob = componentScope.launch {
            startSaving()

            val imageInfo = ImageInfo(
                imageFormat = exportConfig.format,
                width = currentSize.widthPx,
                height = currentSize.heightPx,
                quality = exportConfig.quality.toQuality()
            )

            val result = fileController.save(
                saveTarget = ImageSaveTarget(
                    imageInfo = imageInfo,
                    originalUri = uri.toString(),
                    sequenceNumber = null,
                    metadata = null,
                    data = imageCompressor.compressAndTransform(
                        image = bitmap,
                        imageInfo = imageInfo
                    )
                ),
                keepOriginalMetadata = false,
                oneTimeSaveLocationUri = oneTimeSaveLocationUri
            ).onSuccess { success ->
                registerSave(success)
                _croppedBitmap.update { null }
            }

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
        // 证件照暂不支持批量保存，因为每张图片需要单独裁剪
        onComplete(emptyList())
    }

    /**
     * Int 转换为 Quality
     */
    private fun Int.toQuality(): com.t8rin.imagetoolbox.core.domain.image.model.Quality {
        return com.t8rin.imagetoolbox.core.domain.image.model.Quality.Base(this)
    }

    /**
     * 获取用于文件名选择的格式
     */
    override fun getFormatForFilenameSelection(): ImageFormat = exportConfig.format

    override fun resetState() {
        super.resetState()
        _currentSize.update { IdPhotoSize.DEFAULT }
        _currentBackground.update { IdPhotoBackground.DEFAULT }
        _croppedBitmap.update { null }
        _exportConfig.update { IdPhotoExportConfig() }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
        ): IdPhotoComponent
    }
}
