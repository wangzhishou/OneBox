package com.wanbaohe.bookkeeping.screen.tab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.ChineseDateRangePickerDialog
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTimeFilterBar
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import com.wanbaohe.bookkeeping.model.BookkeepingRecentPreset
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import com.wanbaohe.bookkeeping.model.BookkeepingTimeFilter
import com.wanbaohe.bookkeeping.model.dateRange
import com.wanbaohe.bookkeeping.model.displayLabel
import com.wanbaohe.bookkeeping.model.rememberBookkeepingTimePresets
import com.wanbaohe.bookkeeping.model.selectedPresetKey
import com.wanbaohe.bookkeeping.screen.component.PrimaryTypeChip
import com.wanbaohe.bookkeeping.screen.sheet.CategoryPickerSheet
import com.wanbaohe.bookkeeping.screen.util.centsText
import com.wanbaohe.bookkeeping.screen.util.recordLineSubtitle
import com.wanbaohe.bookkeeping.screen.util.signedAmount
import java.time.LocalDate
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit

// ─────────────────────────────────────────────────────────────────────────────
// 分类筛选状态
// ─────────────────────────────────────────────────────────────────────────────

private sealed class DetailFilter {
    data object All : DetailFilter()
    data class ByCategory(val id: String, val name: String) : DetailFilter()
}

private data class DetailSummaryUi(
    val expenseCents: Long = 0L,
    val incomeCents: Long = 0L,
)

private data class DaySectionCardUi(
    val date: LocalDate,
    val items: List<DetailSectionUi>,
)

private sealed interface DetailSectionUi {
    data class SummaryChip(
        val expenseCents: Long,
        val incomeCents: Long,
    ) : DetailSectionUi

    data class RecordItem(
        val record: BookkeepingRecordUi,
    ) : DetailSectionUi
}

// ─────────────────────────────────────────────────────────────────────────────
// 明细 Tab 入口
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun DetailTab(
    component: BookkeepingComponent,
    modifier: Modifier = Modifier,
) {
    val allCategories by component.allCategoriesFlow.collectAsState()
    val allRecords by component.allRecordsFlow.collectAsState()
    val uiState by component.uiState.collectAsState()
    val timeFilter = uiState.timeFilter

    var detailFilter by remember { mutableStateOf<DetailFilter>(DetailFilter.All) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var pendingDeleteRecordId by remember { mutableStateOf<String?>(null) }

    val rangeRecords = remember(allRecords, timeFilter) {
        allRecords.filterBy(timeFilter)
    }

    val filteredRecords = remember(rangeRecords, detailFilter) {
        when (val f = detailFilter) {
            is DetailFilter.All        -> rangeRecords
            is DetailFilter.ByCategory -> rangeRecords.filter { it.categoryId == f.id }
        }
    }

    val daySections = remember(filteredRecords) {
        filteredRecords.toDaySectionCards()
    }

    val chipLabel = when (val f = detailFilter) {
        is DetailFilter.All        -> stringResource(R.string.bookkeeping_filter_all)
        is DetailFilter.ByCategory -> f.name
    }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            DetailTopHeader(
                timeFilter = timeFilter,
                chipLabel = chipLabel,
                onChipClick = { showCategoryPicker = true },
                onCustomRangeClick = { showDateRangePicker = true },
                onTimeFilterChange = component::setTimeFilter,
            )
        }

        if (daySections.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.bookkeeping_no_records))
                }
            }
        } else {
            items(
                items = daySections,
                key = { section -> section.date.toString() },
            ) { section ->
                DaySectionCard(
                    section = section,
                    onDeleteRecord = { recordId -> pendingDeleteRecordId = recordId },
                    onEditRecord = { recordId ->
                        component.onNavigate(
                            com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Bookkeeping(
                                com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Bookkeeping.Type.AddRecord(
                                    editingRecordId = recordId
                                )
                            )
                        )
                    },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    // 删除确认弹窗
    pendingDeleteRecordId?.let { recordId ->
        EnhancedAlertDialog(
            visible = true,
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            enableGlass = true,
            onDismissRequest = { pendingDeleteRecordId = null },
            title = { Text(text = stringResource(R.string.bookkeeping_delete_confirm_title)) },
            text = { Text(text = stringResource(R.string.bookkeeping_delete_confirm_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        component.removeRecord(recordId)
                        pendingDeleteRecordId = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.bookkeeping_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingDeleteRecordId = null }) {
                    Text(text = stringResource(R.string.bookkeeping_cancel))
                }
            },
        )
    }

    // 分类筛选表单
    if (showCategoryPicker) {
        CategoryPickerSheet(
            title = stringResource(R.string.bookkeeping_filter_title),
            grouped = allCategories,
            onSelectAll = {
                detailFilter = DetailFilter.All
                showCategoryPicker = false
            },
            onSelectCategory = { id, name ->
                detailFilter = DetailFilter.ByCategory(id = id, name = name)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false },
        )
    }

    if (showDateRangePicker) {
        val currentRange = timeFilter.dateRange()
        ChineseDateRangePickerDialog(
            initialStartDate = currentRange.startDate,
            initialEndDate = currentRange.endDate,
            maxDate = LocalDate.now(),
            onDateRangeSelected = { start, end ->
                component.setTimeFilter(BookkeepingTimeFilter.Custom(startDate = start, endDate = end))
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false },
        )
    }
}

@Composable
private fun DaySectionCard(
    section: DaySectionCardUi,
    onDeleteRecord: (String) -> Unit,
    onEditRecord: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = section.date.inlineDayHeader(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            val summary = section.items.firstOrNull() as? DetailSectionUi.SummaryChip
            if (summary != null) {
                SummaryChip(
                    expenseText = centsText(summary.expenseCents),
                    incomeText = centsText(summary.incomeCents),
                )
            }
        }

        section.items.drop(1).forEachIndexed { index, item ->
            when (item) {
                is DetailSectionUi.RecordItem -> {
                    RecordItemRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (index > 0) Modifier.padding(top = 8.dp) else Modifier),
                        record = item.record,
                        onDelete = { onDeleteRecord(item.record.id) },
                        onEdit = { onEditRecord(item.record.id) },
                    )
                }

                is DetailSectionUi.SummaryChip -> Unit
            }
        }
    }
}

