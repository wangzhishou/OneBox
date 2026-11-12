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

package com.wanbaohe.a2ui.transport

import com.wanbaohe.a2ui.domain.model.A2uiAction
import com.wanbaohe.a2ui.domain.model.A2uiMessage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

data class A2uiSurfaceRequest(
    val surfaceId: String,
    val catalogId: String = "basic",
    val endpoint: String? = null,
    val initialData: JsonObject? = null,
)

typealias A2uiMessageListener = (A2uiMessage) -> Unit

interface A2uiTransport {
    val isConnected: StateFlow<Boolean>

    fun connect(request: A2uiSurfaceRequest)

    fun sendAction(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    )

    fun sendFunctionResponse(functionCallId: String, call: String, value: JsonObject?)

    fun sendFunctionError(functionCallId: String, code: String, message: String)

    fun addMessageListener(id: String, listener: A2uiMessageListener)

    fun removeMessageListener(id: String)

    fun disconnect()
}
