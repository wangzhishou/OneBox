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

package com.wanbaohe.bookkeeping.component

import com.shifenmiao.database.bookkeeping.repo.BookkeepingRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.core.ui.widget.charts.BarChartEntry
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.model.BookkeepingDateRange
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import com.wanbaohe.bookkeeping.model.BookkeepingTimeFilter
import com.wanbaohe.bookkeeping.model.CategoryBreakdownUi
import com.wanbaohe.bookkeeping.model.dateRange
import com.wanbaohe.bookkeeping.model.localizedDefaultCategoryName
import com.wanbaohe.bookkeeping.model.toEpochMilliRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 按时间区间聚合订阅器 — 把 [BookkeepingRepository] 暴露的 5 路 Flow
 * 聚合成 records/breakdowns/incomeBreakdowns/dailyBars/monthlyBars 五个 StateFlow,
 * Component 直接消费。
 */
internal class StatsAggregator(
    private val repository: BookkeepingRepository,
    private val scope: CoroutineScope,
) {
    private companion object {
        fun unclassified(): String = AppContext.getString(R.string.bookkeeping_unclassified)
    }

    private val _records = MutableStateFlow<List<BookkeepingRecordUi>>(emptyList())
    val records: StateFlow<List<BookkeepingRecordUi>> = _records

    private val _breakdowns = MutableStateFlow<List<CategoryBreakdownUi>>(emptyList())
    val breakdowns: StateFlow<List<CategoryBreakdownUi>> = _breakdowns

    private val _incomeBreakdowns = MutableStateFlow<List<CategoryBreakdownUi>>(emptyList())
    val incomeBreakdowns: StateFlow<List<CategoryBreakdownUi>> = _incomeBreakdowns

    private val _dailyBars = MutableStateFlow<List<BarChartEntry>>(emptyList())
    val dailyBars: StateFlow<List<BarChartEntry>> = _dailyBars

    private val _monthlyBars = MutableStateFlow<List<BarChartEntry>>(emptyList())
    val monthlyBars: StateFlow<List<BarChartEntry>> = _monthlyBars

    private var observeJob: Job? = null

    fun observeRange(range: BookkeepingDateRange) {
        val (start, end) = range.toEpochMilliRange()
        observeJob?.cancel()
        observeJob = scope.launch {
            repository.observeRecordsInRange(start, end)
                .onEach { list -> _records.value = list.map { it.toUi() } }
                .launchIn(this)

            repository.observeExpenseCategorySumsInRange(start, end)
                .onEach { list ->
                    val total = list.sumOf { it.totalCents }
                    _breakdowns.value = list.mapIndexed { index, item ->
                        CategoryBreakdownUi(
                            name = item.categoryId?.let { localizedDefaultCategoryName(it) }
                                ?: item.categoryName
                                ?: unclassified(),
                            amountCents = item.totalCents,
                            progress = if (total == 0L) 0f else item.totalCents.toFloat() / total.toFloat(),
                            colorIndex = index,
                        )
                    }
                }
                .launchIn(this)

            repository.observeIncomeCategorySumsInRange(start, end)
                .onEach { list ->
                    val total = list.sumOf { it.totalCents }
                    _incomeBreakdowns.value = list.mapIndexed { index, item ->
                        CategoryBreakdownUi(
                            name = item.categoryId?.let { localizedDefaultCategoryName(it) }
                                ?: item.categoryName
                                ?: unclassified(),
                            amountCents = item.totalCents,
                            progress = if (total == 0L) 0f else item.totalCents.toFloat() / total.toFloat(),
                            colorIndex = index,
                        )
                    }
                }
                .launchIn(this)

            repository.observeDailyExpenseTotalsInRange(start, end)
                .onEach { totals ->
                    _dailyBars.value = totals.map { item ->
                        BarChartEntry(
                            label = item.timeKey.substringAfterLast('-'),
                            value = item.totalCents.toFloat(),
                        )
                    }
                }
                .launchIn(this)
        }
    }

    private fun com.shifenmiao.database.bookkeeping.model.BookkeepingRecordWithCategory.toUi(): BookkeepingRecordUi {
        return BookkeepingRecordUi(
            id = id,
            categoryId = categoryId,
            categoryName = categoryId?.let { localizedDefaultCategoryName(it) }
                ?: categoryName
                ?: unclassified(),
            categoryIconKey = categoryIconKey ?: "expense",
            type = BookkeepingRecordType.fromCode(type),
            amountCents = amountCents,
            note = note.orEmpty(),
            happenedDate = Instant.ofEpochMilli(happenedAt).atZone(ZoneId.systemDefault()).toLocalDate(),
            happenedAt = happenedAt,
            excludeFromStats = excludeFromStats,
        )
    }
}
