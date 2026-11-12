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

package com.shifenmiao.model.deserializer

import com.shifenmiao.model.DataValue
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
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put

/**
 * kotlinx 版 DataValueTypeAdapter(Gson 清零,阶段⑤b):
 * Strapi error.details 的多态包装,写出与原 adapter 相同的 {"type","value"} 形状;
 * 读取行为对齐原实现 —— 对象一律 MapValue(含包装形态,原实现如此),
 * 字符串/数字/布尔按裸值还原,数组按容错退化为 StringValue(原实现为抛异常,见阶段⑤b 报告)。
 */
object DataValueKSerializer : KSerializer<DataValue> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DataValue")

    override fun serialize(encoder: Encoder, value: DataValue) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("DataValue 仅支持 JSON 编码")
        jsonEncoder.encodeJsonElement(
            buildJsonObject {
                when (value) {
                    is DataValue.StringValue -> {
                        put("type", "StringValue")
                        put("value", value.value)
                    }
                    is DataValue.IntValue -> {
                        put("type", "IntValue")
                        put("value", value.value)
                    }
                    is DataValue.BooleanValue -> {
                        put("type", "BooleanValue")
                        put("value", value.value)
                    }
                    is DataValue.StringListValue -> {
                        put("type", "StringListValue")
                        put("value", JsonArray(value.value.map { JsonPrimitive(it) }))
                    }
                    is DataValue.MapValue -> {
                        put("type", "MapValue")
                        put("value", value.value.toJson())
                    }
                }
            }
        )
    }

    override fun deserialize(decoder: Decoder): DataValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("DataValue 仅支持 JSON 解码")
        return jsonDecoder.decodeJsonElement().toDataValue()
    }

    private fun JsonElement.toDataValue(): DataValue = when (this) {
        is JsonObject -> DataValue.MapValue(mapValues { it.value.toDataValue() })
        is JsonPrimitive -> when {
            isString -> DataValue.StringValue(content)
            booleanOrNull != null -> DataValue.BooleanValue(content.toBoolean())
            doubleOrNull != null -> DataValue.IntValue(content.toDouble().toInt())
            else -> DataValue.StringValue(content)
        }
        // 原 Gson 实现对裸数组实际为抛异常;此处容错退化为文本
        is JsonArray -> DataValue.StringValue(toString())
    }

    private fun Map<String, DataValue>.toJson(): JsonObject = buildJsonObject {
        entries.forEach { (key, value) -> put(key, value.toJsonElement()) }
    }

    private fun DataValue.toJsonElement(): JsonElement = when (this) {
        is DataValue.StringValue -> JsonPrimitive(value)
        is DataValue.IntValue -> JsonPrimitive(value)
        is DataValue.BooleanValue -> JsonPrimitive(value)
        is DataValue.StringListValue -> JsonArray(value.map { JsonPrimitive(it) })
        is DataValue.MapValue -> value.toJson()
    }
}
