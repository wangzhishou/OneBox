package com.shifenmiao.model.ai

/**
 * 流式问答页（Screen.AIStreamAnswer）的回答缓存策略。
 *
 * 缓存以「问题 + systemPrompt + 引擎/模型」为内容键，复用 message 表中已持久化的回答：
 * 命中时直接展示历史回答，不再发起网络请求。
 */
enum class StreamAnswerCachePolicy {
    /** 不缓存，每次进入都重新请求（默认，保持现状） */
    NONE,

    /** 同内容永久命中；问题/引擎/模型/prompt 任一变化即自动失效 */
    PERMANENT,

    /** 仅当天内同内容命中，跨天重新生成（适合"每日"类场景） */
    TODAY,
}
