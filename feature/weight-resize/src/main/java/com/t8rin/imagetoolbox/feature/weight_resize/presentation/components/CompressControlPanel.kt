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
 */

package com.t8rin.imagetoolbox.feature.weight_resize.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ImageUtils.restrict
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.RoundedTextField

/**
 * 压缩控制面板 —— 预设百分比芯片 + 手动 KB 输入并排展示。
 *
 * 不再需要 "手动/预设" 切换按钮，两种控件同时可见：
 * - **左侧**：可横向滚动的百分比预设芯片（来自设置中保存的预设列表）
 * - **右侧**：直接输入目标 KB 上限的文本框
 *
 * 点击芯片 → 触发 [onPresetChange]（组件内部切换为 preset 模式）
 * 输入 KB → 触发 [onMaxBytesChange]（组件内部切换为 hand 模式）
 *
 * @param enabled       控件是否可交互（图片未加载时禁用）
 * @param maxBytes      当前目标字节上限（来自组件状态）
 * @param presetSelected 当前选中的预设百分比值，0 表示未选中
 * @param onMaxBytesChange 手动输入 KB 时回调，参数为原始字符串（组件内 restrict）
 * @param onPresetChange   点击预设芯片时回调
 */
@Composable
fun CompressControlPanel(
    enabled: Boolean,
    maxBytes: Long,
    presetSelected: Int,
    onMaxBytesChange: (String) -> Unit,
    onPresetChange: (Preset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsState = LocalSettingsState.current

    // 如果当前预设值不在保存的预设列表中，则追加到列表头部
    val presets by remember(settingsState.presets, presetSelected) {
        derivedStateOf {
            settingsState.presets.let { list ->
                if (presetSelected > 0 && presetSelected !in list) {
                    listOf(presetSelected) + list
                } else {
                    list
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── 左侧：预设百分比芯片 ────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .flatGlassContainer(shape = MaterialTheme.shapes.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.presets),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(4.dp))
            val listState = rememberLazyListState()
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fadingEdges(listState),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(presets, key = { it }) { pct ->
                    val isSelected = presetSelected == pct
                    EnhancedChip(
                        selected = isSelected,
                        onClick = { onPresetChange(Preset.Percentage(pct)) },
                        selectedColor = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        AutoSizeText(pct.toString())
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        // ── 右侧：手动 KB 输入框 ────────────────────
        RoundedTextField(
            modifier = Modifier.width(110.dp),
            enabled = enabled,
            value = (maxBytes / 1024).toString().takeIf { it != "0" } ?: "",
            onValueChange = { onMaxBytesChange(it.restrict(1_000_000)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = stringResource(R.string.max_bytes),
        )
    }
}

