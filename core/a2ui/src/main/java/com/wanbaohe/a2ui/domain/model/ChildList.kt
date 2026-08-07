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
