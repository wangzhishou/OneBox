/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
