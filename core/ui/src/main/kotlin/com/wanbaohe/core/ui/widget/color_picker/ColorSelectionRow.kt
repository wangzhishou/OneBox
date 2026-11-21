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

package com.wanbaohe.core.ui.widget.color_picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.ContextUtils.pasteColorFromClipboard
import com.wanbaohe.core.ui.utils.provider.LocalContainerColor
import com.wanbaohe.core.ui.utils.provider.ProvideContainerDefaults
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.enhanced.hapticsCombinedClickable
import com.wanbaohe.core.ui.widget.modifier.fadingEdges
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 横向快选色行。
 *
 * [onOpenPicker] 为空时,点最左的调色板按钮会在色行内部就地拉起 [ColorPickerSheet];
 * 提供时只把"想取色"(带当前色)抛给宿主,由宿主决定面板挂在哪一层 ——
 * 浮动工具条这类独立窗口宿主必须这么做,否则面板会被窗口压住(见 SelectionColorPicker)。
 */
@Composable
fun ColorSelectionRow(
    modifier: Modifier = Modifier,
    defaultColors: List<Color> = ColorSelectionRowDefaults.colorList,
    allowAlpha: Boolean = false,
    allowScroll: Boolean = true,
    value: Color,
    onValueChange: (Color) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    onOpenPicker: ((Color) -> Unit)? = null,
    swatchShape: Shape = CircleShape,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var customColor by remember { mutableStateOf<Color?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    // 不允许 alpha 的场景（部分滤镜参数）不该出现透明色：
    // 透明色会被当成「黑色 + 不透明度 1」，点一下反而变成纯黑，容易误解。
    val shownColors = remember(defaultColors, allowAlpha) {
        if (allowAlpha) defaultColors else defaultColors.filter { it.alpha >= 1f }
    }
    // 展开时直接从选中色开始,避免先渲染 index 0 再 scrollToItem 造成的跳动。
    // 选中色不在预设色板里(自定义取色)时从 0 开始,因为取色入口固定在最左侧。
    val initialIndex = remember(shownColors, allowScroll) {
        if (!allowScroll) {
            0
        } else {
            shownColors.indexOf(value).takeIf { it >= 0 } ?: 0
        }
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    // 首次组合由 initialFirstVisibleItemIndex 定位；之后的选中色/色板变化才做滚动动画，
    // 避免「调色板里新加的颜色」在屏幕外的快捷色行上完全看不到。
    var scrollReady by remember { mutableStateOf(false) }

    LaunchedEffect(value, shownColors) {
        customColor = value.takeIf { it !in shownColors }
        if (!scrollReady) {
            scrollReady = true
            return@LaunchedEffect
        }
        if (!allowScroll) return@LaunchedEffect
        val index = shownColors.indexOf(value)
        if (index < 0) return@LaunchedEffect
        val alreadyVisible = listState.layoutInfo.visibleItemsInfo
            .any { it.index == index }
        if (!alreadyVisible) {
            listState.animateScrollToItem(index)
        }
    }

    val itemSize = 44.dp

    ProvideContainerDefaults(
        color = LocalContainerColor.current
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 自定义取色入口：和预设色块同一形状；选中时只显示对勾，不做形变
            val background = customColor ?: MaterialTheme.colorScheme.primaryContainer
            val isSelected = customColor != null
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .aspectRatio(1f)
                        .clip(swatchShape)
                        .then(
                            if (background.alpha < 1f) Modifier.transparencyChecker()
                            else Modifier
                        )
                        .background(background, swatchShape)
                        .then(
                            // 自定义色才需要给浅色补可见性描边；primaryContainer 入口本身够清晰
                            if (customColor != null) {
                                Modifier.colorSwatchOutline(
                                    color = background,
                                    shape = swatchShape
                                )
                            } else {
                                Modifier
                            }
                        )
                        .hapticsCombinedClickable(
                            onLongClick = {
                                context.pasteColorFromClipboard(
                                    onPastedColor = {
                                        val color = if (allowAlpha) it else it.copy(1f)

                                        onValueChange(color)
                                        customColor = color
                                    },
                                    onPastedColorFailure = { message ->
                                        scope.launch {
                                            AppToastHost.showToast(
                                                message = message,
                                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError
                                            )
                                        }
                                    }
                                )
                            },
                            onClick = {
                                if (onOpenPicker != null) {
                                    onOpenPicker(customColor ?: value)
                                } else {
                                    showColorPicker = true
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme
                        },
                        contentDescription = null,
                        tint = if (isSelected) {
                            background.checkmarkColor()
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(if (isSelected) 22.dp else 26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            LazyRow(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .fadingEdges(listState),
                userScrollEnabled = allowScroll,
                contentPadding = PaddingValues(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(
                    items = shownColors,
                    key = { it.toArgb() }
                ) { color ->
                    val isSelected = value == color && customColor == null
                    Box(
                        Modifier
                            .size(itemSize)
                            .aspectRatio(1f)
                            .clip(swatchShape)
                            .then(
                                if (color.alpha < 1f) Modifier.transparencyChecker()
                                else Modifier
                            )
                            .background(color, swatchShape)
                            .colorSwatchOutline(color = color, shape = swatchShape)
                            .hapticsClickable {
                                onValueChange(color.copy(if (allowAlpha) color.alpha else 1f))
                                customColor = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ColorCheckmark(
                            isSelected = isSelected,
                            color = color
                        )
                    }
                    if (color != shownColors.last()) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }

    ColorPickerSheet(
        visible = showColorPicker,
        onDismiss = { showColorPicker = false },
        color = customColor,
        onColorSelected = {
            val color = it.copy(if (allowAlpha) it.alpha else 1f)
            onValueChange(color)
            customColor = color
        },
        allowAlpha = allowAlpha
    )
}


@Composable
private fun ColorCheckmark(
    isSelected: Boolean,
    color: Color
) {
    AnimatedVisibility(isSelected) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
            contentDescription = null,
            tint = color.checkmarkColor(),
            modifier = Modifier.size(22.dp)
        )
    }
}

/** 色块上对勾的反色：透明/半透明用主题前景色，其余按色块亮度反色。 */
@Composable
private fun Color.checkmarkColor(): Color = if (alpha < 1f) {
    MaterialTheme.colorScheme.onSurface
} else {
    contentColor()
}

object ColorSelectionRowDefaults {
    val colorList by lazy {
        listOf(
            Color(0xFFf8130d),
            Color(0xFFb8070d),
            Color(0xFF7a000b),
            Color(0xFF8a3a00),
            Color(0xFFff7900),
            Color(0xFFfcf721),
            Color(0xFFf8df09),
            Color(0xFFc0dc18),
            Color(0xFF88dd20),
            Color(0xFF07ddc3),
            Color(0xFF01a0a3),
            Color(0xFF59cbf0),
            Color(0xFF005FFF),
            Color(0xFFfa64e1),
            Color(0xFFfc50a6),
            Color(0xFFd7036a),
            Color(0xFFdb94fe),
            Color(0xFFb035f8),
            Color(0xFF7b2bec),
            Color(0xFF022b6d),
            Color(0xFFFFFFFF),
            Color(0xFF768484),
            Color(0xFF333333),
            Color(0xFF000000),
        )
    }
}
