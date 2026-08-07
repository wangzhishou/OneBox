package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class CountdownDetailUiState(
    val countdown: CountdownEvent? = null,
)

class LifeTimeCountdownDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val countdownId: Long,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val countdownRepository: CountdownEventRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(CountdownDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCountdown()
    }

    private fun loadCountdown() {
        componentScope.launch {
            val item = countdownRepository.getCountdownById(countdownId)
            _uiState.emit(_uiState.value.copy(countdown = item))
        }
    }

    fun updateNote(note: String) {
        componentScope.launch {
            val current = _uiState.value.countdown ?: return@launch
            val updated = current.copy(note = note.takeIf { it.isNotBlank() })
            countdownRepository.updateCountdown(updated)
            _uiState.emit(_uiState.value.copy(countdown = updated))
        }
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
        ): LifeTimeCountdownDetailComponent
    }
}
