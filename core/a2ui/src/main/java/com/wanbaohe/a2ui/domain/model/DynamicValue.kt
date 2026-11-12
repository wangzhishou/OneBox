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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Serializable
sealed class DynamicValue {

    @Serializable
    @SerialName("literal")
    data class Literal(val value: JsonElement) : DynamicValue()

    @Serializable
    @SerialName("path")
    data class Pointer(val path: String) : DynamicValue()

    @Serializable
    @SerialName("function")
    data class Function(
        val name: String,
        val arguments: List<DynamicValue> = emptyList(),
    ) : DynamicValue()

    companion object {
        fun from(value: String): DynamicValue = Literal(JsonPrimitive(value))
        fun from(value: Number): DynamicValue = Literal(JsonPrimitive(value))
        fun from(value: Boolean): DynamicValue = Literal(JsonPrimitive(value))
        fun fromNull(): DynamicValue = Literal(JsonNull)

        fun fromJsonElement(element: JsonElement): DynamicValue = when (element) {
            is JsonNull -> fromNull()
            is JsonPrimitive -> fromPrimitive(element)
            is JsonObject -> fromJsonObject(element)
            is JsonArray -> Literal(element)
        }

        private fun fromPrimitive(primitive: JsonPrimitive): DynamicValue = when {
            primitive.isString -> from(primitive.content)
            primitive.booleanOrNull != null -> from(primitive.booleanOrNull!!)
            primitive.intOrNull != null -> from(primitive.intOrNull!!)
            primitive.longOrNull != null -> from(primitive.longOrNull!!)
            primitive.doubleOrNull != null -> from(primitive.doubleOrNull!!)
            else -> from(primitive.contentOrNull ?: "")
        }

        private fun fromJsonObject(obj: JsonObject): DynamicValue {
            val call = obj["call"]?.let { it as? JsonPrimitive }?.contentOrNull
            val path = obj["path"]?.let { it as? JsonPrimitive }?.contentOrNull
            return when {
                path != null -> Pointer(path)
                call != null -> {
                    val args = obj["args"]
                    Function(
                        name = call,
                        arguments = if (args is JsonArray) {
                            args.map { fromJsonElement(it) }
                        } else {
                            emptyList()
                        },
                    )
                }
                else -> Literal(obj)
            }
        }
    }
}
