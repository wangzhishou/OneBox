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

package com.t8rin.imagetoolbox.color_tools.presentation.screenLogic

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.state.update
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ColorToolsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    /** 打开时落在哪个 tab，见 Screen.ColorTools.TAB_* */
    @Assisted val initialTab: Int,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _selectedColor: MutableState<Color> = mutableStateOf(Color.Unspecified)
    val selectedColor: Color by _selectedColor

    /**
     * 当前生效的调色板 id（null = 取用户列表里的第一组）。
     *
     * 色彩工具顶上那行快捷色板就是这一组颜色；调色板页切换调色板时，
     * 这行快捷选择跟着变，四个 tab 共用同一份选择。
     */
    private val _activePaletteId: MutableState<String?> = mutableStateOf(null)
    val activePaletteId: String? by _activePaletteId

    fun updateSelectedColor(newColor: Color) {
        _selectedColor.update { newColor }
    }

    fun selectPalette(id: String?) {
        _activePaletteId.value = id
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            initialTab: Int,
        ): ColorToolsComponent
    }

}