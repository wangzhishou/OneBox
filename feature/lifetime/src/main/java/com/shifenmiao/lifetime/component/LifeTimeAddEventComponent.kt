package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.FrequencyEventRepository
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class AddEventUiState(
    val isLoading: Boolean = false,
    /** 非空表示编辑已有的时光频率 */
    val event: FrequencyEvent? = null,
)

/**
 * 时光频率新建 / 编辑共用。
 * [eventId] 为空时是新建；带 id 时先读出原记录预填表单，保存走 update。
 */
class LifeTimeAddEventComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val eventId: Long?,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val frequencyEventRepository: FrequencyEventRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddEventUiState(isLoading = eventId != null))
    val uiState = _uiState.asStateFlow()

    init {
        eventId?.let(::loadEvent)
    }

    private fun loadEvent(id: Long) {
        componentScope.launch {
            val event = frequencyEventRepository.allEventsFlow.first().firstOrNull { it.id == id }
            _uiState.emit(AddEventUiState(isLoading = false, event = event))
            if (event == null) onGoBack()
        }
    }

    fun save(event: FrequencyEvent) {
        componentScope.launch {
            if (event.id == 0L) {
                frequencyEventRepository.addEvent(event)
            } else {
                frequencyEventRepository.updateEvent(event)
            }
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            eventId: Long?,
            onGoBack: () -> Unit
        ): LifeTimeAddEventComponent
    }
}
