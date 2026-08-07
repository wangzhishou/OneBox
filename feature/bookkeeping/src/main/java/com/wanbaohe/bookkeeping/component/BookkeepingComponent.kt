package com.wanbaohe.bookkeeping.component

import android.content.Context
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity
import com.shifenmiao.database.bookkeeping.model.BookkeepingRecordWithCategory
import com.shifenmiao.database.bookkeeping.repo.BookkeepingRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.model.BookkeepingCategoryUi
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRecordUi
import com.wanbaohe.bookkeeping.model.BookkeepingRestoreResult
import com.wanbaohe.bookkeeping.model.BookkeepingTab
import com.wanbaohe.bookkeeping.model.BookkeepingTimeFilter
import com.wanbaohe.bookkeeping.model.BookkeepingUiState
import com.wanbaohe.bookkeeping.model.CategoriesGrouped
import com.wanbaohe.bookkeeping.model.DefaultCategories
import com.wanbaohe.bookkeeping.model.dateRange
import com.wanbaohe.bookkeeping.model.localizedDefaultCategoryName
import com.wanbaohe.bookkeeping.service.BookkeepingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 记账主页 Component — 仅做 UI 状态编排。
 *
 * - 写操作全部走 [BookkeepingService](AI 工具同样走 Service,共享业务/审计日志)
 * - 表单状态机由 [RecordEditor] 接管
 * - 时间区间数据订阅与聚合由 [StatsAggregator] 接管
 */
class BookkeepingComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen) -> Unit,
    @Assisted("editingRecordId") private val editingRecordId: String?,
    dispatchersHolder: DispatchersHolder,
    private val repository: BookkeepingRepository,
    private val service: BookkeepingService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(BookkeepingUiState())
    val uiState: StateFlow<BookkeepingUiState> = _uiState

    private val expenseCategories = MutableStateFlow<List<BookkeepingCategoryUi>>(emptyList())
    private val incomeCategories = MutableStateFlow<List<BookkeepingCategoryUi>>(emptyList())
    private val excludedCategories = MutableStateFlow<List<BookkeepingCategoryUi>>(emptyList())

    private val recordEditor = RecordEditor(service)
    private val statsAggregator = StatsAggregator(repository, componentScope)

    /** 全部分类(供分类选择 sheet) */
    val allCategoriesFlow: StateFlow<CategoriesGrouped> = combine(
        expenseCategories,
        incomeCategories,
        excludedCategories,
    ) { exp, inc, exc -> CategoriesGrouped(expense = exp, income = inc, excluded = exc) }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), CategoriesGrouped())

    /** 全量账目(供明细页做最近 7 天 / 近半年等区间筛选) */
    val allRecordsFlow: StateFlow<List<BookkeepingRecordUi>> = repository
        .observeRecordsInRange(startTime = 0L, endTime = Long.MAX_VALUE)
        .map { records -> records.map { it.toUi() } }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    init {
        seedDefaultCategories()
        observeCategories()
        statsAggregator.observeRange(_uiState.value.timeFilter.dateRange())
        bindUiState()
        if (editingRecordId != null) {
            componentScope.launch {
                val record = repository.getAllRecords().firstOrNull { it.id == editingRecordId }
                if (record != null) {
                    val categories = repository.getAllCategories()
                    val fallback = categories.firstOrNull { it.type == record.type }?.id
                    val catName = record.categoryId?.let { id ->
                        localizedDefaultCategoryName(id) ?: categories.firstOrNull { it.id == id }?.name
                    } ?: ""
                    val catIconKey = record.categoryId?.let { id -> categories.firstOrNull { it.id == id }?.iconKey } ?: ""
                    val uiRecord = BookkeepingRecordUi(
                        id = record.id,
                        categoryId = record.categoryId,
                        categoryName = catName,
                        categoryIconKey = catIconKey,
                        type = BookkeepingRecordType.fromCode(record.type),
                        amountCents = record.amountCents,
                        note = record.note ?: "",
                        happenedDate = java.time.Instant.ofEpochMilli(record.happenedAt)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        happenedAt = record.happenedAt,
                        excludeFromStats = record.excludeFromStats,
                    )
                    _uiState.value = recordEditor.startEdit(_uiState.value, uiRecord, fallback)
                }
            }
        }
    }

    // ─────────── Tab / Time Filter ───────────

    fun switchTab(tab: BookkeepingTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setTimeFilter(timeFilter: BookkeepingTimeFilter) {
        if (_uiState.value.timeFilter == timeFilter) return
        _uiState.value = _uiState.value.copy(timeFilter = timeFilter)
        statsAggregator.observeRange(timeFilter.dateRange())
    }

    fun previousMonth() = navigateToMonth(_uiState.value.month.minusMonths(1))
    fun nextMonth() = navigateToMonth(_uiState.value.month.plusMonths(1))

    fun navigateToMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(month = month)
    }

    // ─────────── 表单 ───────────

    fun navigateToAddRecord() {
        onNavigate(com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Bookkeeping(
            com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Bookkeeping.Type.AddRecord()
        ))
    }

    fun onTypeChange(type: BookkeepingRecordType) {
        val categoryId = categoriesOfType(type).firstOrNull()?.id
        _uiState.value = recordEditor.changeType(_uiState.value, type, categoryId)
    }

    fun onDateChange(date: LocalDate) {
        _uiState.value = recordEditor.changeDate(_uiState.value, date)
    }

    fun onAmountInputChange(value: String) {
        _uiState.value = recordEditor.changeAmount(_uiState.value, value)
    }

    fun appendAmountDigit(digit: Char) {
        _uiState.value = recordEditor.appendDigit(_uiState.value, digit)
    }

    fun deleteAmountLast() {
        _uiState.value = recordEditor.deleteLast(_uiState.value)
    }

    fun onNoteInputChange(value: String) {
        _uiState.value = recordEditor.changeNote(_uiState.value, value)
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.value = recordEditor.selectCategory(_uiState.value, categoryId)
    }

    fun submitRecord(): Boolean {
        val input = recordEditor.buildInput(_uiState.value) ?: return false
        componentScope.launch {
            service.addRecord(input, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
            onGoBack()
        }
        return true
    }

    fun submitEditRecord(): Boolean {
        val state = _uiState.value
        val recordId = state.editingRecordId ?: return false
        val input = recordEditor.buildInput(state) ?: return false
        componentScope.launch {
            service.editRecord(recordId, input, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
            onGoBack()
        }
        return true
    }

    fun removeRecord(recordId: String) {
        componentScope.launch {
            service.deleteRecord(recordId, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
        }
    }

    // ─────────── 分类 ───────────

    fun addCategory(name: String, type: BookkeepingRecordType) {
        if (name.isBlank()) return
        componentScope.launch {
            service.addCategory(name, type, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
        }
    }

    fun removeCategory(categoryId: String) {
        componentScope.launch {
            service.removeCategory(categoryId, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
        }
    }

    fun renameCategory(categoryId: String, newName: String) {
        if (newName.isBlank()) return
        componentScope.launch {
            service.renameCategory(categoryId, newName, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
        }
    }

    fun reorderCategories(orderedIds: List<String>) {
        componentScope.launch {
            service.reorderCategories(orderedIds)
        }
    }

    // ─────────── 备份 / CSV ───────────

    fun exportBackup(onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        componentScope.launch {
            runCatching { service.exportBackupJson() }
                .onSuccess(onSuccess).onFailure(onFailure)
        }
    }

    fun restoreBackup(
        json: String,
        onSuccess: (BookkeepingRestoreResult) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        componentScope.launch {
            service.restoreBackup(json, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        selectedCategoryId = null,
                    )
                    onSuccess(result)
                }
                .onFailure(onFailure)
        }
    }

    fun exportCsv(context: Context, uri: Uri, onSuccess: () -> Unit, onFailure: () -> Unit) {
        componentScope.launch {
            service.exportCsv(context, uri).onSuccess { onSuccess() }.onFailure { onFailure() }
        }
    }

    fun importCsv(context: Context, uri: Uri, onSuccess: (Int) -> Unit, onFailure: () -> Unit) {
        componentScope.launch {
            service.importCsv(context, uri, BookkeepingService.ACTOR_USER, BookkeepingService.SOURCE_UI)
                .onSuccess(onSuccess).onFailure { onFailure() }
        }
    }

    // ─────────── 内部 ───────────

    private fun bindUiState() {
        combine(expenseCategories, incomeCategories, excludedCategories) { e, i, x -> Triple(e, i, x) }
            .combine(
                combine(
                    statsAggregator.records,
                    statsAggregator.breakdowns,
                    statsAggregator.incomeBreakdowns,
                    statsAggregator.dailyBars,
                ) { r, b, ib, d -> DataQuad(r, b, ib, d) }
            ) { triple, quad -> triple to quad }
            .map { (triple, quad) ->
                val (expense, income, excluded) = triple
                val selectedType = _uiState.value.selectedType
                val categories = when (selectedType) {
                    BookkeepingRecordType.EXPENSE -> expense
                    BookkeepingRecordType.INCOME -> income
                    BookkeepingRecordType.EXCLUDED -> excluded
                }
                val summary = quad.records.fold(com.wanbaohe.bookkeeping.model.BookkeepingMonthSummary()) { acc, item ->
                    when (item.type) {
                        BookkeepingRecordType.EXPENSE -> acc.copy(expenseCents = acc.expenseCents + item.amountCents)
                        BookkeepingRecordType.INCOME -> acc.copy(incomeCents = acc.incomeCents + item.amountCents)
                        BookkeepingRecordType.EXCLUDED -> acc
                    }
                }
                _uiState.value.copy(
                    categories = categories,
                    records = quad.records,
                    breakdowns = quad.breakdowns,
                    incomeBreakdowns = quad.incomeBreakdowns,
                    dailyBars = quad.daily,
                    summary = summary,
                )
            }
            .onEach { state -> _uiState.value = state }
            .launchIn(componentScope)
    }

    private fun observeCategories() {
        repository.observeCategories(BookkeepingRecordType.EXPENSE.code)
            .onEach { list -> expenseCategories.value = list.map { it.toUi() } }
            .launchIn(componentScope)
        repository.observeCategories(BookkeepingRecordType.INCOME.code)
            .onEach { list -> incomeCategories.value = list.map { it.toUi() } }
            .launchIn(componentScope)
        repository.observeCategories(BookkeepingRecordType.EXCLUDED.code)
            .onEach { list -> excludedCategories.value = list.map { it.toUi() } }
            .launchIn(componentScope)
    }

    private fun seedDefaultCategories() {
        componentScope.launch {
            repository.ensureDefaults(DefaultCategories.all())
        }
    }

    private fun categoriesOfType(type: BookkeepingRecordType): List<BookkeepingCategoryUi> = when (type) {
        BookkeepingRecordType.EXPENSE -> expenseCategories.value
        BookkeepingRecordType.INCOME -> incomeCategories.value
        BookkeepingRecordType.EXCLUDED -> excludedCategories.value
    }

    private fun BookkeepingCategoryEntity.toUi(): BookkeepingCategoryUi = BookkeepingCategoryUi(
        id = id,
        // 预置分类按 id 取本地化显示名(兼容旧库中与当前语言不一致的种子名)
        name = localizedDefaultCategoryName(id) ?: name,
        type = BookkeepingRecordType.fromCode(type),
        iconKey = iconKey,
        sortOrder = sortOrder,
        isDefault = isDefault,
    )

    private fun BookkeepingRecordWithCategory.toUi(): BookkeepingRecordUi = BookkeepingRecordUi(
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

    private data class DataQuad(
        val records: List<BookkeepingRecordUi>,
        val breakdowns: List<com.wanbaohe.bookkeeping.model.CategoryBreakdownUi>,
        val incomeBreakdowns: List<com.wanbaohe.bookkeeping.model.CategoryBreakdownUi>,
        val daily: List<com.t8rin.imagetoolbox.core.ui.widget.charts.BarChartEntry>,
    )

    private companion object {
        fun unclassified(): String = AppContext.getString(R.string.bookkeeping_unclassified)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen) -> Unit,
            @Assisted("editingRecordId") editingRecordId: String?,
        ): BookkeepingComponent
    }
}
