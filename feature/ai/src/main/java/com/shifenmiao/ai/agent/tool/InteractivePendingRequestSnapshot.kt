package com.shifenmiao.ai.agent.tool

data class InteractivePendingRequestSnapshot(
    val kind: String = "",
    val confirmationRequest: ToolConfirmationRequest? = null,
    val questionRequest: AgentUserQuestionRequest? = null
) {
    companion object {
        const val KIND_CONFIRMATION = "confirmation"
        const val KIND_QUESTION = "question"
    }
}

data class RestoredInteractiveRequestResult(
    val kind: String,
    val confirmationRequest: ToolConfirmationRequest? = null,
    val questionRequest: AgentUserQuestionRequest? = null,
    val payload: String? = null
)
