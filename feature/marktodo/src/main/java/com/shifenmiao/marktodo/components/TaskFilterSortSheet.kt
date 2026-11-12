/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TaskDateScope
import com.shifenmiao.marktodo.model.TaskFilter
import com.shifenmiao.marktodo.model.TaskSortConfig
import com.shifenmiao.marktodo.model.TaskSortMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.priorityColor
import com.shifenmiao.marktodo.theme.tagAccentColor
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSort
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.system.SelectorField
import java.text.DateFormat
import java.util.Date

/**
 * 筛选与排序浮层。
 *
 * 交互口径（与「快速添加」面板保持一致）：
 * - 分组是单选下拉选择器（分组可能有几十个，不再铺一排 chip）
 * - 其它条件是 chip：默认浮起一层实底，选中换成主色容器
 * - chip / 按钮一律按内容撑开，靠 FlowRow 换行，不拉等宽、不锁死高度
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskFilterSortSheet(
    visible: Boolean,
    categories: List<TodoCategory>,
    tags: List<TodoTag>,
    initialFilter: TaskFilter,
    initialSort: TaskSortConfig,
    onDismiss: () -> Unit,
    onApply: (TaskFilter, TaskSortConfig) -> Unit,
    showFilters: Boolean = true,
) {
    var draftFilter by remember(visible, initialFilter) { mutableStateOf(initialFilter) }
    var draftSort by remember(visible, initialSort) { mutableStateOf(initialSort) }
    var datePickerTarget by remember { mutableStateOf<DateRangeTarget?>(null) }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = stringResource(
            if (showFilters) R.string.p1_filter_sort_title else R.string.p1_sort_by
        ),
        enableBackHandler = true,
        confirmBarModifier = Modifier.imePadding(),
        confirmButton = {
            if (showFilters) {
                OneSecondaryButton(
                    text = stringResource(R.string.p1_clear_filters),
                    onClick = { draftFilter = TaskFilter() },
                    enabled = draftFilter != TaskFilter(),
                )
                Spacer(modifier = Modifier.width(OneBoxDesignSystem.compactSpacing))
            }
            OnePrimaryButton(
                text = stringResource(R.string.p1_apply),
                onClick = { onApply(draftFilter, draftSort) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding)
                .padding(bottom = OneBoxDesignSystem.blockSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            if (showFilters) {
                FilterSection(
                    title = stringResource(R.string.p1_filter_category),
                    icon = Icons.Outlined.LineFolder,
                ) {
                    GroupFilterSelector(
                        categories = categories,
                        filter = draftFilter,
                        onFilterChange = { draftFilter = it },
                    )
                }

                if (tags.isNotEmpty()) {
                    FilterSection(
                        title = stringResource(R.string.p1_filter_tags),
                        icon = Icons.Outlined.LineLabel,
                    ) {
                        FilterChipFlow {
                            tags.forEachIndexed { index, tag ->
                                FilterOptionChip(
                                    selected = tag.name in draftFilter.tagNames,
                                    onClick = {
                                        draftFilter = draftFilter.copy(
                                            tagNames = draftFilter.tagNames.toggle(tag.name),
                                        )
                                    },
                                    label = tag.name,
                                    accentColor = tagAccentColor(tag.colorArgb, index),
                                )
                            }
                        }
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_filter_priority),
                    icon = Icons.Outlined.LineFlag,
                ) {
                    FilterChipFlow {
                        listOf(
                            TodoTask.PRIORITY_HIGH to R.string.priority_high,
                            TodoTask.PRIORITY_MEDIUM to R.string.priority_medium,
                            TodoTask.PRIORITY_LOW to R.string.priority_low,
                        ).forEach { (priority, labelRes) ->
                            val selected = priority in draftFilter.priorities
                            FilterOptionChip(
                                selected = selected,
                                onClick = {
                                    draftFilter = draftFilter.copy(
                                        priorities = draftFilter.priorities.toggle(priority),
                                    )
                                },
                                label = stringResource(labelRes),
                                accentColor = priorityColor(priority),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.LineFlag,
                                        contentDescription = null,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            priorityColor(priority)
                                        },
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                            )
                        }
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_filter_due_range),
                    icon = Icons.Outlined.LineCalendar,
                    subtitle = stringResource(R.string.p1_filter_due_subtitle),
                ) {
                    FilterChipFlow {
                        listOf(
                            TaskDateScope.TODAY to R.string.p1_today,
                            TaskDateScope.OVERDUE to R.string.p1_group_overdue,
                            TaskDateScope.UPCOMING to R.string.p1_group_upcoming,
                            TaskDateScope.NO_DATE to R.string.p1_group_no_date,
                        ).forEach { (scope, labelRes) ->
                            val selected = draftFilter.dateScope == scope
                            FilterOptionChip(
                                selected = selected,
                                onClick = {
                                    draftFilter = draftFilter.copy(
                                        dateScope = if (selected) TaskDateScope.ALL else scope,
                                        dueStartMillis = null,
                                        dueEndMillis = null,
                                    )
                                },
                                label = stringResource(labelRes),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = OneBoxDesignSystem.compactSpacing),
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    ) {
                        DueRangeField(
                            label = stringResource(R.string.p1_due_from),
                            value = draftFilter.dueStartMillis,
                            onPick = { datePickerTarget = DateRangeTarget.START },
                            onClear = { draftFilter = draftFilter.copy(dueStartMillis = null) },
                            modifier = Modifier.weight(1f),
                        )
                        DueRangeField(
                            label = stringResource(R.string.p1_due_until),
                            value = draftFilter.dueEndMillis,
                            onPick = { datePickerTarget = DateRangeTarget.END },
                            onClear = { draftFilter = draftFilter.copy(dueEndMillis = null) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_focus),
                    icon = Icons.Outlined.LineStar,
                ) {
                    FilterChipFlow {
                        FilterOptionChip(
                            selected = draftFilter.starredOnly,
                            onClick = {
                                draftFilter = draftFilter.copy(starredOnly = !draftFilter.starredOnly)
                            },
                            label = stringResource(R.string.p1_starred_only),
                        )
                    }
                }
            }

            SortSection(
                sort = draftSort,
                onSortChange = { draftSort = it },
            )
        }
    }

    datePickerTarget?.let { target ->
        val initialDate = when (target) {
            DateRangeTarget.START -> draftFilter.dueStartMillis
            DateRangeTarget.END -> draftFilter.dueEndMillis
        }
        DueRangeDatePickerDialog(
            initialDate = initialDate,
            onDismiss = { datePickerTarget = null },
            onConfirm = { selected ->
                draftFilter = when (target) {
                    DateRangeTarget.START -> {
                        val adjustedEnd = draftFilter.dueEndMillis?.takeIf { it >= selected } ?: selected
                        draftFilter.copy(dueStartMillis = selected, dueEndMillis = adjustedEnd)
                    }
                    DateRangeTarget.END -> {
                        val adjustedStart = draftFilter.dueStartMillis?.takeIf { it <= selected } ?: selected
                        draftFilter.copy(dueStartMillis = adjustedStart, dueEndMillis = selected)
                    }
                }.copy(dateScope = TaskDateScope.RANGE)
                datePickerTarget = null
            },
        )
    }
}

/**
 * 分组筛选项。分组是单选：全部 / 收集箱 / 某个分组。
 *
 * 旧版是「收集箱 + 每个分组」一排 chip，分组一多就铺满半屏；这里收成和
 * 「快速添加 → 所属分组」同一种下拉选择器。
 */
