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
