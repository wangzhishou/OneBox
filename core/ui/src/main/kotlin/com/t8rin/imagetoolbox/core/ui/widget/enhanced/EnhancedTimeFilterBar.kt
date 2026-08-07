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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDateRange

/**
 * 时间筛选条 — 横向滚动单行。
 *
 * 一行展示：
 *   1) [日期段 chip] — 始终显示 [customRangeLabel]，点击触发 [onCustomRangeClick]
 *   2) 一组 [TimeFilterPreset] chip — 选中态互斥
 *
 * 所有 item 共享同一个 [GlassFilterChip] 实现，因此行高、圆角、内容内边距完全一致；
 * 默认圆角为 [TimeFilterBarCornerRadius]（16.dp，可通过参数覆盖）。
 *
 * @param customRangeLabel  日期段 chip 上要展示的文本，由调用方按需格式化（如 "5月28日 - 6月3日"）
 * @param isCustomRangeSelected  自定义日期段是否处于选中态
 * @param onCustomRangeClick  点击日期段 chip 时的回调（通常用于唤起日期范围选择器）
 * @param presets  预设时间筛选 chip 列表
 * @param selectedPresetKey  当前选中的预设 key，与 [presets] 中某项的 [TimeFilterPreset.key] 一致；
 *                           为 null 时表示没有预设被选中（此时 [isCustomRangeSelected] 应为 true）
 * @param onPresetSelected  点击预设 chip 时的回调，参数为被点中的 [TimeFilterPreset]
 */
@Composable
fun EnhancedTimeFilterBar(
    customRangeLabel: String,
    isCustomRangeSelected: Boolean,
    onCustomRangeClick: () -> Unit,
    presets: List<TimeFilterPreset>,
    selectedPresetKey: String?,
    onPresetSelected: (TimeFilterPreset) -> Unit,
    modifier: Modifier = Modifier,
    customRangeLeadingIcon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDateRange,
    cornerRadius: androidx.compose.ui.graphics.Shape = TimeFilterBarShape,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DateRangeChip(
            label = customRangeLabel,
            selected = isCustomRangeSelected,
            leadingIcon = customRangeLeadingIcon,
            onClick = onCustomRangeClick,
            shape = cornerRadius,
        )
        presets.forEach { preset ->
            TimeFilterChip(
                label = preset.label,
                selected = !isCustomRangeSelected && preset.key == selectedPresetKey,
                onClick = { onPresetSelected(preset) },
                shape = cornerRadius,
            )
        }
    }
}

/**
 * 时间筛选条中的单个预设选项。
 *
 * @param key  用于标识该选项的唯一字符串（建议使用枚举名等稳定 key，便于状态比对与持久化）
 * @param label  chip 上展示的文本
 */
data class TimeFilterPreset(
    val key: String,
    val label: String,
)

/** 时间筛选条统一使用的圆角形状，16.dp 圆角矩形。 */
val TimeFilterBarShape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)

@Composable
private fun DateRangeChip(
    label: String,
    selected: Boolean,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        },
        shape = shape,
        contentPadding = TimeFilterBarContentPadding,
        style = GlassStyle.Regular,
        glassBorderWidth = 0.dp,
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun TimeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
) {
    val labelColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        },
        shape = shape,
        contentPadding = TimeFilterBarContentPadding,
        style = GlassStyle.Regular,
        glassBorderWidth = 0.dp,
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}

/**
 * 时间筛选条 chip 内部内容内边距：在 Material3 `FilterChipDefaults.ContentPadding`（12.dp 水平 / 8.dp 垂直）
 * 基础上，上下各加 2.dp，让 chip 内的文字/图标呼吸感更好。
 */
private val TimeFilterBarContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
