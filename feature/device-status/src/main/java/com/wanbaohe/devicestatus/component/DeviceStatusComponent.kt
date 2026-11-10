package com.wanbaohe.devicestatus.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.network.service.CloudflareNetworkInfo
import com.shifenmiao.network.service.CloudflareNetworkInfoService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.devicestatus.domain.PhoneStatus
import com.wanbaohe.devicestatus.domain.PhoneStatusSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DeviceStatusComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val source: PhoneStatusSource,
    private val networkInfoService: CloudflareNetworkInfoService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _status = MutableStateFlow<PhoneStatus?>(null)
    val status: StateFlow<PhoneStatus?> = _status

    private val _networkNode = MutableStateFlow<CloudflareNetworkInfo?>(null)
    val networkNode: StateFlow<CloudflareNetworkInfo?> = _networkNode
    private var networkNodeJob: Job? = null

    init {
        source.snapshots()
            .onEach { _status.value = it }
            .launchIn(componentScope)
        refreshNetworkNode()
    }

    fun refresh() {
        source.refresh()
        refreshNetworkNode()
    }

    private fun refreshNetworkNode() {
        networkNodeJob?.cancel()
        _networkNode.value = null
        networkNodeJob = componentScope.launch {
            _networkNode.value = networkInfoService.load()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): DeviceStatusComponent
    }
}
