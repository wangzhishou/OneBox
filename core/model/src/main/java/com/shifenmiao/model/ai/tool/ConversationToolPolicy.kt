package com.shifenmiao.model.ai.tool

import kotlinx.serialization.Serializable

/**
 * 会话级工具选择。
 *
 * selectedToolNames 表示当前会话额外显式放开的工具集合。
 */
@Serializable
data class ConversationToolPolicy(
    val selectedToolNames: List<String> = emptyList()
)
