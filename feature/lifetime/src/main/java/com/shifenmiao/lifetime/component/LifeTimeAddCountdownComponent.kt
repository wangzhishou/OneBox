package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class AddCountdownUiState(
    val isLoading: Boolean = false,
    /** 非空表示编辑已有倒数日 */
    val countdown: CountdownEvent? = null,
)

/**
 * 倒数日新建 / 编辑共用。
 * [countdownId] 为空时是新建；带 id 时先读出原记录预填表单，保存走 update。
 */
class LifeTimeAddCountdownComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val countdownId: Long?,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val countdownRepository: CountdownEventRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddCountdownUiState(isLoading = countdownId != null))
    val uiState = _uiState.asStateFlow()

    init {
        countdownId?.let(::loadCountdown)
    }

    private fun loadCountdown(id: Long) {
        componentScope.launch {
            val countdown = countdownRepository.getCountdownById(id)
            _uiState.emit(AddCountdownUiState(isLoading = false, countdown = countdown))
            if (countdown == null) onGoBack()
        }
    }

    fun save(countdown: CountdownEvent) {
        componentScope.launch {
            if (countdown.id == 0L) {
                countdownRepository.addCountdown(countdown)
            } else {
                countdownRepository.updateCountdown(countdown)
            }
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            countdownId: Long?,
            onGoBack: () -> Unit
        ): LifeTimeAddCountdownComponent
    }
}
