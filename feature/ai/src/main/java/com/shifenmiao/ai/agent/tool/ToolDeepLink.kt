package com.shifenmiao.ai.agent.tool

import com.google.gson.annotations.SerializedName

/**
 * 工具执行结果里的 deep link 引导项。
 *
 * 工具在以下两种方式之一暴露 deep link：
 * 1. 静态声明：实现 [AgentTool.deepLinks] 返回固定列表（适合单一目标场景，如主题设置）。
 * 2. 动态注入：在 result JSON 的 `deepLinks` 数组里返回（适合带 id 等动态参数的目标，如打开/编辑某条笔记）。
 *
 * 框架在 [AgentToolRegistry.executeTool] 末尾把两种来源合并并写入 result.content，
 * 供 LLM 读取和 UI 渲染。
 *
 * @property uri        deep link URI，如 "onebox://screen/theme_settings" 或 "onebox://screen/note_detail?note_id=123"
 * @property label      用户可见的链接文本，如 "主题设置"、"打开笔记"
 * @property guidance   引导语，如 "点击查看并微调主题"，null 时 UI 使用默认模板
 * @property primary    是否为主要链接；UI 可对 primary=true 的项加视觉强调（icon 着色、边框等）
 */
data class ToolDeepLink(
    @SerializedName("uri") val uri: String,
    @SerializedName("label") val label: String,
    @SerializedName("guidance") val guidance: String? = null,
    @SerializedName("primary") val primary: Boolean = false,
)
