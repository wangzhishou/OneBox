package com.wanbaohe.decisionwheel.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelRouterComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DecisionWheelHistoryScreen(
    router: DecisionWheelRouterComponent,
    bottomInset: Dp = 0.dp
) {
    val history by router.history.collectAsState()
    val wheels by router.wheels.collectAsState()
    var filterWheelId by remember { mutableStateOf<String?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val visible = remember(history, filterWheelId) {
        if (filterWheelId == null) history else history.filter { it.wheelId == filterWheelId }
    }

    val allLabel = stringResource(R.string.history_filter_all)

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.history),
        onGoBack = router::onTabBack,
        actions = {
            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.clear_history),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (history.isNotEmpty()) {
                // 转盘名长度不可控，横向滚动让筛选行能容纳任意多个 / 任意长名字的转盘
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = allLabel,
                        selected = filterWheelId == null,
                        onClick = { filterWheelId = null }
                    )
                    wheels.forEach { wheel ->
                        FilterChip(
                            label = wheel.title,
                            selected = filterWheelId == wheel.id,
                            onClick = { filterWheelId = wheel.id }
                        )
                    }
                }
            }

            if (visible.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible, key = { it.id }) { record ->
                        SwipeToDeleteHistoryRow(
                            record = record,
                            onDelete = { router.deleteHistory(record.id) }
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_history), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.clear_history_confirm)) },
            confirmButton = {
                GlassTonalButton(
                    onClick = {
                        router.clearHistory()
                        showClearConfirm = false
                    },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { showClearConfirm = false },
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * 单条历史：左滑删除。
 *
 * 只接受 EndToStart（左滑），反方向返回 false 让它弹回 —— 这样"误滑"不会误删。
 * 删除本身不弹确认框：滑动是一个需要主动用力的手势，再套一层确认是多余的一次点击。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteHistoryRow(
    record: WheelHistoryEntity,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }

                else -> false
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
        label = "history_dismiss_bg"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
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
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        HistoryRow(record = record)
    }
}

@Composable
private fun HistoryRow(record: WheelHistoryEntity) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.listRowShape,
        containerAlpha = OneBoxDesignSystem.sectionGlassStyle.backgroundAlpha,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .glassMedium(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.target_emoji),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.selectedOptionName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = buildString {
                        append(formatter.format(Date(record.timestamp)))
                        if (record.wheelTitle.isNotBlank()) {
                            append(" · ")
                            append(record.wheelTitle)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
