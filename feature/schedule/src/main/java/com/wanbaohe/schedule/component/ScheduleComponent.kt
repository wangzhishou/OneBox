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

package com.wanbaohe.schedule.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.schedule.repo.ScheduleRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.schedule.model.ScheduleEvent
import com.wanbaohe.schedule.model.toModel
import com.wanbaohe.schedule.service.ScheduleService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Immutable
data class ScheduleUiState(
    val linkedTaskId: String? = null,
    val focusDateMillis: Long? = null,
    val events: List<ScheduleEvent> = emptyList(),
    val isLoading: Boolean = true,
)

class ScheduleComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val linkedTaskId: String?,
    @Assisted val focusDateMillis: Long?,
    dispatchersHolder: DispatchersHolder,
    private val repository: ScheduleRepository,
    private val service: ScheduleService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            linkedTaskId = linkedTaskId,
            focusDateMillis = focusDateMillis,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        repository.observeEvents()
            .onEach { events ->
                _uiState.value = _uiState.value.copy(
                    events = events.map { it.toModel() }.sortedBy { it.startUtcMillis },
                    isLoading = false,
                )
            }
            .launchIn(componentScope)

        componentScope.launch {
            service.refreshReminders()
        }
    }

    fun openTask(taskId: String) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(
                    editingTaskId = taskId,
                )
            )
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            linkedTaskId: String?,
            focusDateMillis: Long?,
        ): ScheduleComponent
    }
}
