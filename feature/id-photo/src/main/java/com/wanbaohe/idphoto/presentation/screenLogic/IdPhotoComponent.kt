package com.wanbaohe.idphoto.presentation.screenLogic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.imageprocess.ImageProcessOp
import com.shifenmiao.model.imageprocess.RetouchParams
import com.shifenmiao.network.repository.BaiduImageProcessRepository
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
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.logger.makeLog
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.data.IdPhotoSizeRepository
import com.wanbaohe.idphoto.domain.BeautyParamSpec
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.wanbaohe.idphoto.domain.IdPhotoExportConfig
import com.wanbaohe.idphoto.domain.IdPhotoSize
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.IdentityHashMap

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
    private val imageProcessRepository: BaiduImageProcessRepository,
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

    // ========== AI 美化与换底色管线 ==========
    //
    // 位图管线:originBitmap(选图原图) --AI美化(retouch)--> beautifiedBitmap
    //          --AI抠图+填色(segment+compose)--> 预览/裁剪/导出
    // 抠图前景按源位图实例缓存(IdentityHashMap):换颜色只本地重合成;
    // 美化后基底变化,若底色已生效会自动对新基底重抠(同一逻辑动作,不重复扣积分)。

    /** 当前图片解码出的原图(美化始终从它出发,避免叠加美化) */
    private var originBitmap: Bitmap? = null

    /** AI 美化后的位图,null 表示未美化 */
    private var beautifiedBitmap: Bitmap? = null

    /** 抠图前景缓存:key 为源位图实例(原图/美化图),切换图片时清空 */
    private val segmentForegroundCache = IdentityHashMap<Bitmap, Bitmap>()

    /** 当前美化参数,空表示未美化(预设档位只是参数组合,见 BeautyLevel) */
    private val _beautyParams: MutableState<RetouchParams> = mutableStateOf(RetouchParams.EMPTY)
    val beautyParams: RetouchParams by _beautyParams

    /** 编辑中的美化参数草稿:分组 tab 里的加减只改草稿,「应用」才发起修图调用 */
    private val _beautyDraft: MutableState<RetouchParams> = mutableStateOf(RetouchParams.EMPTY)
    val beautyDraft: RetouchParams by _beautyDraft

    /** AI 美化进行中 */
    private val _isBeautifying: MutableState<Boolean> = mutableStateOf(false)
    val isBeautifying: Boolean by _isBeautifying

    /** AI 抠图换底色进行中 */
    private val _isBgProcessing: MutableState<Boolean> = mutableStateOf(false)
    val isBgProcessing: Boolean by _isBgProcessing

    private var beautyJob: Job? = null
    private var bgJob: Job? = null

    /** 当前管线基底(美化图优先,否则原图) */
    private fun currentBase(): Bitmap? = beautifiedBitmap ?: originBitmap

    /** 选择该背景色是否需要先走一次 AI 抠图(UI 层据此做登录+积分预检) */
    fun requiresSegment(background: IdPhotoBackground): Boolean {
        val base = currentBase() ?: return false
        return !background.isOriginal && !segmentForegroundCache.containsKey(base)
    }

    /**
     * 设置背景色。「原图」直接回退基底;真实颜色若前景已缓存则本地合成,
     * 否则发起 AI 抠图(显式选择,成功扣积分)。
     */
    fun setBackground(background: IdPhotoBackground) {
        if (background == currentBackground && !requiresSegment(background)) return
        _currentBackground.update { background }
        registerChanges()
        val base = currentBase()
        if (background.isOriginal || base == null || segmentForegroundCache.containsKey(base)) {
            recomposePreview()
        } else {
            launchSegment(base, chargePoints = true)
        }
    }

    /**
     * 调整草稿单项参数:value 为 null 时清除该参数(连带清除 [spec] 的联动参数),
     * 否则设置并自动补齐联动参数(如牙齿美白三件套)。
     */
    fun setBeautyDraftValue(spec: BeautyParamSpec, value: Float?) {
        var draft = _beautyDraft.value.withValue(spec.key, value)
        if (value == null) {
            spec.companions.keys.forEach { draft = draft.withValue(it, null) }
        } else {
            spec.companions.forEach { (key, companionValue) ->
                draft = draft.withValue(key, companionValue)
            }
        }
        _beautyDraft.update { draft }
    }

    /** 整体替换美化草稿(一键美化档位写入草稿,由底部操作栏「应用」统一发起修图) */
    fun setBeautyDraft(params: RetouchParams) {
        _beautyDraft.update { params }
    }

    /** 放弃草稿的未应用修改:草稿回退到已生效参数,底部操作栏随之回到「保存」状态 */
    fun discardBeautyDraft() {
        _beautyDraft.update { _beautyParams.value }
    }

    /**
     * AI 人像美化:从原图出发调修图接口,成功后按所给参数更新管线。
     * 登录与积分预检由 UI 层(ActionUtils.ensureLoginAndCheckPoints)完成,
     * 仅成功才扣积分;底色已生效时自动对新基底重抠(不重复扣积分)。
     */
    fun applyBeauty(params: RetouchParams) {
        val origin = originBitmap ?: return
        beautyJob?.cancel()
        beautyJob = componentScope.launch {
            _isBeautifying.update { true }
            try {
                val result = imageProcessRepository.retouch(origin, params)
                val bitmap = result.getOrNull()
                if (bitmap == null) {
                    val failure = result.exceptionOrNull()
                    if (failure is CancellationException) throw failure
                    AppToastHost.showFailureToast(
                        failure?.message?.takeIf { it.isNotBlank() }
                            ?: AppContext.getContext().getString(R.string.id_photo_ai_process_failed)
                    )
                    return@launch
                }
                beautifiedBitmap = bitmap
                _beautyParams.update { params }
                _beautyDraft.update { params }
                consumeAiPoints(AppContext.getContext().getString(R.string.id_photo_beauty))
                if (!currentBackground.isOriginal) {
                    // 预览先回新基底,后台重抠完成后自动刷新合成图
                    recomposePreview()
                    launchSegment(bitmap, chargePoints = false)
                } else {
                    recomposePreview()
                }
            } finally {
                _isBeautifying.update { false }
            }
        }
    }

    /** 恢复原图:清除美化,底色仍生效时复用/补齐原图前景缓存(不扣积分) */
    fun restoreBeauty() {
        if (_isBeautifying.value) return
        beautifiedBitmap = null
        _beautyParams.update { RetouchParams.EMPTY }
        _beautyDraft.update { RetouchParams.EMPTY }
        registerChanges()
        val base = originBitmap ?: return
        if (!currentBackground.isOriginal && !segmentForegroundCache.containsKey(base)) {
            recomposePreview()
            launchSegment(base, chargePoints = false)
        } else {
            recomposePreview()
        }
    }

    /** 取消进行中的 AI 处理(美化/抠图) */
    fun cancelAiProcessing() {
        beautyJob?.cancel()
        beautyJob = null
        bgJob?.cancel()
        bgJob = null
        _isBeautifying.update { false }
        _isBgProcessing.update { false }
    }

    /**
     * 发起 AI 抠图并缓存前景,完成后刷新合成预览。
     * [chargePoints] 为 true(用户显式选择底色/换图自动抠图)时成功扣积分。
     */
    private fun launchSegment(base: Bitmap, chargePoints: Boolean) {
        // 新请求取消进行中的旧抠图,避免过期结果被丢弃后新请求也被忽略
        bgJob?.cancel()
        bgJob = componentScope.launch {
            _isBgProcessing.update { true }
            try {
                val result = imageProcessRepository.process(ImageProcessOp.Segment, base)
                val foreground = result.getOrNull()
                if (foreground == null) {
                    val failure = result.exceptionOrNull()
                    if (failure is CancellationException) throw failure
                    AppToastHost.showFailureToast(
                        failure?.message?.takeIf { it.isNotBlank() }
                            ?: AppContext.getContext().getString(R.string.id_photo_ai_process_failed)
                    )
                    return@launch
                }
                segmentForegroundCache[base] = foreground
                if (chargePoints) {
                    consumeAiPoints(AppContext.getContext().getString(R.string.id_photo_bg_replace))
                }
                // 基底与底色仍是最新状态才刷新预览(避免过期结果覆盖)
                if (currentBase() === base && !currentBackground.isOriginal) {
                    recomposePreview()
                }
            } finally {
                _isBgProcessing.update { false }
            }
        }
    }

    /** 管线重算统一出口:按当前基底与底色合成并刷新预览 */
    private fun recomposePreview() {
        val base = currentBase() ?: return
        val background = currentBackground
        val foreground = if (background.isOriginal) null else segmentForegroundCache[base]
        updatePreviewBitmap(
            if (foreground != null) composeOnBackground(foreground, background) else base
        )
    }

    /** 透明底前景合成到纯色背景上(本地操作,尺寸与原图一致) */
    private fun composeOnBackground(
        foreground: Bitmap,
        background: IdPhotoBackground
    ): Bitmap {
        val result = Bitmap.createBitmap(foreground.width, foreground.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(background.getColor().toArgb())
        canvas.drawBitmap(foreground, 0f, 0f, null)
        return result
    }

    private fun consumeAiPoints(desc: String) {
        BaseUtils.consumePoints(
            degree = com.shifenmiao.base.utils.aiImageProcessPointsCost(),
            desc = desc,
            source = AI_POINTS_SOURCE,
            showToast = true
        )
    }

    // ========== ImageBaseComponent 抽象方法实现 ==========

    override fun onUriSelected(uri: Uri) {
        loadImage(uri)
    }

    override fun onAllUrisCleared() {
        super.onAllUrisCleared()
        _croppedBitmap.update { null }
        resetAiPipeline()
    }

    /** 清空 AI 管线状态(换图/清图时调用) */
    private fun resetAiPipeline() {
        cancelAiProcessing()
        originBitmap = null
        beautifiedBitmap = null
        segmentForegroundCache.clear()
        _beautyParams.update { RetouchParams.EMPTY }
        _beautyDraft.update { RetouchParams.EMPTY }
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
    // setBackground 已并入「AI 美化与换底色管线」一节(真实抠图合成)。

    // ========== 图片处理 ==========

    /**
     * 加载图片:解码后缓存为管线原图并重置 AI 状态;
     * 若底色已生效(用户换图后保持底色选择),自动对新图抠图(成功扣积分)
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
                resetAiPipeline()
                originBitmap = loadedBitmap
                updatePreviewBitmap(loadedBitmap)
                val base = loadedBitmap
                if (base != null && !currentBackground.isOriginal) {
                    launchSegment(base, chargePoints = true)
                }
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
     * 批量导出所有图片:逐张解码 → 按当前尺寸比例居中裁剪 → 同参数压缩保存。
     *
     * 注意:批量不含 AI 美化/换底色(AI 处理按张扣积分,只作用于当前预览图),
     * 需要 AI 效果的图片请逐张保存。
     */
    override fun saveAllBitmaps(
        onProgress: (Int, Int) -> Unit,
        onComplete: (List<SaveResult>) -> Unit
    ) {
        val uris = _selectedUris.value
        if (uris.isEmpty()) {
            onComplete(emptyList())
            return
        }
        savingJob = componentScope.launch {
            startBatchSaving(uris.size)

            val imageInfo = ImageInfo(
                imageFormat = exportConfig.format,
                width = currentSize.widthPx,
                height = currentSize.heightPx,
                quality = exportConfig.quality.toQuality()
            )

            val results = mutableListOf<SaveResult>()
            uris.forEachIndexed { index, uri ->
                val decoded = imageGetter.getImage(uri.toString(), originalSize = true)?.image
                val result = if (decoded == null) {
                    SaveResult.Error.Exception(Exception("图片解码失败: $uri"))
                } else {
                    val cropped = centerCrop(decoded, currentSize.aspectRatio)
                    fileController.save(
                        saveTarget = ImageSaveTarget(
                            imageInfo = imageInfo,
                            originalUri = uri.toString(),
                            sequenceNumber = index,
                            metadata = null,
                            data = imageCompressor.compressAndTransform(
                                image = cropped,
                                imageInfo = imageInfo
                            )
                        ),
                        keepOriginalMetadata = false,
                        oneTimeSaveLocationUri = null
                    )
                }
                result.onSuccess { registerSave() }
                results.add(result)
                updateBatchProgress(index + 1, uris.size)
                onProgress(index, uris.size)
            }

            finishSaving()
            onComplete(results)
        }
    }

    /** 按目标宽高比居中裁剪;源图比例已匹配时原样返回 */
    private fun centerCrop(source: Bitmap, aspectRatio: Float): Bitmap {
        val sourceAspect = source.width.toFloat() / source.height
        if (kotlin.math.abs(sourceAspect - aspectRatio) < 0.01f) return source
        val cropWidth: Int
        val cropHeight: Int
        if (sourceAspect > aspectRatio) {
            // 源图更宽,裁两侧
            cropHeight = source.height
            cropWidth = (cropHeight * aspectRatio).toInt().coerceIn(1, source.width)
        } else {
            // 源图更高,裁上下
            cropWidth = source.width
            cropHeight = (cropWidth / aspectRatio).toInt().coerceIn(1, source.height)
        }
        val left = (source.width - cropWidth) / 2
        val top = (source.height - cropHeight) / 2
        return Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
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
        resetAiPipeline()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
        ): IdPhotoComponent
    }
}

/** AI 处理(美化/换底色)积分来源标识 */
private const val AI_POINTS_SOURCE = "id_photo_ai"
