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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = A2uiComponentSerializer::class)
data class A2uiComponent(
    val id: String,
    @SerialName("component")
    val type: String,
    val properties: Map<String, DynamicValue> = emptyMap(),
    val children: ChildList = ChildList.Empty,
    val action: A2uiAction? = null,
) {
    val childIds: List<String>
        get() = (children as? ChildList.Array)?.children ?: emptyList()

    val childTemplate: ChildList.Template?
        get() = children as? ChildList.Template
}

object A2uiComponentSerializer : KSerializer<A2uiComponent> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("A2uiComponent")

    override fun deserialize(decoder: Decoder): A2uiComponent {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("A2uiComponent can only be deserialized from JSON")
        val obj = input.decodeJsonElement().jsonObject

        val id = obj["id"]?.jsonPrimitive?.content
            ?: throw SerializationException("A2uiComponent missing required field 'id'")
        val type = obj["component"]?.jsonPrimitive?.content
            ?: throw SerializationException("A2uiComponent missing required field 'component'")

        val children = obj["children"]?.let { ChildListSerializer.parse(it) } ?: ChildList.Empty
        val action = obj["action"]?.let {
            input.json.decodeFromJsonElement(A2uiAction.serializer(), it)
        }

        val reservedKeys = setOf("id", "component", "children", "action")
        val properties = obj.filterKeys { it !in reservedKeys }.mapValues { (_, value) ->
            DynamicValue.fromJsonElement(value)
        }

        return A2uiComponent(
            id = id,
            type = type,
            properties = properties,
            children = children,
            action = action,
        )
    }

    override fun serialize(encoder: Encoder, value: A2uiComponent) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("A2uiComponent can only be serialized to JSON")

        val map = mutableMapOf<String, JsonElement>(
            "id" to JsonPrimitive(value.id),
            "component" to JsonPrimitive(value.type),
        )

        if (value.children != ChildList.Empty) {
            map["children"] = ChildListSerializer.toJsonElement(value.children)
        }

        value.action?.let {
            map["action"] = output.json.encodeToJsonElement(A2uiAction.serializer(), it)
        }

        value.properties.forEach { (key, dynamicValue) ->
            map[key] = output.json.encodeToJsonElement(DynamicValue.serializer(), dynamicValue)
        }

        output.encodeJsonElement(JsonObject(map))
    }
}
