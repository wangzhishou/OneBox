package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TaskCompletionFilter
import com.shifenmiao.marktodo.model.TaskDateScope
import com.shifenmiao.marktodo.model.TaskFilter
import com.shifenmiao.marktodo.model.TaskSortConfig
import com.shifenmiao.marktodo.model.TaskSortMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSort
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import java.text.DateFormat
import java.util.Date

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
                    text = stringResource(R.string.p1_reset),
                    onClick = {
                        draftFilter = TaskFilter()
                        draftSort = TaskSortConfig()
                    },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(OneBoxDesignSystem.compactSpacing))
            }
            OnePrimaryButton(
                text = stringResource(R.string.p1_apply),
                onClick = { onApply(draftFilter, draftSort) },
                modifier = if (showFilters) Modifier.weight(2f) else Modifier.fillMaxWidth(),
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
                    title = stringResource(R.string.p1_filter_status),
                    icon = Icons.Outlined.LineCheckCircleOutline,
                    subtitle = stringResource(R.string.p1_filter_status_subtitle),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                        TaskCompletionFilter.entries.forEach { completion ->
                            GlassFilterChip(
                                selected = draftFilter.completion == completion,
                                onClick = { draftFilter = draftFilter.copy(completion = completion) },
                                label = { Text(taskCompletionLabel(completion)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_filter_category),
                    icon = Icons.Outlined.LineFolder,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    ) {
                        GlassFilterChip(
                            selected = draftFilter.includeInbox,
                            onClick = {
                                draftFilter = draftFilter.copy(includeInbox = !draftFilter.includeInbox)
                            },
                            label = { Text(stringResource(R.string.p1_inbox)) },
                        )
                        categories.forEach { category ->
                            val selected = category.id in draftFilter.categoryIds
                            GlassFilterChip(
                                selected = selected,
                                onClick = {
                                    draftFilter = draftFilter.copy(
                                        categoryIds = draftFilter.categoryIds.toggle(category.id),
                                    )
                                },
                                label = {
                                    Text(
                                        text = category.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }

                if (tags.isNotEmpty()) {
                    FilterSection(
                        title = stringResource(R.string.p1_filter_tags),
                        icon = Icons.Outlined.LineLabel,
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                        ) {
                            tags.forEach { tag ->
                                GlassFilterChip(
                                    selected = tag.name in draftFilter.tagNames,
                                    onClick = {
                                        draftFilter = draftFilter.copy(
                                            tagNames = draftFilter.tagNames.toggle(tag.name),
                                        )
                                    },
                                    label = { Text(tag.name) },
                                )
                            }
                        }
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_filter_priority),
                    icon = Icons.Outlined.LineFlag,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    ) {
                        listOf(
                            TodoTask.PRIORITY_HIGH to R.string.priority_high,
                            TodoTask.PRIORITY_MEDIUM to R.string.priority_medium,
                            TodoTask.PRIORITY_LOW to R.string.priority_low,
                        ).forEach { (priority, labelRes) ->
                            GlassFilterChip(
                                selected = priority in draftFilter.priorities,
                                onClick = {
                                    draftFilter = draftFilter.copy(
                                        priorities = draftFilter.priorities.toggle(priority),
                                    )
                                },
                                label = { Text(stringResource(labelRes)) },
                            )
                        }
                    }
                }

                FilterSection(
                    title = stringResource(R.string.p1_filter_due_range),
                    icon = Icons.Outlined.LineCalendar,
                    subtitle = stringResource(R.string.p1_filter_due_subtitle),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    ) {
                        listOf(
                            TaskDateScope.TODAY to R.string.p1_today,
                            TaskDateScope.OVERDUE to R.string.p1_group_overdue,
                            TaskDateScope.UPCOMING to R.string.p1_group_upcoming,
                            TaskDateScope.NO_DATE to R.string.p1_group_no_date,
                        ).forEach { (scope, labelRes) ->
                            val selected = draftFilter.dateScope == scope
                            GlassFilterChip(
                                selected = selected,
                                onClick = {
                                    draftFilter = draftFilter.copy(
                                        dateScope = if (selected) TaskDateScope.ALL else scope,
                                        dueStartMillis = null,
                                        dueEndMillis = null,
                                    )
                                },
                                label = { Text(stringResource(labelRes)) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                        DueRangeButton(
                            label = stringResource(R.string.p1_due_from),
                            value = draftFilter.dueStartMillis,
                            onPick = { datePickerTarget = DateRangeTarget.START },
                            onClear = { draftFilter = draftFilter.copy(dueStartMillis = null) },
                            modifier = Modifier.weight(1f),
                        )
                        DueRangeButton(
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
                    GlassFilterChip(
                        selected = draftFilter.starredOnly,
                        onClick = { draftFilter = draftFilter.copy(starredOnly = !draftFilter.starredOnly) },
                        label = { Text(stringResource(R.string.p1_starred_only)) },
                    )
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
            TaskSortMode.entries.chunked(2).forEach { modes ->
                Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                    modes.forEach { mode ->
                        GlassFilterChip(
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
                            label = { Text(taskSortModeLabel(mode)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (sort.mode != TaskSortMode.MANUAL) {
                Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                    GlassFilterChip(
                        selected = sort.ascending,
                        onClick = { onSortChange(sort.copy(ascending = true)) },
                        label = { Text(stringResource(R.string.p1_sort_ascending)) },
                        modifier = Modifier.weight(1f),
                    )
                    GlassFilterChip(
                        selected = !sort.ascending,
                        onClick = { onSortChange(sort.copy(ascending = false)) },
                        label = { Text(stringResource(R.string.p1_sort_descending)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DueRangeButton(
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
            modifier = Modifier.padding(bottom = 4.dp),
        )
        GlassCard(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = OneBoxDesignSystem.compactSpacing),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = value?.let(::formatFilterDate) ?: stringResource(R.string.p1_due_any),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                )
                if (value != null) {
                    IconButton(onClick = onClear, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.p1_clear_condition),
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
fun taskCompletionLabel(completion: TaskCompletionFilter): String = stringResource(
    when (completion) {
        TaskCompletionFilter.ALL -> R.string.filter_all
        TaskCompletionFilter.ACTIVE -> R.string.filter_active
        TaskCompletionFilter.COMPLETED -> R.string.filter_completed
    }
)

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

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value

private fun formatFilterDate(timestamp: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
