package com.wanbaohe.habittracker.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.habit.entity.HabitEntity
import com.shifenmiao.database.habit.repo.HabitRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.habittracker.model.CheckInUiState
import com.wanbaohe.habittracker.model.HabitStatsUiState
import com.wanbaohe.habittracker.model.HabitTab
import com.wanbaohe.habittracker.model.HabitWithStatus
import com.wanbaohe.habittracker.service.HabitService
import com.wanbaohe.habittracker.service.HabitStatsCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 打卡主页 Component — 仅做 UI 状态编排。
 *
 * - 写操作全部走 [HabitService](AI 工具同样走 Service,共享业务/活动日志)
 * - 统计聚合由 [HabitStatsCalculator] 纯函数完成
 * - 未来日期只读,不允许补打卡
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HabitTrackerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: HabitRepository,
    private val service: HabitService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState

    private val _statsState = MutableStateFlow(HabitStatsUiState())
    val statsState: StateFlow<HabitStatsUiState> = _statsState

    private val selectedDay = MutableStateFlow(LocalDate.now().toEpochDay())

    /** 当前全量习惯快照,统计刷新时使用 */
    private var currentHabits: List<HabitEntity> = emptyList()

    init {
        seedPresetHabits()
        observeSelectedDay()
        observeHabitsForStats()
        refreshStats()
    }

    /** 首次进入播种预置习惯(幂等:持久化 flag + 表为空双条件,只播一次) */
    private fun seedPresetHabits() {
        componentScope.launch {
            service.seedPresetHabitsIfNeeded()
        }
    }

    // ─────────── Tab / 日期 / 管理模式 ───────────

    fun toggleTab(tab: HabitTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun selectDate(epochDay: Long) {
        if (epochDay == _uiState.value.selectedDateEpochDay) return
        selectedDay.value = epochDay
        _uiState.value = _uiState.value.copy(selectedDateEpochDay = epochDay)
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = !_uiState.value.isEditMode)
    }

    // ─────────── 导航 ───────────

    fun navigateToAddHabit() {
        onNavigate(Screen.HabitTracker(Screen.HabitTracker.Type.Edit(null)))
    }

    fun navigateToEditHabit(habitId: String) {
        onNavigate(Screen.HabitTracker(Screen.HabitTracker.Type.Edit(habitId)))
    }

    // ─────────── 写操作 ───────────

    /** 打卡/取消打卡。未来日期只读,直接忽略。 */
    fun toggleCheckIn(habitId: String) {
        val day = _uiState.value.selectedDateEpochDay
        if (day > LocalDate.now().toEpochDay()) return
        val item = _uiState.value.habits.firstOrNull { it.habit.id == habitId } ?: return
        componentScope.launch {
            if (item.isChecked) {
                service.uncheckIn(habitId = habitId, epochDay = day)
            } else {
                // 仅真正写入(非重复打卡)成功时撒花;取消打卡不撒
                service.checkIn(
                    habitId = habitId,
                    epochDay = day,
                    actor = HabitService.ACTOR_USER,
                ).onSuccess { inserted ->
                    if (inserted) AppToastHost.showConfetti()
                }
            }
            refreshStats()
        }
    }

    fun deleteHabit(habitId: String) {
        componentScope.launch {
            service.deleteHabit(habitId = habitId, actor = HabitService.ACTOR_USER)
            refreshStats()
        }
    }

    // ─────────── 内部 ───────────

    /** 订阅选中日的习惯 + 打卡记录,组装列表状态 */
    private fun observeSelectedDay() {
        selectedDay
            .flatMapLatest { day ->
                combine(
                    repository.observeHabits(),
                    repository.observeCheckIns(day),
                ) { habits, checkIns ->
                    val checkedIds = checkIns.map { it.habitId }.toSet()
                    habits
                        .filter { com.wanbaohe.habittracker.model.HabitRepeat.isDueOn(it, day) }
                        .sortedBy { it.sortOrder }
                        .map { HabitWithStatus(habit = it, isChecked = it.id in checkedIds) }
                }
            }
            .onEach { list ->
                _uiState.value = _uiState.value.copy(
                    habits = list,
                    doneCount = list.count { it.isChecked },
                    dueCount = list.size,
                )
            }
            .launchIn(componentScope)
    }

    /** 习惯集合变化时刷新统计 */
    private fun observeHabitsForStats() {
        repository.observeHabits()
            .onEach { habits ->
                currentHabits = habits
                refreshStats()
            }
            .launchIn(componentScope)
    }

    /** 统计以"今天"为锚:本周打卡率、今日分布、近 N 天趋势 */
    private fun refreshStats() {
        val habits = currentHabits
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val trendDays = _statsState.value.trendDays
        componentScope.launch {
            val weekStart = today.with(DayOfWeek.MONDAY).toEpochDay()
            val rangeStart = minOf(weekStart, todayEpochDay - trendDays + 1)
            val checkIns = repository.getCheckInsBetween(rangeStart, todayEpochDay)
            _statsState.value = _statsState.value.copy(
                weekRates = HabitStatsCalculator.weekRates(habits, checkIns, weekStart),
                distributionDone = HabitStatsCalculator
                    .distribution(habits, checkIns, todayEpochDay).first,
                distributionMissed = HabitStatsCalculator
                    .distribution(habits, checkIns, todayEpochDay).second,
                distributionNotStarted = HabitStatsCalculator
                    .distribution(habits, checkIns, todayEpochDay).third,
                totalHabits = habits.size,
                trendPoints = HabitStatsCalculator.trend(
                    habits = habits,
                    checkIns = checkIns,
                    endEpochDay = todayEpochDay,
                    days = trendDays,
                ),
            )
        }
    }

    fun setTrendDays(days: Int) {
        if (days == _statsState.value.trendDays) return
        _statsState.value = _statsState.value.copy(trendDays = days)
        refreshStats()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): HabitTrackerComponent
    }
}
