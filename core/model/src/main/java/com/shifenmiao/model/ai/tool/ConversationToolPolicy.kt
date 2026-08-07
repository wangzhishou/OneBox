package com.shifenmiao.model.ai.tool

/**
 * 会话级工具选择。
 *
 * workingMode 表示当前对话使用 Ask / Plan / Agent 中的哪种工作模式；
 * selectedToolNames 表示当前会话额外显式放开的工具集合。
 */
data class ConversationToolPolicy(
    val workingMode: ChatWorkingMode = ChatWorkingMode.AGENT,
    val selectedToolNames: List<String> = emptyList()
)
