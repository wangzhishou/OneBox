package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.empty.EmptyStateGuide
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavoriteFilled
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.helper.toHex
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groups.forEach { group ->
                        item(key = "header-${group.title}") {
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
                            PickColorHistoryRow(
                                entry = entry,
                                isCurrent = entry.argb == currentArgb,
                                time = time,
                                onClick = { onSelect(entry.argb) },
                                onCopy = {
                                    Clipboard.copy(
                                        text = Color(entry.argb).toHex(),
                                        message = R.string.color_copied
                                    )
                                },
                                onToggleFavorite = { onToggleFavorite(entry.argb) },
                                onDelete = { onRemove(entry.argb) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickColorHistoryRow(
    entry: ColorPickHistoryEntry,
    isCurrent: Boolean,
    time: String,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "color_history_dismiss_bg"
    )
    val color = Color(entry.argb)
    val rgb = color.toRgbTriplet()

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // 行本身是半透明玻璃，删除图标只在真正左滑时绘制；
            // 否则它会一直透在行后面，和收藏爱心叠成一团。
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(OneBoxDesignSystem.listRowShape)
                        .background(backgroundColor)
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete_history_item),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) {
        OneBoxListItem(
            contained = true,
            selected = isCurrent,
            onClick = onClick,
            leadingContent = {
                PickColorSwatch(
                    argb = entry.argb,
                    size = 48.dp,
                    shape = RoundedCornerShape(14.dp),
                    showCheck = isCurrent
                )
            },
            headlineContent = {
                Text(
                    text = color.toHex(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    fontWeight = FontWeight.Bold
                )
            },
            subtitle = {
                Text(
                    text = stringResource(
                        R.string.pick_color_rgb_value,
                        rgb.red,
                        rgb.green,
                        rgb.blue
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall.copy(
                            textDirection = TextDirection.Ltr
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EnhancedIconButton(
                            onClick = onCopy,
                            forceMinimumInteractiveComponentSize = false,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = stringResource(R.string.copy),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        EnhancedIconButton(
                            onClick = onToggleFavorite,
                            forceMinimumInteractiveComponentSize = false,
                            containerColor = Color.Transparent,
                            contentColor = if (entry.isFavorite) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (entry.isFavorite) {
                                    Icons.Outlined.LineFavoriteFilled
                                } else {
                                    Icons.Outlined.LineFavorite
                                },
                                contentDescription = stringResource(
                                    if (entry.isFavorite) {
                                        R.string.pick_color_unfavorite
                                    } else {
                                        R.string.favorite
                                    }
                                ),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}
