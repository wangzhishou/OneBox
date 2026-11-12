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

package com.wanbaohe.a2ui.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class A2uiAction(
    val event: A2uiActionEvent? = null,
    val functionCall: A2uiFunctionCall? = null,
) {
    init {
        require(event != null || functionCall != null) {
            "A2uiAction must contain either event or functionCall"
        }
    }
}

@Serializable
data class A2uiActionEvent(
    val name: String,
    val context: JsonObject? = null,
    val wantResponse: Boolean = false,
    val responsePath: String? = null,
)

@Serializable
data class A2uiFunctionCall(
    val call: String,
    val args: JsonObject? = null,
)
