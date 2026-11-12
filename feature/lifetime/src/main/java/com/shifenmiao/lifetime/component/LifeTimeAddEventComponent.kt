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
