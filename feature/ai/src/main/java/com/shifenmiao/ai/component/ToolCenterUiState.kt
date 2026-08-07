package com.shifenmiao.ai.component

import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCatalogItem

/**
 * 工具中心面板状态：
 * 聚合会话选择和工具目录，供聊天页工具面板直接渲染。
 */
data class ToolCenterUiState(
    val isLoading: Boolean = false,
    val workingMode: ChatWorkingMode = ChatWorkingMode.PLAN,
    val allTools: List<ToolCatalogItem> = emptyList(),
    val bootstrapToolNames: List<String> = emptyList(),
    val enabledToolNames: List<String> = emptyList(),
    val systemToolNames: List<String> = emptyList(),
    val disabledSystemToolTitles: List<String> = emptyList()
)
