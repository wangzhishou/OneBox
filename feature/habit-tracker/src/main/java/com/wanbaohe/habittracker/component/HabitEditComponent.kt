package com.wanbaohe.habittracker.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.habit.repo.HabitRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.habittracker.model.HabitEditUiState
import com.wanbaohe.habittracker.service.HabitService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 新增/编辑习惯 Component — 表单状态机。
 *
 * 写操作走 [HabitService],保存成功后回调 onGoBack。
 */
class HabitEditComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted("habitId") private val habitId: String?,
    dispatchersHolder: DispatchersHolder,
    private val repository: HabitRepository,
    private val service: HabitService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(HabitEditUiState(habitId = habitId, isEditing = habitId != null))
    val uiState: StateFlow<HabitEditUiState> = _uiState

    init {
        if (habitId != null) {
            componentScope.launch {
                repository.getHabit(habitId)?.let { habit ->
                    _uiState.value = _uiState.value.copy(
                        name = habit.name,
                        iconKey = habit.iconKey,
                        colorArgb = habit.colorArgb,
                        repeatType = habit.repeatType,
                        repeatTarget = habit.repeatTarget,
                        weekdaysMask = habit.weekdaysMask,
                        remindMinutes = habit.remindMinutes,
                        note = habit.note.orEmpty(),
                        statsEnabled = habit.statsEnabled,
                    )
                }
            }
        }
    }

    // ─────────── 表单字段 ───────────

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onIconSelect(iconKey: String) {
        _uiState.value = _uiState.value.copy(iconKey = iconKey)
    }

    fun onColorSelect(colorArgb: Long?) {
        _uiState.value = _uiState.value.copy(colorArgb = colorArgb)
    }

    fun onRepeatTypeChange(repeatType: String) {
        _uiState.value = _uiState.value.copy(repeatType = repeatType)
    }

    fun onRepeatTargetChange(delta: Int) {
        val next = (_uiState.value.repeatTarget + delta).coerceIn(1, 30)
        _uiState.value = _uiState.value.copy(repeatTarget = next)
    }

    /** 切换某个星期是否选中(dayOfWeekValue: 1=周一 … 7=周日) */
    fun onWeekdayToggle(dayOfWeekValue: Int) {
        val bit = com.wanbaohe.habittracker.model.HabitRepeat.maskFor(dayOfWeekValue)
        val mask = _uiState.value.weekdaysMask xor bit
        _uiState.value = _uiState.value.copy(weekdaysMask = mask)
    }

    fun onRemindTimeSelect(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(remindMinutes = hour * 60 + minute)
    }

    fun onRemindClear() {
        _uiState.value = _uiState.value.copy(remindMinutes = null)
    }

    fun onNoteChange(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun onStatsEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(statsEnabled = enabled)
    }

    // ─────────── 保存 ───────────

    /** 名称非空校验后落库,成功返回 true(由 Screen 决定 toast/返回) */
    fun save(onSaved: () -> Unit): Boolean {
        val state = _uiState.value
        if (state.name.isBlank() || state.isSaving) return false
        _uiState.value = state.copy(isSaving = true)
        val input = HabitService.HabitInput(
            name = state.name,
            iconKey = state.iconKey,
            colorArgb = state.colorArgb,
            repeatType = state.repeatType,
            repeatTarget = state.repeatTarget,
            weekdaysMask = state.weekdaysMask,
            remindMinutes = state.remindMinutes,
            note = state.note,
            statsEnabled = state.statsEnabled,
        )
        componentScope.launch {
            val result = if (state.habitId != null) {
                service.updateHabit(state.habitId, input, HabitService.ACTOR_USER).map { }
            } else {
                service.createHabit(input, HabitService.ACTOR_USER).map { }
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            result.onSuccess { onSaved() }
        }
        return true
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            @Assisted("habitId") habitId: String?,
        ): HabitEditComponent
    }
}
