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

package com.wanbaohe.a2ui

import com.wanbaohe.a2ui.domain.model.A2uiMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

object A2uiContentParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }

    fun parse(
        jsonString: String,
        surfaceId: String = "ai_dynamic",
    ): A2uiMessage.CreateSurface? {
        if (jsonString.isBlank()) return null
        return try {
            val root = json.parseToJsonElement(jsonString).jsonObject

            val surfaceObj = root["createSurface"]?.jsonObject
                ?: root["body"]?.jsonObject
                ?: root

            if (surfaceObj["components"] == null) return null

            val withSurfaceId = JsonObject(
                surfaceObj.toMutableMap().apply {
                    put("surfaceId", JsonPrimitive(surfaceId))
                }
            )

            json.decodeFromJsonElement(A2uiMessage.CreateSurface.serializer(), withSurfaceId)
        } catch (_: Exception) {
            null
        }
    }

    fun isA2uiFormat(jsonString: String): Boolean = parse(jsonString) != null
}
