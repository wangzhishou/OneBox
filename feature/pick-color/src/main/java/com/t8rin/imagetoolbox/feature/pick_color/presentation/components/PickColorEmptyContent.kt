/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.AddToPaletteSheet
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.glass.glassMedium
import com.wanbaohe.core.ui.widget.system.OneBoxPillButton

/**
 * 取色引导页（还没选图时）。
 *
 * 两段就够：紧凑的选图引导 + 最近取到的大色块。
 * 色块点一下就是复制色值，不再需要「点开卡片再点复制」的第二下；
 * 想收藏 / 存进调色板 / 删掉，长按色块走和取色历史同一张操作单。
 */
@Composable
internal fun PickColorEmptyContent(
    recentColors: List<ColorPickHistoryEntry>,
    bottomInset: Dp,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onShowHistory: () -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var actionEntry by remember { mutableStateOf<ColorPickHistoryEntry?>(null) }
    var paletteTargetArgb by remember { mutableStateOf<Int?>(null) }

    val visibleRecentColors = remember(recentColors) { recentColors.take(6) }

    BaseScreen(
        modifier = modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.pick_color_title),
        onGoBack = onBack,
        isBackHandler = false,
        isShowDefaultActions = false,
        showNavigationBarsPadding = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 引导区压成一行：图标 + 标题/说明并排，比原来 150dp 的大头图省一半纵向空间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .glassMedium(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LineImage,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    )
                }
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.pick_color_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.pick_color_empty_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            // 两个按钮贴合自己的文字宽度，长语种最多各占一半
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OneBoxPillButton(
                    text = stringResource(R.string.pick_image_alt),
                    onClick = onPickImage,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 54.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    textStyle = MaterialTheme.typography.titleMedium,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
                OneBoxPillButton(
                    text = stringResource(R.string.take_photo),
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 54.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    textStyle = MaterialTheme.typography.titleMedium,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recently_picked),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (recentColors.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.show_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .hapticsClickable(onClick = onShowHistory)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            if (visibleRecentColors.isEmpty()) {
                Text(
                    text = stringResource(R.string.pick_color_recent_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            } else {
                visibleRecentColors.chunked(2).forEach { rowEntries ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowEntries.forEach { entry ->
                            val color = Color(entry.argb)
                            LargeColorBlock(
                                color = color,
                                onClick = {
                                    Clipboard.copy(
                                        text = color.toHex(),
                                        message = R.string.color_copied
                                    )
                                },
                                onLongClick = { actionEntry = entry },
                                primaryText = color.toHex(),
                                secondaryText = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 84.dp)
                            )
                        }
                        if (rowEntries.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    actionEntry?.let { entry ->
        PickColorHistoryActionsSheet(
            entry = entry,
            showUseColor = false,
            onDismiss = { actionEntry = null },
            onSelect = { actionEntry = null },
            onCopy = {
                actionEntry = null
                Clipboard.copy(
                    text = Color(entry.argb).toHex(),
                    message = R.string.color_copied
                )
            },
            onToggleFavorite = {
                actionEntry = null
                onToggleFavorite(entry.argb)
            },
            onAddToPalette = {
                actionEntry = null
                paletteTargetArgb = entry.argb
            },
            onDelete = {
                actionEntry = null
                onRemove(entry.argb)
            }
        )
    }

    paletteTargetArgb?.let { argb ->
        AddToPaletteSheet(
            color = Color(argb),
            visible = true,
            onDismiss = { paletteTargetArgb = null }
        )
    }
}
