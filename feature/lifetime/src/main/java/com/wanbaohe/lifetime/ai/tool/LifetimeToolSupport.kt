package com.wanbaohe.lifetime.ai.tool

import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.wanbaohe.core.ui.utils.navigation.Screen
import org.json.JSONArray
import org.json.JSONObject

/**
 * 人生时间线工具的 deeplink 与成功结果(JSON)支撑(与 feature/ai 内部实现解耦,避免跨模块依赖)。
 *
 * 成功结果统一为 JSON:{"success":true, "markdown":..., "deepLinks":[...]}。
 *  - markdown 字段供 LLM 阅读;
 *  - deepLinks 数组由 AgentToolRegistry 原样合并,AI 聊天据此渲染跳转卡片。
 */

/** 人生时间线主页 deeplink(Screen.LifeTime 由 AppNavigationRegistry 静态注册,无需额外登记) */
internal fun lifetimeScreenDeeplink(): String {
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = Screen.LifeTime.routeKey,
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
internal fun lifetimeSuccessResult(
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

/** 去掉 markdown 特殊字符,防止用户输入破坏结果格式 */
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
