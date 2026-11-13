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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavoriteFilled
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.AddToPaletteSheet
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import com.wanbaohe.core.ui.widget.color_picker.contentColor
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

/**
 * 取色历史：按日期分组的大色块网格。
 *
 * 与色彩工具里的色块保持一致——一眼看到颜色本身，色值压在色块上；
 * 点色块直接复制色值，左上方星星切收藏，长按弹出「选用 / 复制 / 收藏 / 删除」。
 */
@Composable
internal fun PickColorHistoryContent(
    history: List<ColorPickHistoryEntry>,
    currentArgb: Int?,
    bottomInset: Dp,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var actionEntry by remember { mutableStateOf<ColorPickHistoryEntry?>(null) }
    var paletteTargetArgb by remember { mutableStateOf<Int?>(null) }

    val todayLabel = stringResource(R.string.today)
    val yesterdayLabel = stringResource(R.string.yesterday)
    val dateFormatter = remember { historyDateFormatter() }
    val timeFormatter = remember { historyTimeFormatter() }

    val visibleHistory = remember(history, favoritesOnly) {
        if (favoritesOnly) history.filter { it.isFavorite } else history
    }
    val groups = remember(visibleHistory, todayLabel, yesterdayLabel) {
        groupColorPickHistory(
            history = visibleHistory,
            todayLabel = todayLabel,
            yesterdayLabel = yesterdayLabel,
            dateFormatter = dateFormatter
        )
    }

    BaseScreen(
        modifier = modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.color_history),
        onGoBack = onBack,
        isBackHandler = false,
        showNavigationBarsPadding = false,
        actions = {
            if (history.isNotEmpty()) {
                EnhancedIconButton(
                    onClick = { showClearDialog = true },
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.clear_color_history)
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedChip(
                        selected = !favoritesOnly,
                        onClick = { favoritesOnly = false },
                        selectedColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(stringResource(R.string.all))
                    }
                    EnhancedChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = true },
                        selectedColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(stringResource(R.string.favorite))
                    }
                }
            }

            if (groups.isEmpty()) {
                EmptyHistory(
                    favoritesOnly = favoritesOnly && history.isNotEmpty()
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groups.forEach { group ->
                        item(
                            key = "header-${group.title}",
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    top = 8.dp,
                                    bottom = 2.dp
                                )
                            )
                        }
                        items(
                            items = group.entries,
                            key = { it.argb }
                        ) { entry ->
                            val time = remember(entry.pickedAtMillis, timeFormatter) {
                                localTimeOf(entry.pickedAtMillis).format(timeFormatter)
                            }
                            PickColorHistoryBlock(
                                entry = entry,
                                isCurrent = entry.argb == currentArgb,
                                time = time,
                                onClick = {
                                    Clipboard.copy(
                                        text = Color(entry.argb).toHex(),
                                        message = R.string.color_copied
                                    )
                                },
                                onLongClick = { actionEntry = entry },
                                onToggleFavorite = { onToggleFavorite(entry.argb) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_color_history)) },
            text = { Text(stringResource(R.string.clear_color_history_confirm)) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LineDeleteForever,
                    contentDescription = null
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.delete),
                    onClick = {
                        showClearDialog = false
                        onClear()
                    }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showClearDialog = false }
                )
            }
        )
    }

    actionEntry?.let { entry ->
        PickColorHistoryActionsSheet(
            entry = entry,
            onDismiss = { actionEntry = null },
            onSelect = {
                actionEntry = null
                onSelect(entry.argb)
            },
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

@Composable
private fun PickColorHistoryBlock(
    entry: ColorPickHistoryEntry,
    isCurrent: Boolean,
    time: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val color = Color(entry.argb)
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .then(
                if (isCurrent) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
    ) {
        LargeColorBlock(
            color = color,
            onClick = onClick,
            onLongClick = onLongClick,
            shape = shape,
            primaryText = color.toHex(),
            secondaryText = time,
            textStyle = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp),
            badge = {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 1f))
                        .hapticsClickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (entry.isFavorite) {
                            Icons.Outlined.LineFavoriteFilled
                        } else {
                            Icons.Outlined.LineFavorite
                        },
                        contentDescription = stringResource(
                            if (entry.isFavorite) R.string.pick_color_unfavorite
                            else R.string.favorite
                        ),
                        modifier = Modifier.size(16.dp),
                        tint = color.contentColor()
                    )
                }
            }
        )
    }
}

@Composable
internal fun PickColorHistoryActionsSheet(
    entry: ColorPickHistoryEntry,
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPalette: () -> Unit,
    onDelete: () -> Unit,
    showUseColor: Boolean = true,
) {
    val color = Color(entry.argb)

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = color.toHex()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            if (showUseColor) {
                HistoryActionRow(
                    icon = Icons.Outlined.LineCheckCircleOutline,
                    text = stringResource(R.string.use_this_color),
                    onClick = onSelect
                )
            }
            HistoryActionRow(
                icon = Icons.Rounded.ContentCopy,
                text = stringResource(R.string.copy),
                onClick = onCopy
            )
            HistoryActionRow(
                icon = if (entry.isFavorite) {
                    Icons.Outlined.LineFavoriteFilled
                } else {
                    Icons.Outlined.LineFavorite
                },
                text = stringResource(
                    if (entry.isFavorite) R.string.pick_color_unfavorite else R.string.favorite
                ),
                onClick = onToggleFavorite
            )
            HistoryActionRow(
                icon = Icons.Outlined.LinePaletteTools,
                text = stringResource(R.string.add_to_palette),
                onClick = onAddToPalette
            )
            HistoryActionRow(
                icon = Icons.Outlined.Delete,
                text = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.onErrorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun HistoryActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    OneBoxListItem(
        headlineContent = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        },
        onClick = onClick,
        shape = ShapeDefaults.default
    )
}

@Composable
private fun EmptyHistory(
    favoritesOnly: Boolean
) {
    EmptyStateGuide(
        icon = if (favoritesOnly) {
            Icons.Outlined.LineFavoriteFilled
        } else {
            Icons.Outlined.LineHistory
        },
        title = stringResource(
            if (favoritesOnly) R.string.no_favorite_colors
            else R.string.no_color_history
        ),
        description = stringResource(
            if (favoritesOnly) R.string.no_favorite_colors_desc
            else R.string.no_color_history_desc
        ),
        actions = emptyList()
    )
}
