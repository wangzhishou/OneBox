/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 图片处理模块的基础 Component
 *
 * ## 模块位置
 * - **模块**: `core/ui`
 * - **包路径**: `com.t8rin.imagetoolbox.core.ui.utils`
 * - **文件**: `ImageBaseComponent.kt`
 *
 * ## 功能概述
 * 提供图片处理模块的通用状态管理和操作：
 * - URI 管理（单张/批量）
 * - 图片切换与索引
 * - 预览 Bitmap 管理
 * - 保存状态管理
 * - 导出格式/质量配置
 *
 * ## 继承关系
 * ```
 * BaseComponent (基础组件)
 *     └── ImageBaseComponent (图片处理基础组件)
 *             └── CameraWatermarkComponent (相机水印)
 *             └── DrawComponent (绘图)
 *             └── SingleEditComponent (单图编辑)
 *             └── ... (其他图片处理模块)
 * ```
 *
 * ## 使用示例
 * ```kotlin
 * class MyImageComponent @AssistedInject constructor(
 *     @Assisted componentContext: ComponentContext,
 *     @Assisted val initialUri: Uri?,
 *     dispatchersHolder: DispatchersHolder,
 *     // ... 其他依赖
 * ) : ImageBaseComponent(dispatchersHolder, componentContext) {
 *
 *     init {
 *         debounce {
 *             initialUri?.let { setSelectedUri(it) }
 *         }
 *     }
 *
 *     override fun onUriSelected(uri: Uri) {
 *         // 加载图片逻辑
 *         loadImage(uri)
 *     }
 *
 *     override fun saveBitmap(
 *         oneTimeSaveLocationUri: String?,
 *         onComplete: (SaveResult) -> Unit
 *     ) {
 *         // 保存图片逻辑
 *     }
 * }
 * ```
 *
 * ## 可复用功能
 * | 功能 | 属性/方法 | 说明 |
 * |------|----------|------|
 * | URI 管理 | `selectedUris`, `currentIndex` | 支持单张和批量图片 |
 * | URI 操作 | `setSelectedUris()`, `addUris()`, `removeUri()` | 增删改查 |
 * | 图片切换 | `switchToIndex()`, `switchToNext()`, `switchToPrevious()` | 批量时切换 |
 * | 预览管理 | `previewBitmap`, `updatePreviewBitmap()` | 预览 Bitmap |
 * | 保存状态 | `isSaving`, `exportProgress` | 保存进度 |
 * | 保存控制 | `startSaving()`, `finishSaving()`, `cancelSaving()` | 保存流程控制 |
 * | 导出配置 | `imageFormat`, `quality` | 格式和质量 |
 *
 * ## 抽象方法（子类必须实现）
 * - `onUriSelected(uri: Uri)`: 当选中新的 URI 时调用
 * - `saveBitmap(...)`: 保存当前图片
 *
 * @see BaseComponent 基础组件
 * @see com.shifenmiao.common.ui.ImageBaseScreen 配套的 UI 组件
 */
