package com.wanbaohe.bookkeeping.model

import com.t8rin.imagetoolbox.core.ui.widget.charts.BarChartEntry
import java.time.LocalDate
import java.time.YearMonth

enum class BookkeepingRecordType(val code: Int) {
    EXPENSE(0),
    INCOME(1),
    EXCLUDED(2);

    companion object {
        fun fromCode(code: Int): BookkeepingRecordType {
            return entries.firstOrNull { it.code == code } ?: EXPENSE
        }
    }
}

data class BookkeepingCategoryUi(
    val id: String,
    val name: String,
    val type: BookkeepingRecordType,
    val iconKey: String,
    val sortOrder: Int,
    val isDefault: Boolean,
)

data class BookkeepingRecordUi(
    val id: String,
    val categoryId: String?,
    val categoryName: String,
    val categoryIconKey: String,
    val type: BookkeepingRecordType,
    val amountCents: Long,
    val note: String,
    val happenedDate: LocalDate,
    val happenedAt: Long,
    val excludeFromStats: Boolean,
)

data class BookkeepingMonthSummary(
    val expenseCents: Long = 0,
    val incomeCents: Long = 0,
)

data class BookkeepingUiState(
    val month: YearMonth = YearMonth.now(),
    val timeFilter: BookkeepingTimeFilter = BookkeepingTimeFilter.Recent(BookkeepingRecentPreset.LAST_1_YEAR),
    val currentTab: BookkeepingTab = BookkeepingTab.DETAIL,
    val selectedType: BookkeepingRecordType = BookkeepingRecordType.EXPENSE,
    val selectedDate: LocalDate = LocalDate.now(),
    val amountInput: String = "",
    val noteInput: String = "",
    val selectedCategoryId: String? = null,
    val editingRecordId: String? = null,
    val summary: BookkeepingMonthSummary = BookkeepingMonthSummary(),
    val categories: List<BookkeepingCategoryUi> = emptyList(),
    val records: List<BookkeepingRecordUi> = emptyList(),
    val breakdowns: List<CategoryBreakdownUi> = emptyList(),
    val incomeBreakdowns: List<CategoryBreakdownUi> = emptyList(),
    val dailyBars: List<BarChartEntry> = emptyList(),
)

enum class BookkeepingTab {
    DETAIL,
    STATS,
    SETTINGS,
}

data class CategoryBreakdownUi(
    val name: String,
    val amountCents: Long,
    val progress: Float,
    val colorIndex: Int,   // 颜色索引，由 Composable 层映射为主题色
)

data class CategoriesGrouped(
    val expense: List<BookkeepingCategoryUi> = emptyList(),
    val income: List<BookkeepingCategoryUi> = emptyList(),
    val excluded: List<BookkeepingCategoryUi> = emptyList(),
)

