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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItemDefaults
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText

/**
 * 分段选择按钮组。
 *
 * **视觉实现已统一到 [GlassSegmentedButtonRow]**（毛玻璃分段行）：
 * 每个按钮独立圆角、滑块带玻璃描边，选中项有色调叠加。
 * 这里保留原有 API（`items` / `selectedIndex` / `title` / 颜色参数），
 * 因此全项目 20 余处调用点无需改动即可切到新的玻璃视觉。
 *
 * 多选重载（`values` / `selectedIndices`）在玻璃行上表示为多个「已选」滑块：
 * 玻璃行本身只支持单选，多选时会把每个选中项都画成滑块。
 */
@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    items: List<String>,
    selectedIndex: Int,
    title: String? = null,
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface
) {
    EnhancedButtonGroup(
        enabled = enabled,
        items = items,
        selectedIndex = selectedIndex,
        onIndexChange = onIndexChange,
        modifier = modifier,
        title = {
            title?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        inactiveButtonColor = inactiveButtonColor
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean,
    items: List<String>,
    selectedIndex: Int,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface
) {
    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = items.size,
        selectedIndex = selectedIndex,
        itemContent = {
            AutoSizeText(
                text = items[it],
                style = LocalTextStyle.current.copy(
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
        },
        onIndexChange = onIndexChange,
        title = title,
        inactiveButtonColor = inactiveButtonColor
    )
}

@Composable
fun <T> EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    entries: List<T>,
    value: T,
    itemContent: @Composable (item: T) -> Unit,
    title: String?,
    onValueChange: (T) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    isScrollable: Boolean = true,
    contentPadding: PaddingValues = DefaultContentPadding
) {
    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = entries.size,
        selectedIndex = entries.indexOf(value),
        itemContent = {
            itemContent(entries[it])
        },
        onIndexChange = {
            onValueChange(
                entries[it]
            )
        },
        title = {
            title?.let {
                Text(
                    text = title,
                    style = PreferenceItemDefaults.TitleFontStyleCentered,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }
        },
        inactiveButtonColor = inactiveButtonColor,
        activeButtonColor = activeButtonColor,
        isScrollable = isScrollable,
        contentPadding = contentPadding
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    itemCount: Int,
    selectedIndex: Int,
    itemContent: @Composable (item: Int) -> Unit,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    isScrollable: Boolean = true,
    contentPadding: PaddingValues = DefaultContentPadding
) {
    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = itemCount,
        selectedIndices = setOf(selectedIndex),
        itemContent = itemContent,
        title = title,
        onIndexChange = onIndexChange,
        inactiveButtonColor = inactiveButtonColor,
        activeButtonColor = activeButtonColor,
        isScrollable = isScrollable,
        contentPadding = contentPadding
    )
}

@Composable
fun <T> EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    entries: List<T>,
    values: List<T>,
    itemContent: @Composable (item: T) -> Unit,
    title: String?,
    onValueChange: (T) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    isScrollable: Boolean = true,
    contentPadding: PaddingValues = DefaultContentPadding
) {
    val selectedIndices by remember(values, entries) {
        derivedStateOf {
            values.mapTo(mutableSetOf()) { entries.indexOf(it) }
        }
    }

    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = entries.size,
        selectedIndices = selectedIndices,
        itemContent = {
            itemContent(entries[it])
        },
        onIndexChange = {
            onValueChange(
                entries[it]
            )
        },
        title = {
            title?.let {
                Text(
                    text = title,
                    style = PreferenceItemDefaults.TitleFontStyleCentered,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }
        },
        inactiveButtonColor = inactiveButtonColor,
        activeButtonColor = activeButtonColor,
        isScrollable = isScrollable,
        contentPadding = contentPadding
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    itemCount: Int,
    selectedIndices: Set<Int>,
    itemContent: @Composable (item: Int) -> Unit,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    isScrollable: Boolean = true,
    contentPadding: PaddingValues = DefaultContentPadding
) {
    val disabledColor = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.38f)
        .compositeOver(MaterialTheme.colorScheme.surface)

    // 兼容层：视觉完全交给 GlassSegmentedButtonRow 的默认玻璃样式 ——
    // 与「日夜间模式」用的是同一个组件、同一套默认值，不再另调底色/容器样式。
    // 老调用方传的 surface / secondaryContainer 是当年的默认值，这里视为「没传」。
    val selectedColor = activeButtonColor.takeIf {
        it != MaterialTheme.colorScheme.secondaryContainer
    }
    val unselectedColor = inactiveButtonColor.takeIf {
        it != MaterialTheme.colorScheme.surface
    }
    val isMultiSelect = selectedIndices.size > 1

    ProvideTextStyle(
        value = LocalTextStyle.current.copy(
            color = if (!enabled) disabledColor
            else Color.Unspecified
        )
    ) {
        Column(
            modifier = modifier.alpha(if (enabled) 1f else 0.55f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = title
            )

            val row: @Composable (Modifier) -> Unit = { rowModifier ->
                val sharedLabel: @Composable (Int) -> Unit = { itemContent(it) }
                val rowContentPadding =
                    if (isScrollable) DefaultContentPadding else contentPadding
                if (isMultiSelect) {
                    GlassSegmentedButtonRow(
                        options = (0 until itemCount).toList(),
                        selectedOptions = selectedIndices,
                        onOptionSelected = { index, _ -> onIndexChange(index) },
                        modifier = rowModifier,
                        label = sharedLabel,
                        selectedColor = selectedColor
                            ?: MaterialTheme.colorScheme.primaryContainer,
                        unselectedColor = unselectedColor ?: Color.Transparent,
                        contentPadding = rowContentPadding,
                        hugContent = isScrollable,
                    )
                } else {
                    GlassSegmentedButtonRow(
                        options = (0 until itemCount).toList(),
                        selectedOption = selectedIndices.firstOrNull() ?: 0,
                        onOptionSelected = onIndexChange,
                        modifier = rowModifier,
                        label = sharedLabel,
                        selectedColor = selectedColor
                            ?: MaterialTheme.colorScheme.primaryContainer,
                        unselectedColor = unselectedColor ?: Color.Transparent,
                        contentPadding = rowContentPadding,
                        hugContent = isScrollable,
                    )
                }
            }

            if (isScrollable) {
                val scrollState = rememberScrollState()
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val rowWidth = maxWidth
                    Row(
                        modifier = Modifier
                            .fadingEdges(scrollState)
                            .horizontalScroll(scrollState),
                    ) {
                        row(
                            Modifier
                                .widthIn(min = rowWidth)
                                .padding(contentPadding)
                        )
                    }
                }
            } else {
                row(
                    Modifier
                        .fillMaxWidth()
                        .padding(contentPadding)
                )
            }
        }
    }
}

private val defaultModifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)

private val DefaultContentPadding = PaddingValues(
    start = 6.dp,
    end = 6.dp,
    bottom = 6.dp,
    top = 8.dp
)
