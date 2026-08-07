package com.shifenmiao.lifetime.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.FrequencyEventRepository
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class LifeTimeAddEventComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val frequencyEventRepository: FrequencyEventRepository
) : BaseComponent(dispatchersHolder, componentContext) {

    fun addEvent(event: FrequencyEvent) {
        componentScope.launch {
            frequencyEventRepository.addEvent(event)
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): LifeTimeAddEventComponent
    }
}
