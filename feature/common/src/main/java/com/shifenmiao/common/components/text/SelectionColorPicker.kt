/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.common.components.text

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import com.wanbaohe.core.ui.widget.color_picker.ColorPickerSheet
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow

/**
 * 选区取色(图文卡片 / 图片创作共用的就地文字编辑)。
 *
 * 为什么单独做一层:选区工具条 [TextSelectionToolbar] 和选区手柄都是 Compose 的
 * 独立窗口(Popup),而取色面板走 ModalSheet(decorView 内的图层)。独立窗口永远压在
 * 装饰视图图层之上,两者同时存在时面板就会被盖住 —— 也就是"点调色板之后,选色面板
 * 跑到工具条下面"。所以有三条规矩:
 *
 * 1. 面板开着的时候工具条必须让位,面板只能挂在 [TextSelectionToolbar] 之外(不是 footer);
 * 2. 面板开着的时候把选区收起来(光标留在选区末端),否则两个紫色手柄会飘在面板上;
 * 3. 面板收起时把选区还原再上色,用户看到的还是原来那段选中文字。
 *
 * ```
 * val colorPicker = rememberSelectionColorPickerState(state)
 *
 * if (hasSelection && selectionToolbarVisible && !colorPicker.isSheetVisible) {
 *     TextSelectionToolbar(
 *         footer = if (colorPicker.quickColorRowVisible) {
 *             {{ colorPicker.QuickColorRow(currentColor) { color -> apply(color) } }}
 *         } else null
 *     )
 * }
 *
 * // 工具条外面:面板收起动画期间也继续挂着
 * colorPicker.ColorSheet { color -> apply(color) }
 * ```
 */
@Stable
class SelectionColorPickerState internal constructor(
    private val state: TextFieldState,
) {

    /** 工具条 footer 里的快选色行是否展开 */
    var quickColorRowVisible by mutableStateOf(false)
        private set

    /** 非空 = 全屏取色面板正在显示(种子色就是当前的文字颜色) */
    var pickerColor by mutableStateOf<Color?>(null)
        private set

    /** 打开面板时收起来的选区,面板关闭时还原 */
    private var suspendedSelection: TextRange? = null

    /** 宿主据此收起工具条:面板在的时候工具条必须让位 */
    val isSheetVisible: Boolean
        get() = pickerColor != null

    fun toggleQuickColorRow() {
        quickColorRowVisible = !quickColorRowVisible
    }

    fun collapseQuickColorRow() {
        quickColorRowVisible = false
    }

    fun openSheet(currentColor: Color) {
        val range = state.selection
        if (!range.collapsed) {
            // 手柄是独立窗口,不收起就会画在面板上面;光标留在选区末端,视觉上不动声色
            suspendedSelection = range
            state.edit { selection = TextRange(range.max.coerceIn(0, length)) }
        }
        pickerColor = currentColor
    }

    /**
     * 关闭面板:先把选区还原,再关。上色路径也先走这里,
     * 保证写样式时选区还是用户当初选中的那一段。
     */
    fun closeSheet() {
        restoreSelection()
        pickerColor = null
    }

    internal fun restoreSelection() {
        val range = suspendedSelection ?: return
        suspendedSelection = null
        state.edit {
            selection = TextRange(
                range.min.coerceIn(0, length),
                range.max.coerceIn(0, length)
            )
        }
    }
}

/** 选区取色状态;换编辑会话([state])时重建,避免残留上一个文字块的选区 */
@Composable
fun rememberSelectionColorPickerState(state: TextFieldState): SelectionColorPickerState {
    val colorPicker = remember(state) { SelectionColorPickerState(state) }
    val selectionCollapsed = state.selection.collapsed
    LaunchedEffect(selectionCollapsed) {
        // 选区没了就把快选色行收掉。面板不跟着关:它打开时会故意收起选区(见 openSheet),
        // 而且面板是模态的,期间选区不会被别的东西改掉。
        if (selectionCollapsed && !colorPicker.isSheetVisible) {
            colorPicker.collapseQuickColorRow()
        }
    }
    return colorPicker
}

/**
 * 工具条 footer:快选色行。点最左的调色板按钮不再就地拉面板,而是交给 [ColorSheet]
 * 在工具条之外承接(工具条届时会收起)。
 */
@Composable
fun SelectionColorPickerState.QuickColorRow(
    value: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    allowAlpha: Boolean = false,
) {
    ColorSelectionRow(
        modifier = modifier,
        value = value,
        allowAlpha = allowAlpha,
        onValueChange = { color ->
            onColorChange(color)
            collapseQuickColorRow()
        },
        onOpenPicker = { currentColor -> openSheet(currentColor) },
    )
}

/**
 * 全屏取色面板。必须挂在 [TextSelectionToolbar] 之外,否则它会画在工具条窗口下面。
 *
 * 面板收起后本 composable 仍留在树上,交给 [ColorPickerSheet] 自己播完下滑动画;
 * 面板不可见时它内部直接返回,不产生布局。
 */
@Composable
fun SelectionColorPickerState.ColorSheet(
    onColorPicked: (Color) -> Unit,
    allowAlpha: Boolean = false,
) {
    // 收起后 visible 变 false,种子色要留住,否则下一次打开会从黑色起步
    var seed by remember { mutableStateOf(Color.Black) }
    val current = pickerColor
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(current) {
        if (current != null) seed = current
        // 面板要占满下半屏:软键盘不收起会把面板底部的确定/收藏条挡在屏幕外
        if (current != null) keyboardController?.hide()
    }
    ColorPickerSheet(
        visible = current != null,
        onDismiss = { closeSheet() },
        color = current ?: seed,
        onColorSelected = { color ->
            // 先还原选区再上色:面板打开期间选区是收起来的
            restoreSelection()
            onColorPicked(color)
            closeSheet()
        },
        allowAlpha = allowAlpha,
    )
}
