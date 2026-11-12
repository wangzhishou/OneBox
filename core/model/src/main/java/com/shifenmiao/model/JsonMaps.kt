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

package com.shifenmiao.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Gson → kotlinx 迁移(阶段②a):`gson.toJson(any)` 的容器版替代。
 *
 * Agent 工具结果大量是 `mapOf(...)` / `linkedMapOf(...)` 字面量,kotlinx 没有
 * `Map<String, Any?>` 的序列化器,这里递归转成 [JsonElement] 再输出紧凑 JSON。
 * 行为对齐 Gson 默认配置(serializeNulls=false):Map 中的 null 值整个 key 省略;
 * 集合/数组中的 null 元素保留为 JSON null。
 *
 * @Serializable 的具名类型请直接用 `ModelProvider.AppJson.encodeToString(value)`。
 */
fun jsonStringOf(value: Any?): String = value.toJsonElement().toString()

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Enum<*> -> JsonPrimitive(name)
    is Map<*, *> -> JsonObject(
        entries.mapNotNull { (key, value) ->
            val mapKey = key?.toString() ?: return@mapNotNull null
            // Gson 默认省略 Map 中的 null 值
            if (value == null) null else mapKey to value.toJsonElement()
        }.toMap()
    )
    is Iterable<*> -> JsonArray(map { it.toJsonElement() })
    is Array<*> -> JsonArray(map { it.toJsonElement() })
    is IntArray -> JsonArray(map { JsonPrimitive(it) })
    is LongArray -> JsonArray(map { JsonPrimitive(it) })
    is DoubleArray -> JsonArray(map { JsonPrimitive(it) })
    is FloatArray -> JsonArray(map { JsonPrimitive(it) })
    is BooleanArray -> JsonArray(map { JsonPrimitive(it) })
    else -> throw IllegalArgumentException(
        "jsonStringOf 不支持的类型 ${this::class.java.name},@Serializable 类型请用 AppJson.encodeToString"
    )
}

/**
 * Gson `fromJson(json, Map::class.java)` 的松散解析替代:把 JSON 对象解成
 * `Map<String, Any?>`,数字统一为 Double(与 Gson LinkedTreeMap 行为一致,
 * 调用方多为 `as? Number` / `as? String`)。解析失败返回空 Map。
 */
fun parseLooseJsonObject(json: String): Map<String, Any?> {
    if (json.isBlank()) return emptyMap()
    return runCatching {
        (ModelProvider.AppJson.parseToJsonElement(json) as? JsonObject)
            ?.mapValues { it.value.unwrapJsonValue() }
            .orEmpty()
    }.getOrElse { emptyMap() }
}

private fun JsonElement.unwrapJsonValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonObject -> mapValues { it.value.unwrapJsonValue() }
    is JsonArray -> map { it.unwrapJsonValue() }
    is JsonPrimitive -> when {
        isString -> content
        booleanOrNull != null -> booleanOrNull
        doubleOrNull != null -> doubleOrNull
        else -> content
    }
}
