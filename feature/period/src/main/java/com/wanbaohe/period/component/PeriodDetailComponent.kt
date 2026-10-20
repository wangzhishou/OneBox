package com.wanbaohe.period.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodStatus
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.service.PeriodService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
data class PeriodDetailUi(
    val id: String = "",
    val recordDate: LocalDate = LocalDate.now(),
    val isPeriodStart: Boolean = false,
    val isPeriodEnd: Boolean = false,
    val flowIntensity: PeriodFlow = PeriodFlow.NONE,
    val symptoms: List<PeriodSymptom> = emptyList(),
    val mood: PeriodMood = PeriodMood.NORMAL,
    val note: String? = null,
    val cycleDay: Int? = null,
    val status: PeriodStatus = PeriodStatus.NORMAL,
    val nextPeriodStart: LocalDate? = null,
    val daysUntilNext: Int? = null,
    val predictedPeriodEnd: LocalDate? = null,
    val averagePeriodDays: Int = PeriodService.DEFAULT_PERIOD_DAYS_DISPLAY,
    val isLoaded: Boolean = false,
)

/** 记录详情页 Component。 */
class PeriodDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted("recordId") private val recordId: String,
    dispatchersHolder: DispatchersHolder,
    private val service: PeriodService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PeriodDetailUi())
    val uiState: StateFlow<PeriodDetailUi> = _uiState

    init {
        // 订阅数据流:从编辑页返回、或 Agent 改动数据时自动刷新
        service.observeAll()
            .onEach { load() }
            .launchIn(componentScope)
    }

    fun openEdit() {
        onNavigate(Screen.Period(Screen.Period.Type.Edit(recordId)))
    }

    fun reload() {
        load()
    }

    private fun load() {
        componentScope.launch {
            val view = service.getRecordUi(recordId)
            if (view == null) {
                // 记录已被删除(如 Agent 删除),退出详情页
                if (_uiState.value.isLoaded) onGoBack()
                return@launch
            }
            val ctx = service.getCycleContext()
            val today = LocalDate.now()
            val next = ctx.nextPeriodStart
            val predictedEnd = view.recordDate.plusDays((ctx.averagePeriodDays - 1).toLong())
            _uiState.update {
                PeriodDetailUi(
                    id = view.id,
                    recordDate = view.recordDate,
                    isPeriodStart = view.isPeriodStart,
                    isPeriodEnd = view.isPeriodEnd,
                    flowIntensity = view.flowIntensity,
                    symptoms = view.symptoms,
                    mood = view.mood,
                    note = view.note,
                    cycleDay = view.cycleDay,
                    status = when {
                        PeriodService.isInPeriodWindow(
                            date = view.recordDate,
                            startDates = ctx.startDates,
                            averagePeriodDays = ctx.averagePeriodDays,
                        ) -> PeriodStatus.PERIOD

                        PeriodService.isInOvulationWindow(
                            date = view.recordDate,
                            nextStart = ctx.nextPeriodStart,
                        ) -> PeriodStatus.OVULATION

                        else -> PeriodStatus.NORMAL
                    },
                    nextPeriodStart = next,
                    daysUntilNext = next?.let {
                        java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt()
                    },
                    predictedPeriodEnd = predictedEnd,
                    averagePeriodDays = ctx.averagePeriodDays,
                    isLoaded = true,
                )
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            @Assisted("recordId") recordId: String,
        ): PeriodDetailComponent
    }
}
