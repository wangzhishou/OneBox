package com.wanbaohe.devicestatus.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.devicestatus.domain.PhoneStatus
import com.wanbaohe.devicestatus.domain.PhoneStatusSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DeviceStatusComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val source: PhoneStatusSource,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _status = MutableStateFlow<PhoneStatus?>(null)
    val status: StateFlow<PhoneStatus?> = _status

    init {
        source.snapshots()
            .onEach { _status.value = it }
            .launchIn(componentScope)
    }

    fun refresh() {
        source.refresh()
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): DeviceStatusComponent
    }
}