@Stable
abstract class ImageBaseComponent(
    dispatchersHolder: DispatchersHolder,
    componentContext: ComponentContext
) : BaseComponent(dispatchersHolder, componentContext) {

    // ==================== URI 管理 ====================

    /**
     * 已选择的图片 URI 列表（支持单张和批量）
     */
    protected val _selectedUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedUris: StateFlow<List<Uri>> = _selectedUris.asStateFlow()

    /**
     * 当前显示的图片索引
     */
    protected val _currentIndex: MutableState<Int> = mutableIntStateOf(0)
    val currentIndex: Int by _currentIndex

    /**
     * 当前选中的 URI
     */
    val currentUri: Uri?
        get() = _selectedUris.value.getOrNull(currentIndex)

    /**
     * 是否有选中的图片
     */
    val hasSelectedImages: Boolean
        get() = _selectedUris.value.isNotEmpty()

    /**
     * 选中的图片数量
     */
    val selectedCount: Int
        get() = _selectedUris.value.size

    /**
     * 设置选择的图片（替换现有列表）
     */
    open fun setSelectedUris(uris: List<Uri>) {
        _selectedUris.value = uris
        _currentIndex.update { 0 }
        if (uris.isNotEmpty()) {
            onUriSelected(uris.first())
        }
        registerChanges()
    }

    /**
     * 设置单张图片
     */
    fun setSelectedUri(uri: Uri) {
        setSelectedUris(listOf(uri))
    }

    /**
     * 添加图片（追加到现有列表）
     */
    open fun addUris(uris: List<Uri>) {
        val isFirstAdd = _selectedUris.value.isEmpty()
        _selectedUris.value = _selectedUris.value + uris
        if (isFirstAdd && uris.isNotEmpty()) {
            onUriSelected(uris.first())
        }
        registerChanges()
    }

    /**
     * 移除图片
     */
    open fun removeUri(uri: Uri) {
        val newList = _selectedUris.value.filter { it != uri }
        _selectedUris.value = newList

        // 调整当前索引
        if (currentIndex >= newList.size) {
            _currentIndex.update { (newList.size - 1).coerceAtLeast(0) }
        }

        // 加载新的当前图片或清空
        if (newList.isNotEmpty()) {
            onUriSelected(newList[currentIndex])
        } else {
            onAllUrisCleared()
            registerChangesCleared()
        }
    }

    /**
     * 切换到指定索引的图片
     */
    open fun switchToIndex(index: Int) {
        if (index in _selectedUris.value.indices && index != currentIndex) {
            _currentIndex.update { index }
            _selectedUris.value.getOrNull(index)?.let { onUriSelected(it) }
        }
    }

    /**
     * 切换到下一张图片
     */
    fun switchToNext() {
        val nextIndex = (currentIndex + 1) % selectedCount
        switchToIndex(nextIndex)
    }

    /**
     * 切换到上一张图片
     */
    fun switchToPrevious() {
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else selectedCount - 1
        switchToIndex(prevIndex)
    }

    /**
     * 清空所有图片
     */
    open fun clearAllUris() {
        _selectedUris.value = emptyList()
        _currentIndex.update { 0 }
        onAllUrisCleared()
        registerChangesCleared()
    }

    /**
     * 当选中新的 URI 时调用（子类实现加载逻辑）
     */
    protected abstract fun onUriSelected(uri: Uri)

    /**
     * 当所有 URI 被清空时调用
     */
    protected open fun onAllUrisCleared() {
        _previewBitmap.update { null }
    }

    // ==================== 预览 Bitmap 管理 ====================

    /**
     * 预览 Bitmap
     */
    protected val _previewBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val previewBitmap: Bitmap? by _previewBitmap

    /**
     * 更新预览 Bitmap
     */
    protected fun updatePreviewBitmap(bitmap: Bitmap?) {
        _previewBitmap.update { bitmap }
    }

    // ==================== 保存状态管理 ====================

    /**
     * 是否正在保存
     */
    protected val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    /**
     * 保存任务
     */
    protected var savingJob: Job? by smartJob {
        _isSaving.update { false }
        _done.update { 0 }
        _left.update { 0 }
    }

    /**
     * 导出进度 (0-100)
     */
    protected val _exportProgress: MutableState<Int> = mutableIntStateOf(0)
    val exportProgress: Int by _exportProgress

    /**
     * 已完成数量（用于 LoadingDialog 进度显示）
     */
    protected val _done: MutableState<Int> = mutableIntStateOf(0)
    val done: Int by _done

    /**
     * 剩余数量（用于 LoadingDialog 进度显示）
     */
    protected val _left: MutableState<Int> = mutableIntStateOf(0)
    val left: Int by _left

    /**
     * 取消保存操作
     */
    open fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.update { false }
        _done.update { 0 }
        _left.update { 0 }
    }

    /**
     * 开始保存
     */
    protected fun startSaving() {
        _isSaving.update { true }
        _exportProgress.update { 0 }
        _done.update { 0 }
        _left.update { 0 }
    }

    /**
     * 开始批量保存
     * @param total 总数量
     */
    protected fun startBatchSaving(total: Int) {
        _isSaving.update { true }
        _exportProgress.update { 0 }
        _done.update { 0 }
        _left.update { total }
    }

    /**
     * 更新批量保存进度
     * @param completed 已完成数量
     * @param total 总数量
     */
    protected fun updateBatchProgress(completed: Int, total: Int) {
        _done.update { completed }
        _left.update { total - completed }
        _exportProgress.update { if (total > 0) (completed * 100) / total else 0 }
    }

    /**
     * 结束保存
     */
    protected fun finishSaving() {
        _isSaving.update { false }
        _exportProgress.update { 100 }
        _done.update { 0 }
        _left.update { 0 }
    }

    /**
     * 更新导出进度
     */
    protected fun updateExportProgress(progress: Int) {
        _exportProgress.update { progress.coerceIn(0, 100) }
    }

    /**
     * 计算批量导出进度
     */
    protected fun calculateBatchProgress(currentIndex: Int, total: Int): Int {
        return if (total > 0) ((currentIndex + 1) * 100) / total else 0
    }

    // ==================== 导出配置 ====================

    /**
     * 导出图片格式
     */
    protected val _imageFormat: MutableState<ImageFormat> = mutableStateOf(ImageFormat.Default)
    val imageFormat: ImageFormat by _imageFormat

    /**
     * 导出质量
     */
    protected val _quality: MutableState<Quality> = mutableStateOf(Quality.Base(100))
    val quality: Quality by _quality

    /**
     * 设置导出格式
     */
    open fun setImageFormat(format: ImageFormat) {
        _imageFormat.update { format }
        registerChanges()
    }

    /**
     * 设置导出质量
     */
    open fun setQuality(quality: Quality) {
        _quality.update { quality }
        registerChanges()
    }

    /**
     * 设置导出质量（Int 版本）
     */
    fun setQuality(value: Int) {
        setQuality(Quality.Base(value.coerceIn(1, 100)))
    }

    /**
     * 获取用于文件名选择的格式
     */
    open fun getFormatForFilenameSelection(): ImageFormat = imageFormat

    // ==================== 抽象保存方法 ====================

    /**
     * 保存当前图片
     *
     * @param oneTimeSaveLocationUri 一次性保存位置，为 null 时使用默认位置
     * @param onComplete 保存完成回调
     */
    abstract fun saveBitmap(
        oneTimeSaveLocationUri: String? = null,
        onComplete: (SaveResult) -> Unit
    )

    /**
     * 批量保存所有图片
     *
     * @param onProgress 进度回调 (当前索引, 总数)
     * @param onComplete 完成回调，返回所有保存结果
     */
    open fun saveAllBitmaps(
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: (List<SaveResult>) -> Unit
    ) {
        // 默认实现：只保存当前图片
        saveBitmap { result ->
            onComplete(listOf(result))
        }
    }

    // ==================== 生命周期 ====================

    /**
     * 重置状态
     */
    override fun resetState() {
        _selectedUris.value = emptyList()
        _currentIndex.update { 0 }
        _previewBitmap.update { null }
        _isSaving.update { false }
        _exportProgress.update { 0 }
        _imageFormat.update { ImageFormat.Default }
        _quality.update { Quality.Base(100) }
        registerChangesCleared()
    }
}