@Composable
private fun SummaryChip(
    expenseText: String,
    incomeText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryMetricPill(
            label = stringResource(R.string.bookkeeping_expense_short),
            value = expenseText,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        SummaryMetricPill(
            label = stringResource(R.string.bookkeeping_income_short),
            value = incomeText,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun SummaryMetricPill(
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    GlassCard(
        shape = RoundedCornerShape(99.dp),
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        containerAlpha = 0.78f,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.88f),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 顶部摘要区
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailTopHeader(
    timeFilter: BookkeepingTimeFilter,
    chipLabel: String,
    onChipClick: () -> Unit,
    onCustomRangeClick: () -> Unit,
    onTimeFilterChange: (BookkeepingTimeFilter) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryTypeChip(text = chipLabel, onClick = onChipClick)

        EnhancedTimeFilterBar(
            customRangeLabel = timeFilter.dateRange().displayLabel(),
            isCustomRangeSelected = timeFilter is BookkeepingTimeFilter.Custom,
            onCustomRangeClick = onCustomRangeClick,
            presets = rememberBookkeepingTimePresets(),
            selectedPresetKey = timeFilter.selectedPresetKey,
            onPresetSelected = { preset ->
                onTimeFilterChange(
                    BookkeepingTimeFilter.Recent(
                        BookkeepingRecentPreset.valueOf(preset.key)
                    )
                )
            },
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 账单条目行
// ─────────────────────────────────────────────────────────────────────────────

@Composable
@Suppress("DEPRECATION")
private fun RecordItemRow(
    modifier: Modifier = Modifier,
    record: BookkeepingRecordUi,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        },
    )

    androidx.compose.material3.SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val isSwipingToDelete = dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
            val color by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1f else 0f,
                label = "swipe_bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = color * 0.8f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isSwipingToDelete) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.bookkeeping_swipe_delete),
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            borderWidth = 0.dp,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            containerAlpha = 0.58f,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = record.categoryName.take(1),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text(
                        text = record.categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = recordLineSubtitle(record),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = signedAmount(record),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.bookkeeping_edit_action),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun List<BookkeepingRecordUi>.filterBy(timeFilter: BookkeepingTimeFilter): List<BookkeepingRecordUi> {
    val range = timeFilter.dateRange()
    return this.filter { record ->
        record.happenedDate in range.startDate..range.endDate
    }.sortedByDescending { it.happenedAt }
}

private fun List<BookkeepingRecordUi>.toSummary(): DetailSummaryUi {
    return fold(DetailSummaryUi()) { acc, record ->
        when (record.type) {
            BookkeepingRecordType.EXPENSE -> acc.copy(expenseCents = acc.expenseCents + record.amountCents)
            BookkeepingRecordType.INCOME -> acc.copy(incomeCents = acc.incomeCents + record.amountCents)
            BookkeepingRecordType.EXCLUDED -> acc
        }
    }
}

private fun List<BookkeepingRecordUi>.toDaySectionCards(): List<DaySectionCardUi> {
    return groupBy { it.happenedDate }
        .toSortedMap(compareByDescending { it })
        .map { (date, records) ->
            val sortedRecords = records.sortedByDescending { it.happenedAt }
            val summary = sortedRecords.toSummary()
            DaySectionCardUi(
                date = date,
                items = buildList {
                    add(
                        DetailSectionUi.SummaryChip(
                            expenseCents = summary.expenseCents,
                            incomeCents = summary.incomeCents,
                        )
                    )
                    sortedRecords.forEach { record ->
                        add(DetailSectionUi.RecordItem(record = record))
                    }
                },
            )
        }
}

@Composable
private fun LocalDate.dayLabel(today: LocalDate = LocalDate.now()): String {
    return when (this) {
        today -> stringResource(R.string.bookkeeping_day_today)
        today.minusDays(1) -> stringResource(R.string.bookkeeping_day_yesterday)
        else -> when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> stringResource(R.string.bookkeeping_day_monday)
            java.time.DayOfWeek.TUESDAY -> stringResource(R.string.bookkeeping_day_tuesday)
            java.time.DayOfWeek.WEDNESDAY -> stringResource(R.string.bookkeeping_day_wednesday)
            java.time.DayOfWeek.THURSDAY -> stringResource(R.string.bookkeeping_day_thursday)
            java.time.DayOfWeek.FRIDAY -> stringResource(R.string.bookkeeping_day_friday)
            java.time.DayOfWeek.SATURDAY -> stringResource(R.string.bookkeeping_day_saturday)
            java.time.DayOfWeek.SUNDAY -> stringResource(R.string.bookkeeping_day_sunday)
        }
    }
}

@Composable
private fun LocalDate.inlineDayHeader(): String {
    return stringResource(
        R.string.bookkeeping_day_header_inline,
        stringResource(R.string.bookkeeping_day_month_day, monthValue, dayOfMonth),
        dayLabel(),
    )
}
