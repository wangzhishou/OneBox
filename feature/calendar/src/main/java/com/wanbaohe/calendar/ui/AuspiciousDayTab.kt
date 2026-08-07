package com.wanbaohe.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.data.AuspiciousDayResult
import com.wanbaohe.calendar.data.YiJiCalculator
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline

/**
 * 择日 Tab
 *
 * 支持两种模式：吉日择取 / 忌事避讳
 * 所有事项以两行横向滚动展示，支持多选筛选。
 */
@Composable
fun AuspiciousDayTab(
    state: CalendarUiState,
    onToggleItem: (String) -> Unit,
    onToggleMode: () -> Unit,
    onDayClick: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvoid = state.isAvoidMode
    val items = remember(isAvoid) {
        val src = if (isAvoid) YiJiCalculator.JI_ITEMS else YiJiCalculator.YI_ITEMS
        src.toList().distinct()
    }
    // 拆成两行
    val mid = (items.size + 1) / 2
    val row1 = items.subList(0, mid)
    val row2 = items.subList(mid, items.size)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── 顶部引导卡片（含开关） ──
        item {
            Spacer(modifier = Modifier.height(4.dp))
            HeaderCard(
                isAvoidMode = isAvoid,
                onToggleMode = onToggleMode,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 选择事项标题 ──
        item {
            Text(
                text = if (isAvoid) stringResource(R.string.select_ji_items) else stringResource(R.string.select_yi_items),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 12.dp, bottom = 10.dp)
            )
        }

        // ── 两行横向同步滚动事项 ──
        item {
            val scrollState = rememberScrollState()
            Column {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row1.forEach { item ->
                        val isSelected = item in state.selectedAuspiciousItems
                        ItemChip(
                            text = item,
                            isSelected = isSelected,
                            isAvoidMode = isAvoid,
                            onClick = { onToggleItem(item) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row2.forEach { item ->
                        val isSelected = item in state.selectedAuspiciousItems
                        ItemChip(
                            text = item,
                            isSelected = isSelected,
                            isAvoidMode = isAvoid,
                            onClick = { onToggleItem(item) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── 未选择时的引导占位 ──
        if (state.selectedAuspiciousItems.isEmpty()) {
            item {
                EmptyGuide(isAvoidMode = isAvoid)
            }
        }

        // ── 搜索结果标题 ──
        item {
            AnimatedVisibility(
                visible = state.selectedAuspiciousItems.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                        contentDescription = null,
                        tint = if (isAvoid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val modeLabel = if (isAvoid) {
                        stringResource(R.string.inauspicious_days_count, state.auspiciousDayResults.size)
                    } else {
                        stringResource(R.string.auspicious_days_count, state.auspiciousDayResults.size)
                    }
                    Text(
                        text = if (state.isAuspiciousLoading) stringResource(R.string.searching)
                        else modeLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Loading indicator
        if (state.isAuspiciousLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = if (isAvoid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        // Empty result after search
        if (!state.isAuspiciousLoading && state.selectedAuspiciousItems.isNotEmpty() && state.auspiciousDayResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAvoid) stringResource(R.string.no_inauspicious_days)
                        else stringResource(R.string.no_auspicious_days),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Result cards
        if (!state.isAuspiciousLoading) {
            items(
                items = state.auspiciousDayResults,
                key = { "${it.solarYear}-${it.solarMonth}-${it.solarDay}" }
            ) { result ->
                AuspiciousDayCard(
                    result = result,
                    isAvoidMode = isAvoid,
                    onClick = { onDayClick(result.solarYear, result.solarMonth, result.solarDay) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── 顶部引导卡片（含模式开关） ──────────────────────────────

@Composable
private fun HeaderCard(
    isAvoidMode: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isAvoidMode)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isAvoidMode)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAvoidMode) stringResource(R.string.avoid_mode_title) else stringResource(R.string.lucky_mode_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isAvoidMode) stringResource(R.string.avoid_mode_desc)
                    else stringResource(R.string.lucky_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            GlassSwitch(
                checked = isAvoidMode,
                onCheckedChange = { onToggleMode() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.error,
                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    }
}

// ── 事项 Chip ──────────────────────────────────────────

@Composable
private fun ItemChip(
    text: String,
    isSelected: Boolean,
    isAvoidMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected && isAvoidMode -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .glassBackground(
                shape = RoundedCornerShape(12.dp),
                color = bgColor
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
            maxLines = 1
        )
    }
}

// ── 未选择时的引导占位 ──────────────────────────────────────

@Composable
private fun EmptyGuide(isAvoidMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTouchApp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isAvoidMode) stringResource(R.string.empty_guide_avoid) else stringResource(R.string.empty_guide_yi),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isAvoidMode) stringResource(R.string.empty_guide_avoid_hint) else stringResource(R.string.empty_guide_yi_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

// ── 搜索结果卡片 ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuspiciousDayCard(
    result: AuspiciousDayResult,
    isAvoidMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部：日期 + 农历
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.month_day_format, result.solarMonth, result.solarDay),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.weekDay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                // 干支日 badge
                Box(
                    modifier = Modifier
                        .glassBackground(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.ganZhiDay,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 农历信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${result.lunarMonthName}${result.lunarDayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result.jianChu.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .glassBackground(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${result.jianChu}日",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 宜事项
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    result.allYi.forEach { item ->
                        val isMatched = !isAvoidMode && item in result.matchedItems
                        Box(
                            modifier = Modifier
                                .glassBackground(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isMatched) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isMatched) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 忌事项（如有）
            if (result.allJi.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.allJi.forEach { item ->
                            val isMatched = isAvoidMode && item in result.matchedItems
                            Box(
                                modifier = Modifier
                                    .glassBackground(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isMatched) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isMatched) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 底部：查看日历提示
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.view_calendar),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
