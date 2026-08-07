package com.shifenmiao.ai.agent.tool

data class ToolConfirmationRequest(
    val toolCallId: String,
    val toolName: String,
    val interactionOwnerId: String? = null,
    val dialogTitle: String = toolName,
    val dialogMessage: String = "",
    // 按钮文案留空时由 UI 层回退到本地化默认文案（继续/拒绝），不要在这里写死中文
    val submitButtonText: String = "",
    val cancelButtonText: String = "",
    val confirmPayload: String? = null,
    val dismissPayload: String? = null
)