@Composable
private fun GroupFilterSelector(
    categories: List<TodoCategory>,
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
) {
    val allLabel = stringResource(R.string.filter_all)
    val inboxLabel = stringResource(R.string.p1_inbox)
    val options = remember(categories, allLabel, inboxLabel) {
        buildList {
            add(GroupFilterOption(id = null, label = allLabel))
            add(GroupFilterOption(id = TodoCategory.INBOX_ID, label = inboxLabel))
            categories.forEach { add(GroupFilterOption(id = it.id, label = it.title)) }
        }
    }
    val selectedId = when {
        filter.includeInbox -> TodoCategory.INBOX_ID
        filter.categoryIds.size == 1 -> filter.categoryIds.first()
        else -> null
    }
    val selected = options.firstOrNull { it.id == selectedId } ?: options.first()

    SelectorField(
        selected = selected,
        options = options,
        optionLabel = { it.label },
        onSelect = { option ->
            onFilterChange(
                when (option.id) {
                    null -> filter.copy(categoryIds = emptySet(), includeInbox = false)
                    TodoCategory.INBOX_ID ->
                        filter.copy(categoryIds = emptySet(), includeInbox = true)
                    else -> filter.copy(categoryIds = setOf(option.id), includeInbox = false)
                }
            )
        },
    )
}

@Immutable
private data class GroupFilterOption(
    /** null = 全部分组，[TodoCategory.INBOX_ID] = 收集箱，其余是真实分组 id */
    val id: String?,
    val label: String,
)

@Composable
private fun FilterSection(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    GlassCard(
        shape = RoundedCornerShape(OneBoxDesignSystem.mediumRadius),
        style = GlassStyle.Regular,
        colors = tintedGlassCardColors(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            content()
        }
    }
}

