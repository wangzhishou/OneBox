package com.wanbaohe.calendar.auspicious.screenLogic

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.calendar.data.AuspiciousDayFinder
import com.wanbaohe.calendar.data.AuspiciousDayResult
import com.wanbaohe.calendar.data.YiJiCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 择日查询子组件状态
 */
@Immutable
data class AuspiciousViewState(
    val isAvoidMode: Boolean = false,
    val selectedItems: Set<String> = emptySet(),
    val results: List<AuspiciousDayResult> = emptyList(),
    val isLoading: Boolean = false,
)

/**
 * 择日查询子组件
 *
 * 职责：吉日择取 / 忌事避讳、事项多选、结果列表
 */
class AuspiciousComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val isAvoidMode: Boolean,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        AuspiciousViewState(isAvoidMode = isAvoidMode)
    )
    val uiState = _uiState.asStateFlow()
    private var searchJob: Job? = null

    /** 切换吉日/忌事模式 */
    fun toggleAvoidMode() {
        val newMode = !_uiState.value.isAvoidMode
        _uiState.value = _uiState.value.copy(
            isAvoidMode = newMode,
            selectedItems = emptySet(),
            results = emptyList()
        )
    }

    /** 切换选中事项 */
    fun toggleItem(item: String) {
        val current = _uiState.value.selectedItems
        val newSet = if (item in current) current - item else current + item
        _uiState.value = _uiState.value.copy(selectedItems = newSet)
        searchAuspiciousDays()
    }

    /** 从结果跳转到日历查看指定日 */
    fun navigateToCalendar(year: Int, month: Int, day: Int) {
        onNavigate(
            Screen.Calendar(
                type = Screen.Calendar.Type.CalendarView(year = year, month = month, day = day)
            )
        )
    }

    private fun searchAuspiciousDays() {
        val state = _uiState.value
        if (state.selectedItems.isEmpty()) {
            _uiState.value = state.copy(results = emptyList(), isLoading = false)
            return
        }
        _uiState.value = state.copy(isLoading = true)
        searchJob?.cancel()
        searchJob = componentScope.launch {
            val now = Calendar.getInstance()
            val results = withContext(defaultDispatcher) {
                AuspiciousDayFinder.findDays(
                    startYear = now.get(Calendar.YEAR),
                    startMonth = now.get(Calendar.MONTH) + 1,
                    startDay = now.get(Calendar.DAY_OF_MONTH),
                    rangeDays = 90,
                    selectedItems = state.selectedItems,
                    isAvoidMode = state.isAvoidMode
                )
            }
            ensureActive()
            _uiState.value = _uiState.value.copy(
                results = results,
                isLoading = false
            )
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            isAvoidMode: Boolean = false,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AuspiciousComponent
    }
}
