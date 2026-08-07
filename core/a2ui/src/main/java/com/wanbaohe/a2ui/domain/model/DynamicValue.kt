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
