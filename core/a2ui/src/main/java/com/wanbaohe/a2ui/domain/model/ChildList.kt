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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class ChildList {
    data class Array(val children: List<String>) : ChildList()
    data class Template(val path: String, val componentId: String) : ChildList()

    val isEmpty: Boolean
        get() = when (this) {
            is Array -> children.isEmpty()
            is Template -> false
        }

    companion object {
        val Empty = Array(emptyList())
    }
}

object ChildListSerializer : KSerializer<ChildList> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChildList")

    override fun deserialize(decoder: Decoder): ChildList {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("ChildList can only be deserialized from JSON")
        val element = input.decodeJsonElement()
        return parse(element)
    }

    override fun serialize(encoder: Encoder, value: ChildList) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("ChildList can only be serialized to JSON")
        output.encodeJsonElement(toJsonElement(value))
    }

    fun parse(element: JsonElement): ChildList = when (element) {
        is JsonArray -> ChildList.Array(
            element.mapNotNull { it.jsonPrimitive.contentOrNull }
        )

        is JsonObject -> {
            val path = element["path"]?.let { it as? JsonPrimitive }?.contentOrNull
            val componentId = element["componentId"]?.let { it as? JsonPrimitive }?.contentOrNull
            if (path != null && componentId != null) {
                ChildList.Template(path = path, componentId = componentId)
            } else {
                ChildList.Empty
            }
        }

        else -> ChildList.Empty
    }

    fun toJsonElement(value: ChildList): JsonElement = when (value) {
        is ChildList.Array -> JsonArray(
            value.children.map { JsonPrimitive(it) }
        )

        is ChildList.Template -> JsonObject(
            mapOf(
                "path" to JsonPrimitive(value.path),
                "componentId" to JsonPrimitive(value.componentId),
            )
        )
    }
}
