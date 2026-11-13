/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.wanbaohe.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.feature.pick_color.domain.ColorPickHistoryRepository
import com.t8rin.imagetoolbox.feature.pick_color.presentation.model.PickColorTab
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PickColorFromImageComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUri: Uri?,
    @Assisted val onGoBack: () -> Unit,
    private val imageGetter: ImageGetter<Bitmap>,
    private val colorPickHistoryRepository: ColorPickHistoryRepository,
    dispatchersHolder: DispatchersHolder
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

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    /**
     * 图片配色的取色源：原图 4000px 直接做颜色量化会卡主线程，
     * 加载图时顺手缩到 ~180px，量化 25k 像素毫秒级完成，主色分布不变。
     */
    private val _paletteSource: MutableState<ImageBitmap?> = mutableStateOf(null)
    val paletteSource: ImageBitmap? by _paletteSource

    private val _color: MutableState<Color> = mutableStateOf(Color.Unspecified)
    val color: Color by _color

    private val _tab: MutableState<PickColorTab> = mutableStateOf(PickColorTab.PICK)
    val tab: PickColorTab by _tab

    private val _history: MutableState<List<ColorPickHistoryEntry>> =
        mutableStateOf(colorPickHistoryRepository.load())
    val history: List<ColorPickHistoryEntry> by _history

    private val _uri = mutableStateOf<Uri?>(null)
    val uri: Uri? by _uri

    val colorArgb: Int?
        get() = color.takeIf { it.isSpecified }?.toArgb()

    fun setUri(
        uri: Uri,
        onFailure: (Throwable) -> Unit
    ) {
        _uri.value = uri
        _color.value = Color.Unspecified
        _paletteSource.value = null
        componentScope.launch {
            runSuspendCatching {
                val bitmap = imageGetter.getImage(
                    data = uri,
                    size = 4000
                )
                _bitmap.value = bitmap
                _paletteSource.value = bitmap?.let {
                    kotlinx.coroutines.withContext(ioDispatcher) {
                        it.toPaletteSource()
                    }
                }
            }.onFailure(onFailure)
        }
    }

    private fun Bitmap.toPaletteSource(): ImageBitmap {
        val maxSide = 180
        val longest = maxOf(width, height)
        if (longest <= maxSide) return asImageBitmap()
        val scale = maxSide.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            this,
            maxOf(1, (width * scale).toInt()),
            maxOf(1, (height * scale).toInt()),
            true
        )
        return scaled.asImageBitmap()
    }

    fun updateColor(color: Color) {
        _color.value = color
    }

    fun selectTab(tab: PickColorTab) {
        _tab.value = tab
    }

    /**
     * 记一条取色历史。
     *
     * 拖动过程中会以高频回调 [updateColor]，改由 UI 在颜色稳定后调用这里，
     * 避免把每一帧都写进 MMKV。
     */
    fun recordColor(color: Color) {
        if (!color.isSpecified) return
        _history.value = colorPickHistoryRepository.record(color.toArgb())
    }

    fun isFavorite(color: Color): Boolean {
        if (!color.isSpecified) return false
        val argb = color.toArgb()
        return _history.value.any { it.argb == argb && it.isFavorite }
    }

    fun toggleFavorite(color: Color) {
        if (!color.isSpecified) return
        _history.value = colorPickHistoryRepository.setFavorite(
            argb = color.toArgb(),
            isFavorite = !isFavorite(color)
        )
    }

    fun applyHistoryColor(argb: Int) {
        _color.value = Color(argb)
        _history.value = colorPickHistoryRepository.record(argb)
        if (_bitmap.value != null) {
            _tab.value = PickColorTab.PICK
        }
    }

    fun removeHistoryEntry(argb: Int) {
        _history.value = colorPickHistoryRepository.remove(argb)
    }

    fun clearHistory() {
        colorPickHistoryRepository.clear()
        _history.value = emptyList()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            onGoBack: () -> Unit,
        ): PickColorFromImageComponent
    }
}
