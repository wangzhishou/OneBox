package com.wanbaohe.period.component

import androidx.annotation.StringRes
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodCalculations
import com.wanbaohe.period.model.PeriodMonthGroup
import com.wanbaohe.period.model.PeriodStatsRange
import com.wanbaohe.period.model.PeriodTab
import com.wanbaohe.period.model.PeriodUiState
import com.wanbaohe.period.service.PeriodService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
/**
 * 经期主页 Component — 记录列表 + 周期统计两个 tab。
 * 写操作走 PeriodService；订阅走 Repository Flow（经 Service 包装）。
 */
class PeriodComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted("initialTab") initialTab: PeriodTab?,
    dispatchersHolder: DispatchersHolder,
    private val service: PeriodService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        PeriodUiState(selectedTab = initialTab ?: PeriodTab.RECORDS)
    )
    val uiState: StateFlow<PeriodUiState> = _uiState

    private val searchQuery = MutableStateFlow("")
    private val statsRange = MutableStateFlow(PeriodStatsRange.SIX_MONTHS)
    private val calendarMonth = MutableStateFlow(YearMonth.now())

    init {
        observeRecords()
        observeStats()
        observeCalendar()
    }

    fun switchTab(tab: PeriodTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setStatsRange(range: PeriodStatsRange) {
        statsRange.value = range
        _uiState.update { it.copy(statsRange = range) }
    }

    fun onSearchQueryChange(value: String) {
        searchQuery.value = value
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun toggleSearchActive() {
        _uiState.update {
            val next = !it.isSearchActive
            it.copy(
                isSearchActive = next,
                searchQuery = if (next) it.searchQuery else "",
            )
        }
        if (!_uiState.value.isSearchActive) searchQuery.value = ""
    }

    fun onCalendarMonthChange(deltaMonths: Long) {
        calendarMonth.update { it.plusMonths(deltaMonths) }
    }

    fun openCalendarDay(date: java.time.LocalDate) {
        _uiState.value.calendarRecordIds[date]?.let(::openDetail)
    }

    fun toggleMonthCollapse(yearMonthKey: String) {
        _uiState.update {
            val collapsed = it.collapsedMonths.toMutableSet()
            if (!collapsed.add(yearMonthKey)) collapsed.remove(yearMonthKey)
            it.copy(collapsedMonths = collapsed)
        }
    }

    fun openAddRecord() {
        onNavigate(Screen.Period(Screen.Period.Type.Add))
    }

    fun openDetail(recordId: String) {
        onNavigate(Screen.Period(Screen.Period.Type.Detail(recordId)))
    }

    fun openEdit(recordId: String) {
        onNavigate(Screen.Period(Screen.Period.Type.Edit(recordId)))
    }

    fun navigateToAiAssist() {
        onNavigate(
            Screen.AITabChatScreen(
                Conversation(
                    entryType = AIConversationEntryType.ASSISTANT,
                    title = AppContext.getString(R.string.period_ai_assist_title),
                    template = AppContext.getString(R.string.period_ai_assist_fill_in),
                )
            )
        )
    }

    private fun observeRecords() {
        combine(
            service.observeAll(),
            searchQuery,
        ) { records, query ->
            records.asSequence()
                .filter {
                    if (query.isBlank()) {
                        true
                    } else {
                        it.note?.contains(query, ignoreCase = true) == true ||
                            it.symptoms.any { s -> s.name.contains(query, ignoreCase = true) }
                    }
                }
                .toList()
        }
            .onEach { list ->
                _uiState.update { state ->
                    state.copy(
                        records = list,
                        monthGroups = buildMonthGroups(list, state.collapsedMonths),
                    )
                }
            }
            .launchIn(componentScope)
    }

    private fun observeCalendar() {
        combine(service.observeAll(), calendarMonth) { records, month -> records to month }
            .onEach { (records, month) ->
                val starts = records.filter { it.isPeriodStart }.map { it.recordDate }
                val ends = records.filter { it.isPeriodEnd }.map { it.recordDate }
                val avgPeriod = PeriodCalculations.averagePeriodDays(starts, ends)
                val periodDays = (1..month.lengthOfMonth())
                    .asSequence()
                    .map { month.atDay(it) }
                    .filter { PeriodCalculations.isInPeriodWindow(it, starts, avgPeriod) }
                    .toSet()
                _uiState.update {
                    it.copy(
                        calendarMonth = month,
                        calendarPeriodDays = periodDays,
                        calendarRecordIds = records.associate { r -> r.recordDate to r.id },
                    )
                }
            }
            .launchIn(componentScope)
    }

    private fun observeStats() {
        combine(service.observeAll(), statsRange) { _, range -> range }
            .onEach { range ->
                componentScope.launch {
                    val stats = service.getStats(range)
                    _uiState.update {
                        it.copy(stats = stats, statsRange = range, today = java.time.LocalDate.now())
                    }
                    // 重建分组时带上最新统计上下文
                    _uiState.update { state ->
                        state.copy(monthGroups = buildMonthGroups(state.records, state.collapsedMonths))
                    }
                }
            }
            .launchIn(componentScope)
    }

    private fun buildMonthGroups(
        records: List<com.wanbaohe.period.model.PeriodRecordUi>,
        collapsed: Set<String>,
    ): List<PeriodMonthGroup> {
        return records
            .groupBy { YearMonth.from(it.recordDate) }
            .toSortedMap(compareByDescending { it })
            .map { (ym, list) ->
                PeriodMonthGroup(
                    yearMonth = ym,
                    records = list.sortedByDescending { it.recordDate },
                    isCollapsed = collapsed.contains(ym.toString()),
                )
            }
    }

    private fun showToast(@StringRes resId: Int) {
        AppToastHost.showToast(AppContext.getString(resId))
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            @Assisted("initialTab") initialTab: PeriodTab?,
        ): PeriodComponent
    }
}
