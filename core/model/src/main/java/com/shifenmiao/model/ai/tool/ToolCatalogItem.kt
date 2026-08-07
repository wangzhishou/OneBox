package com.shifenmiao.model.ai.tool

/**
 * 工具目录项：
 * 负责描述工具是什么、适合什么场景和使用边界，
 * 不承担真正的执行逻辑。
 */
data class ToolCatalogItem(
    val name: String,
    val title: String,
    val summary: String,
    val description: String,
    val category: ToolCategory,
    val keywords: List<String>,
    val examples: List<String>,
    val dependencies: List<String> = emptyList(),
    val bootstrapModes: Set<ChatWorkingMode> = emptySet(),
    val visibleToUser: Boolean,
    val requiresConfirmation: Boolean,
    val isInteractive: Boolean,
    val riskLevel: ToolRiskLevel,
    val sortOrder: Int = 0,
    val version: Int = 1
)
