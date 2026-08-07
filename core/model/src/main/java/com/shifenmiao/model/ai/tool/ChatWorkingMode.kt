package com.shifenmiao.model.ai.tool

/**
 * 聊天工作模式：
 * - ASK: 直接回答
 * - PLAN: 调研与规划，不直接执行
 * - AGENT: 允许工具驱动执行
 */
enum class ChatWorkingMode {
    ASK,
    PLAN,
    AGENT,
}

