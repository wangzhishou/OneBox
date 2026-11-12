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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容的工具定义，用于 ChatCompletionRequest.tools 字段。
 * 发送给 LLM 告知其可调用哪些本地工具。
 */
@Parcelize
@Serializable
data class ToolDefinition(
    @SerialName("type")
    val type: String = "function",
    @SerialName("function")
    val function: ToolFunctionDef
) : Parcelable

/**
 * 工具函数定义：名称、描述、参数 JSON Schema。
 */
@Parcelize
@Serializable
data class ToolFunctionDef(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("parameters")
    val parameters: ToolParameters? = null
) : Parcelable

/**
 * 工具参数 JSON Schema（简化版本，满足大多数场景）。
 */
@Parcelize
@Serializable
data class ToolParameters(
    @SerialName("type")
    val type: String = "object",
    @SerialName("properties")
    val properties: Map<String, ToolParameterProperty> = emptyMap(),
    @SerialName("required")
    val required: List<String> = emptyList()
) : Parcelable

/**
 * 单个参数属性定义。
 *
 * 兼容 JSON Schema 子集：
 * - `type`: 基础类型（"string" / "number" / "integer" / "boolean" / "object" / "array"）
 * - `description`: 字段说明
 * - `enum`: 字符串枚举
 * - `properties`: 对象类型的子字段定义（仅 type="object" 时使用）
 * - `required`: 对象类型必填字段列表（仅 type="object" 时使用）
 * - `items`: 数组元素 schema（仅 type="array" 时使用）
 *
 * 未覆盖 JSON Schema 的全部能力（$ref / allOf / additionalProperties 等），
 * 当前 LLM 工具定义够用即可。
 */
@Parcelize
@Serializable
data class ToolParameterProperty(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("enum")
    val enum: List<String>? = null,
    @SerialName("properties")
    val properties: Map<String, ToolParameterProperty>? = null,
    @SerialName("required")
    val required: List<String>? = null,
    @SerialName("items")
    val items: ToolParameterProperty? = null
) : Parcelable

/**
 * 流式返回中 delta 内的 tool_calls 增量碎片。
 * 每个 chunk 可能只返回 id/name 的一部分或 arguments 的一段。
 */
@Parcelize
@Serializable
data class ToolCallDelta(
    @SerialName("index")
    val index: Int = 0,
    @SerialName("id")
    val id: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("function")
    val function: FunctionCallDelta? = null
) : Parcelable

/**
 * 流式返回中函数调用的增量碎片。
 */
@Parcelize
@Serializable
data class FunctionCallDelta(
    @SerialName("name")
    val name: String? = null,
    @SerialName("arguments")
    val arguments: String? = null
) : Parcelable
