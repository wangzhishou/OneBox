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

package com.wanbaohe.a2ui.state

import com.wanbaohe.a2ui.domain.model.A2uiAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class A2uiActionEvent(
    val surfaceId: String,
    val sourceComponentId: String,
    val action: A2uiAction,
    val actionId: String,
)

@Singleton
class A2uiActionBus @Inject constructor() {

    private val _events = MutableSharedFlow<A2uiActionEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<A2uiActionEvent> = _events.asSharedFlow()

    suspend fun emit(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    ) {
        _events.emit(A2uiActionEvent(surfaceId, sourceComponentId, action, actionId))
    }

    fun tryEmit(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    ): Boolean = _events.tryEmit(A2uiActionEvent(surfaceId, sourceComponentId, action, actionId))
}
