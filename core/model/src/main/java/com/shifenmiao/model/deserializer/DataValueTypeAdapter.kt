package com.shifenmiao.model.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.shifenmiao.model.DataValue
import java.lang.reflect.Type

class DataValueTypeAdapter : JsonSerializer<DataValue>, JsonDeserializer<DataValue> {
    override fun serialize(
        src: DataValue,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        when (src) {
            is DataValue.StringValue -> {
                jsonObject.addProperty("type", "StringValue")
                jsonObject.addProperty("value", src.value)
            }

            is DataValue.IntValue -> {
                jsonObject.addProperty("type", "IntValue")
                jsonObject.addProperty("value", src.value)
            }

            is DataValue.BooleanValue -> {
                jsonObject.addProperty("type", "BooleanValue")
                jsonObject.addProperty("value", src.value)
            }

            is DataValue.StringListValue -> {
                jsonObject.addProperty("type", "StringListValue")
                jsonObject.add("value", context.serialize(src.value))
            }

            is DataValue.MapValue -> {
                jsonObject.addProperty("type", "MapValue")
                jsonObject.add("value", context.serialize(src.value))
            }
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DataValue {
        try {
            val jsonObject = json.asJsonObject
            if (jsonObject.isJsonArray) {
                return DataValue.StringListValue(context.deserialize(jsonObject, List::class.java))
            } else if (jsonObject.isJsonObject) {
                return DataValue.MapValue(context.deserialize(jsonObject, Map::class.java))
            } else if (jsonObject.isJsonPrimitive) {
                val primitive = jsonObject.asJsonPrimitive
                if (primitive.isString) {
                    return DataValue.StringValue(primitive.asString)
                } else if (primitive.isNumber) {
                    return DataValue.IntValue(primitive.asInt)
                } else if (primitive.isBoolean) {
                    return DataValue.BooleanValue(primitive.asBoolean)
                }
            } else {
                return DataValue.StringValue("Unknown type of json element")
            }
        } catch (_: Exception) {
            return DataValue.StringValue(json.asString)
        }
        return DataValue.StringValue(json.asString)
    }
}