package com.wanbaohe.schedule.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.schedule.repo.ScheduleRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.schedule.model.ScheduleEvent
import com.wanbaohe.schedule.model.ScheduleProviderType
import com.wanbaohe.schedule.model.ScheduleSyncState
import com.wanbaohe.schedule.model.toModel
import com.wanbaohe.schedule.service.ScheduleService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Immutable
data class ScheduleUiState(
    val linkedTaskId: String? = null,
    val focusDateMillis: Long? = null,
    val events: List<ScheduleEvent> = emptyList(),
    val googleSyncState: ScheduleSyncState? = null,
    val isLoading: Boolean = true,
)

class ScheduleComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val linkedTaskId: String?,
    @Assisted val focusDateMillis: Long?,
    dispatchersHolder: DispatchersHolder,
    private val repository: ScheduleRepository,
    private val service: ScheduleService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            linkedTaskId = linkedTaskId,
            focusDateMillis = focusDateMillis,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            if (linkedTaskId.isNullOrBlank()) {
                repository.observeEvents()
            } else {
                repository.observeEventsByLinkedTaskId(linkedTaskId)
            },
            repository.observeSyncState(ScheduleProviderType.GOOGLE_CALENDAR.name)
        ) { events, syncState ->
            _uiState.value.copy(
                events = events.map { it.toModel() }.sortedBy { it.startUtcMillis },
                googleSyncState = syncState?.toModel(),
                isLoading = false,
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(componentScope)
    }

    fun createQuickEvent(
        title: String,
        description: String?,
    ) {
        componentScope.launch {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusHours(1)
            val start = now.toInstant().toEpochMilli()
            val end = now.plusHours(1).toInstant().toEpochMilli()
            service.createLocalEvent(
                input = ScheduleService.EventInput(
                    linkedTaskId = linkedTaskId,
                    title = title,
                    description = description,
                    startUtcMillis = start,
                    endUtcMillis = end,
                    timeZoneId = ZoneId.systemDefault().id,
                ),
                source = "UI:ScheduleScreen"
            )
        }
    }

    fun createTodayFocusEvent(title: String) {
        val focusMillis = focusDateMillis ?: return
        componentScope.launch {
            val start = focusMillis
            val end = Instant.ofEpochMilli(focusMillis)
                .atZone(ZoneId.systemDefault())
                .plusHours(1)
                .toInstant()
                .toEpochMilli()
            service.createLocalEvent(
                input = ScheduleService.EventInput(
                    linkedTaskId = linkedTaskId,
                    title = title,
                    startUtcMillis = start,
                    endUtcMillis = end,
                    timeZoneId = ZoneId.systemDefault().id,
                ),
                source = "UI:ScheduleScreen:FocusDate"
            )
        }
    }

    fun saveEventToSystemCalendar(
        eventId: String,
        calendarId: Long,
        onResult: (Result<Long>) -> Unit,
    ) {
        componentScope.launch {
            onResult(
                service.saveEventToSystemCalendar(
                    localEventId = eventId,
                    calendarId = calendarId,
                )
            )
        }
    }

    fun openTodoList() {
        onNavigate(Screen.MarkTodoRouter())
    }

    fun openChineseCalendar() {
        onNavigate(Screen.Calendar())
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            linkedTaskId: String?,
            focusDateMillis: Long?,
        ): ScheduleComponent
    }
}


