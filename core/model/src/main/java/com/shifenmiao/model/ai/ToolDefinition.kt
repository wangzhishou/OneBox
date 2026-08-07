package com.shifenmiao.model.ai

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容的工具定义，用于 ChatCompletionRequest.tools 字段。
 * 发送给 LLM 告知其可调用哪些本地工具。
 */
@Parcelize
@Serializable
data class ToolDefinition(
    @SerializedName("type")
    val type: String = "function",
    @SerializedName("function")
    val function: ToolFunctionDef
) : Parcelable

/**
 * 工具函数定义：名称、描述、参数 JSON Schema。
 */
@Parcelize
@Serializable
data class ToolFunctionDef(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("parameters")
    val parameters: ToolParameters? = null
) : Parcelable

/**
 * 工具参数 JSON Schema（简化版本，满足大多数场景）。
 */
@Parcelize
@Serializable
data class ToolParameters(
    @SerializedName("type")
    val type: String = "object",
    @SerializedName("properties")
    val properties: Map<String, ToolParameterProperty> = emptyMap(),
    @SerializedName("required")
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
    @SerializedName("type")
    val type: String,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("enum")
    val enum: List<String>? = null,
    @SerializedName("properties")
    val properties: Map<String, ToolParameterProperty>? = null,
    @SerializedName("required")
    val required: List<String>? = null,
    @SerializedName("items")
    val items: ToolParameterProperty? = null
) : Parcelable

/**
 * 流式返回中 delta 内的 tool_calls 增量碎片。
 * 每个 chunk 可能只返回 id/name 的一部分或 arguments 的一段。
 */
@Parcelize
@Serializable
data class ToolCallDelta(
    @SerializedName("index")
    val index: Int = 0,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("function")
    val function: FunctionCallDelta? = null
) : Parcelable

/**
 * 流式返回中函数调用的增量碎片。
 */
@Parcelize
@Serializable
data class FunctionCallDelta(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("arguments")
    val arguments: String? = null
) : Parcelable
