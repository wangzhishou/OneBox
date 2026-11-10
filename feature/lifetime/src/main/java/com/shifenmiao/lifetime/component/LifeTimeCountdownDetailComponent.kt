package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Immutable
data class CountdownDetailUiState(
    val countdown: CountdownEvent? = null,
)

class LifeTimeCountdownDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val countdownId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val countdownRepository: CountdownEventRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(CountdownDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeCountdown()
    }

    /**
     * 用 Flow 观察而不是一次性读取：从"编辑"页返回时详情要跟着变，
     * 记录被别处删掉时也要及时退回。
     */
    private fun observeCountdown() {
        countdownRepository.allCountdownsFlow
            .onEach { items ->
                val item = items.firstOrNull { it.id == countdownId }
                _uiState.emit(CountdownDetailUiState(countdown = item))
            }
            .launchIn(componentScope)
    }

    fun updateNote(note: String) {
        componentScope.launch {
            val current = _uiState.value.countdown ?: return@launch
            val updated = current.copy(note = note.takeIf { it.isNotBlank() })
            countdownRepository.updateCountdown(updated)
            _uiState.emit(_uiState.value.copy(countdown = updated))
        }
    }

    fun editCountdown() {
        onNavigate(Screen.LifeTimeAddCountdown(countdownId = countdownId))
    }

    fun deleteCountdown() {
        componentScope.launch {
            _uiState.value.countdown?.let { countdownRepository.deleteCountdown(it) }
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            countdownId: Long,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): LifeTimeCountdownDetailComponent
    }
}