/**
 * 条件 chip：默认是浮起来的一层实底，选中切到主色容器。
 *
 * 尺寸交给内容（M3 chip 自身 32dp 高、宽由 label 决定），外层 FlowRow 负责换行，
 * 不再用 `weight(1f)` 把 chip 拉成等宽格子。
 */
@Composable
private fun FilterOptionChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.widthIn(max = 240.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = scheme.surfaceContainerHighest,
            labelColor = scheme.onSurface,
            iconColor = scheme.onSurfaceVariant,
            selectedContainerColor = scheme.primaryContainer,
            selectedLabelColor = scheme.onPrimaryContainer,
            selectedLeadingIconColor = scheme.onPrimaryContainer,
        ),
        style = GlassStyle.None,
        glassBorderWidth = 0.dp,
        glassContainerColor = scheme.surfaceContainerHighest,
        glassSelectedContainerColor = scheme.primaryContainer,
        contentPadding = FilterChipContentPadding,
        leadingIcon = leadingIcon,
        label = {
            Text(
                text = label,
                color = if (selected) {
                    scheme.onPrimaryContainer
                } else {
                    accentColor ?: Color.Unspecified
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipFlow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        content = content,
    )
}

@Composable
private fun SortSection(
    sort: TaskSortConfig,
    onSortChange: (TaskSortConfig) -> Unit,
) {
    FilterSection(
        title = stringResource(R.string.p1_sort_by),
        icon = Icons.Outlined.LineSort,
        subtitle = stringResource(R.string.p1_sort_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            FilterChipFlow {
                TaskSortMode.entries.forEach { mode ->
                    FilterOptionChip(
                        selected = sort.mode == mode,
                        onClick = {
                            onSortChange(
                                TaskSortConfig(
                                    mode = mode,
                                    ascending = when (mode) {
                                        TaskSortMode.MANUAL, TaskSortMode.DUE_DATE -> true
                                        TaskSortMode.PRIORITY, TaskSortMode.CREATED_AT -> false
                                    },
                                )
                            )
                        },
                        label = taskSortModeLabel(mode),
                    )
                }
            }
            if (sort.mode != TaskSortMode.MANUAL) {
                FilterChipFlow {
                    FilterOptionChip(
                        selected = sort.ascending,
                        onClick = { onSortChange(sort.copy(ascending = true)) },
                        label = stringResource(R.string.p1_sort_ascending),
                    )
                    FilterOptionChip(
                        selected = !sort.ascending,
                        onClick = { onSortChange(sort.copy(ascending = false)) },
                        label = stringResource(R.string.p1_sort_descending),
                    )
                }
            }
        }
    }
}

/**
 * 自定义截止日期的一端。视觉上是「输入框」而不是 chip：高度固定 48dp、等宽两列，
 * 和上面的快捷日期 chip 区分开。
 */
@Composable
private fun DueRangeField(
    label: String,
    value: Long?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = OneBoxDesignSystem.microSpacing),
        )
        GlassCard(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.None,
            colors = tintedGlassCardColors(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .padding(start = OneBoxDesignSystem.itemSpacing),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = value?.let(::formatFilterDate) ?: stringResource(R.string.p1_due_any),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (value != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                )
                IconButton(
                    onClick = onClear,
                    enabled = value != null,
                    modifier = Modifier.size(40.dp),
                ) {
                    if (value != null) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.p1_clear_condition),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DueRangeDatePickerDialog(
    initialDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    com.wanbaohe.base.ui.picker.ChineseDatePickerDialog(
        initialDateMillis = initialDate ?: System.currentTimeMillis(),
        onDismiss = onDismiss,
        onDateSelected = onConfirm,
        placeAboveAll = true,
    )
}

@Composable
fun taskSortModeLabel(mode: TaskSortMode): String = stringResource(
    when (mode) {
        TaskSortMode.MANUAL -> R.string.p1_sort_manual
        TaskSortMode.DUE_DATE -> R.string.p1_sort_due_date
        TaskSortMode.PRIORITY -> R.string.p1_sort_priority
        TaskSortMode.CREATED_AT -> R.string.p1_sort_created_at
    }
)

@Composable
fun taskSortDirectionLabel(ascending: Boolean): String = stringResource(
    if (ascending) R.string.p1_sort_ascending else R.string.p1_sort_descending
)

private enum class DateRangeTarget {
    START, END
}

private val FilterChipContentPadding = PaddingValues(horizontal = 8.dp)

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value

private fun formatFilterDate(timestamp: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
