package com.shifenmiao.lifetime.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class LifeTimeAddMilestoneComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val personalMilestoneRepository: PersonalMilestoneRepository
) : BaseComponent(dispatchersHolder, componentContext) {

    fun addMilestone(milestone: PersonalMilestone) {
        componentScope.launch {
            personalMilestoneRepository.addMilestone(milestone)
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): LifeTimeAddMilestoneComponent
    }
}
