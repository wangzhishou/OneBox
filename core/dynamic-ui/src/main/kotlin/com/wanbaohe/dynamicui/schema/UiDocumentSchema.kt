package com.wanbaohe.dynamicui.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 新协议文档结构。
 *
 * 约束：
 * - 框架元字段位于节点外层
 * - 组件参数统一位于 `props`
 * - 默认子内容统一位于 `children`
 * - 事件统一位于 `actions`
 */
@Serializable
data class UiDocumentSchema(
    val dataContext: JsonObject? = null,
    val root: UiNodeSchema,
)

/**
 * 通用节点结构。
 */
@Serializable
data class UiNodeSchema(
    val type: String,
    val id: String? = null,
    val prompt: String? = null,
    @SerialName("if")
    val visibleExpr: String? = null,
    val meta: JsonObject? = null,
    val props: JsonObject? = null,
    val children: List<UiNodeSchema>? = null,
    val listConfig: ListConfigSchema? = null,
    val actions: Map<String, ActionSpecSchema>? = null,
)

@Serializable
data class ListConfigSchema(
    val dataSource: String,
    val itemKey: String? = null,
    val itemTemplate: UiNodeSchema,
    val columns: Int = 1,
)

@Serializable
data class ActionSpecSchema(
    val type: String,
    val params: Map<String, JsonElement>? = null,
    val onSuccess: ActionSpecSchema? = null,
    val onError: ActionSpecSchema? = null,
    val onConfirm: ActionSpecSchema? = null,
)
