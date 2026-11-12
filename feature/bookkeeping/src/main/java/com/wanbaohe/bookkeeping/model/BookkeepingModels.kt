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

package com.wanbaohe.bookkeeping.model

import com.shifenmiao.model.money.AppCurrency
import com.wanbaohe.core.ui.widget.charts.BarChartEntry
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
    /** 记账/金额展示币种(来自 AppSharedStorage,可在设置页改) */
    val currency: AppCurrency = AppCurrency.default,
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

