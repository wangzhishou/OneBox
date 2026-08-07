package com.wanbaohe.habittracker.screen.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutChartSlice
import com.t8rin.imagetoolbox.core.ui.widget.charts.DonutExpenseChart
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.week_selector.WeekSelector
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.component.HabitTrackerComponent
import com.wanbaohe.habittracker.model.HabitIcons
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.model.HabitWithStatus
import com.wanbaohe.habittracker.model.resolveHabitColor
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// 打卡 tab:周日历条 + 今日汇总卡 + 习惯列表
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun CheckInTab(
    component: HabitTrackerComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── 周筛选(chip 跳周 + 周一~周日,未来日期置灰) ──
        item {
            WeekSelector(
                selectedDate = LocalDate.ofEpochDay(uiState.selectedDateEpochDay),
                onDateSelected = { component.selectDate(it.toEpochDay()) },
                maxDate = LocalDate.now(),
            )
        }

        // ── 今日汇总卡 ─────────────────────────────────
        item {
            SummaryCard(
                doneCount = uiState.doneCount,
                dueCount = uiState.dueCount,
            )
        }

        // ── "今日打卡" 标题行 + 编辑切换 ─────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.habit_today_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(
                        if (uiState.isEditMode) R.string.habit_edit_done
                        else R.string.habit_edit_mode
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = component::toggleEditMode)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        // ── 习惯列表 ───────────────────────────────────
        if (uiState.habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.habit_empty_list),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(
                items = uiState.habits,
                key = { it.habit.id },
            ) { item ->
                HabitListItem(
                    item = item,
                    isEditMode = uiState.isEditMode,
                    isReadOnly = uiState.selectedDateEpochDay > LocalDate.now().toEpochDay(),
                    onToggle = { component.toggleCheckIn(item.habit.id) },
                    onDelete = { component.deleteHabit(item.habit.id) },
                    onEdit = { component.navigateToEditHabit(item.habit.id) },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 今日汇总卡(左圆环完成率 + 右侧三列统计)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    doneCount: Int,
    dueCount: Int,
) {
    val rate = if (dueCount > 0) doneCount.toFloat() / dueCount.toFloat() else 0f
    val percentText = "${(rate * 100).toInt()}%"
    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val slices = remember(doneCount, dueCount, ringColor, trackColor) {
        listOf(
            DonutChartSlice(value = doneCount.toFloat().coerceAtLeast(0f), color = ringColor),
            DonutChartSlice(
                value = (dueCount - doneCount).toFloat().coerceAtLeast(0f),
                color = trackColor,
            ),
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        containerAlpha = 0.92f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DonutExpenseChart(
                slices = slices,
                centerText = percentText,
                chartSize = 92.dp,
                strokeWidth = 12.dp,
            )
            Spacer(modifier = Modifier.width(18.dp))
            SummaryStatColumn(
                modifier = Modifier.weight(1f),
                value = "$doneCount / $dueCount",
                label = stringResource(R.string.habit_summary_rate),
            )
            SummaryStatColumn(
                modifier = Modifier.weight(1f),
                value = doneCount.toString(),
                label = stringResource(R.string.habit_summary_done),
            )
            SummaryStatColumn(
                modifier = Modifier.weight(1f),
                value = (dueCount - doneCount).toString(),
                label = stringResource(R.string.habit_summary_missed),
            )
        }
    }
}

@Composable
private fun SummaryStatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 习惯列表行(彩色图标 + 名称 + 频率,右侧勾选/删除)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HabitListItem(
    item: HabitWithStatus,
    isEditMode: Boolean,
    isReadOnly: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val habitColor = resolveHabitColor(item.habit.colorArgb, item.habit.id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isEditMode, onClick = onEdit)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 彩色圆角方块图标
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(habitColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HabitIcons.iconFor(item.habit.iconKey),
                contentDescription = null,
                tint = habitColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.habit.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 副标题:频率文案,备注非空时追加 " · 备注"(备注可能多行,先压成单行)
            val frequencyText = HabitRepeat.frequencySubtitle(item.habit)
            val noteText = item.habit.note?.replace('\n', ' ')?.trim().orEmpty()
            Text(
                text = if (noteText.isEmpty()) frequencyText else "$frequencyText · $noteText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isEditMode) {
            // 管理模式:行尾删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.habit_delete_habit),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            // 圆形勾选钮:已打卡 = 实心对勾
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (item.isChecked) habitColor else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (item.isChecked) habitColor
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    )
                    .clickable(enabled = !isReadOnly, onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                if (item.isChecked) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
