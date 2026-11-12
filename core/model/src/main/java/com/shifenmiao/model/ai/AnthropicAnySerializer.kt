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

package com.shifenmiao.model.ai

import com.shifenmiao.model.toJsonElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Anthropic 模型里 `@RawValue Any` 字段的双向 serializer(替代 Gson 的运行时反射序列化)。
 *
 * 编码:按运行时类型写出,String/Number/Boolean → 裸字面量;List<ContentBlock> → 数组
 * (元素必须走 ContentBlock 自己的 serializer,不能走 JsonMaps.toJsonElement —
 *  它不认 @Serializable data class);Map → 对象。
 * 解码:统一解成 JsonElement。响应侧 SSE 帧(AnthropicStreamEvent → ContentBlock.input)
 * 也会命中本 serializer,不能只实现编码。
 */
object AnthropicAnySerializer : KSerializer<Any> {

    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("AnthropicAnySerializer 仅支持 JSON 编码")
        jsonEncoder.encodeJsonElement(value.toAnthropicJson(jsonEncoder))
    }

    override fun deserialize(decoder: Decoder): Any {
        // SSE 响应侧也会命中本 serializer:content_block_start 的 tool_use block
        // 固定携带 "input":{}。此处若抛异常,整行事件会被当作畸形行丢弃,
        // tool_use 起点丢失 → content_block_stop 不再发射 tool_call chunk →
        // Agent 工具链静默中断。解码为 JsonElement 即可,流解析只读 id/name/type。
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("AnthropicAnySerializer 仅支持 JSON 解码")
        return jsonDecoder.decodeJsonElement()
    }

    private fun Any?.toAnthropicJson(encoder: JsonEncoder): JsonElement = when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is ContentBlock -> encoder.json.encodeToJsonElement(ContentBlock.serializer(), this)
        is List<*> -> JsonArray(map { it.toAnthropicJson(encoder) })
        is Map<*, *> -> JsonObject(
            entries.associate { (key, v) -> key.toString() to v.toAnthropicJson(encoder) }
        )
        else -> toJsonElement()
    }
}
