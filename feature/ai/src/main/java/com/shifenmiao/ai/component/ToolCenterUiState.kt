package com.shifenmiao.ai.component

import com.shifenmiao.model.ai.tool.ToolCatalogItem

/**
 * 工具中心面板状态：
 * 聚合会话选择和工具目录，供聊天页工具面板直接渲染。
 */
data class ToolCenterUiState(
    val isLoading: Boolean = false,
    val allTools: List<ToolCatalogItem> = emptyList(),
    val bootstrapToolNames: List<String> = emptyList(),
    val enabledToolNames: List<String> = emptyList(),
    val systemToolNames: List<String> = emptyList(),
    val disabledSystemToolTitles: List<String> = emptyList(),
    /** 会话级记忆开关（仅会话表值，不含全局） */
    val memoryEnabled: Boolean = true,
    /** 会话级技能开关（仅会话表值，不含全局） */
    val skillsEnabled: Boolean = true,
    /** 全局记忆总开关（false 时会话行禁用展示） */
    val memoryGlobalEnabled: Boolean = true,
    /** 全局技能总开关（false 时会话行禁用展示） */
    val skillsGlobalEnabled: Boolean = true
)
