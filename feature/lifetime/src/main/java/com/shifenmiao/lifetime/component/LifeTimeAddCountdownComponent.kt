package com.shifenmiao.lifetime.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class LifeTimeAddCountdownComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val countdownRepository: CountdownEventRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    fun addCountdown(event: CountdownEvent) {
        componentScope.launch {
            countdownRepository.addCountdown(event)
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): LifeTimeAddCountdownComponent
    }
}
