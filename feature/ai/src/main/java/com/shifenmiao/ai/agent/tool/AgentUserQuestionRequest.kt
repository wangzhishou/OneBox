package com.shifenmiao.ai.agent.tool

data class AgentUserQuestionRequest(
    val toolCallId: String = "",
    val toolName: String = "",
    val presentation: AgentUserQuestionPresentation = AgentUserQuestionPresentation.dialog,
    val title: String = "",
    val message: String = "",
    val questions: List<AgentUserQuestionItem> = emptyList(),
    val interactionOwnerId: String? = null,
    // 按钮文案留空时由 UI 层回退到本地化默认文案（继续/取消），不要在这里写死中文
    val confirmText: String = "",
    val cancelText: String = ""
)
