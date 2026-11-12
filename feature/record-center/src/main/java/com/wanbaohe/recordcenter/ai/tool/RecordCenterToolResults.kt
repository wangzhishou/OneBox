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

package com.wanbaohe.recordcenter.ai.tool

import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import org.json.JSONArray
import org.json.JSONObject

/**
 * 记录中心工具的 deeplink 与成功结果(JSON)支撑。
 *
 * 成功结果统一为 JSON:{"success":true, "markdown":..., "deepLinks":[...]}。
 *  - markdown 字段保留原有 markdown 输出(含跳转链接),供 LLM 阅读;
 *  - deepLinks 数组是结构化跳转项,AgentToolRegistry 会原样合并,
 *    AI 聊天据此渲染跳转卡片(纯 markdown 结果无法渲染卡片)。
 */

/** 记录列表页 deeplink:onebox://screen/record_center?type=list&record_type=<key> */
internal fun recordCenterListDeeplink(recordType: String? = null): String {
    val params = buildMap {
        put("type", "list")
        recordType?.takeIf { it.isNotBlank() }?.let { put("record_type", it) }
    }
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = AddHealthRecordTool.RECORD_CENTER_ROUTE_KEY,
        params = params,
    )
}

/** 一条结构化 deep link(键名与 ToolDeepLink 的 SerializedName 对齐) */
internal fun toolDeepLinkJson(
    uri: String,
    label: String,
    guidance: String? = null,
    primary: Boolean = false,
): JSONObject = JSONObject().apply {
    put("uri", uri)
    put("label", label)
    guidance?.takeIf { it.isNotBlank() }?.let { put("guidance", it) }
    put("primary", primary)
}

/** 成功结果:markdown(供 LLM)+ deepLinks(供 UI 跳转卡片),extraFields 可附加结构化字段 */
internal fun recordCenterSuccessResult(
    markdown: String,
    deepLinks: List<JSONObject>,
    extraFields: JSONObject.() -> Unit = {},
): AgentToolResult {
    val json = JSONObject().apply {
        put("success", true)
        put("markdown", markdown)
        if (deepLinks.isNotEmpty()) {
            put("deepLinks", JSONArray().apply { deepLinks.forEach { put(it) } })
        }
        extraFields()
    }
    return AgentToolResult(content = json.toString())
}
