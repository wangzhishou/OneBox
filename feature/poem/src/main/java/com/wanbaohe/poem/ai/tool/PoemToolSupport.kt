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

package com.wanbaohe.poem.ai.tool

import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.wanbaohe.poem.model.Poem
import org.json.JSONArray
import org.json.JSONObject

/**
 * 诗词工具的 deeplink 与成功结果(JSON)支撑(与 feature/ai 内部实现解耦,避免跨模块依赖)。
 *
 * 成功结果统一为 JSON:{"success":true, "markdown":..., "deepLinks":[...]}。
 *  - markdown 字段供 LLM 阅读;
 *  - deepLinks 数组由 AgentToolRegistry 原样合并,AI 聊天据此渲染跳转卡片。
 */

/** 诗词详情页 deeplink:onebox://screen/poem?poem_id=<id>(详情页从本地库加载,调用前须确保诗词已落库) */
internal fun poemDetailDeeplink(poemId: Long): String {
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = POEM_ROUTE_KEY,
        params = mapOf("poem_id" to poemId.toString()),
    )
}

/** 诗词搜索页 deeplink:onebox://screen/poem_search?q=<query> */
internal fun poemSearchDeeplink(query: String? = null): String {
    val params = query?.takeIf { it.isNotBlank() }?.let { mapOf("q" to it) }.orEmpty()
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = POEM_SEARCH_ROUTE_KEY,
        params = params,
    )
}

/** 诗词主页 deeplink:onebox://screen/poem */
internal fun poemHomeDeeplink(): String {
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = POEM_ROUTE_KEY,
    )
}

/** 一条结构化 deep link(键名与 ToolDeepLink 的 SerializedName 对齐) */
internal fun toolDeepLinkJson(
    uri: String,
    label: String,
    primary: Boolean = false,
): JSONObject = JSONObject().apply {
    put("uri", uri)
    put("label", label)
    put("primary", primary)
}

/** 成功结果:markdown(供 LLM)+ deepLinks(供 UI 跳转卡片),extraFields 可附加结构化字段 */
internal fun poemSuccessResult(
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

internal fun buildMarkdownLink(label: String, deeplink: String): String = "[$label]($deeplink)"

/** 去掉 markdown 特殊字符,防止诗词内容/用户输入破坏结果格式 */
internal fun sanitizeMarkdownText(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("\n", " ")
}

/** 列表用节选:取前两行(足以辨认诗词),控制结果长度 */
internal fun Poem.excerpt(maxLines: Int = 2): String =
    content.take(maxLines).joinToString(" ")

private const val POEM_ROUTE_KEY = "poem"
private const val POEM_SEARCH_ROUTE_KEY = "poem_search"
