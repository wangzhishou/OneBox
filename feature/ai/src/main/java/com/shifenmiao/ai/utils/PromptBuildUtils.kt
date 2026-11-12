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

package com.shifenmiao.ai.utils

import com.wanbaohe.a2ui.A2uiContentParser
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object PromptBuildUtils {

    fun buildPromptString(json: String): String? {
        if (json.isBlank()) return null
        return try {
            val message = A2uiContentParser.parse(json) ?: return null
            val submitAction = message.components.firstOrNull { it.action != null }?.action
            val template = submitAction?.event?.context
                ?.get("prompt")?.jsonPrimitive?.contentOrNull
            val result = if (template != null) {
                template.replace(Regex("\\$\\{(\\w+)}")) { match ->
                    resolveValue(message.dataModel[match.groupValues[1]]) ?: ""
                }
            } else {
                message.dataModel.entries
                    .mapNotNull { (key, value) ->
                        resolveValue(value)
                            ?.takeIf { it.isNotBlank() }
                            ?.let { "$key: $it" }
                    }
                    .joinToString("")
            }
            result.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveValue(element: kotlinx.serialization.json.JsonElement?): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull
        is JsonArray -> element.mapNotNull { resolveValue(it) }
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
            ?: element.toString()
        is JsonObject -> element.toString()
        else -> null
    }
}
