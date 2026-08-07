package com.shifenmiao.ai.execution.model

enum class AiExecutionPhase {
    HIDDEN,
    RUNNING,
    WAITING_USER_ACTION,
    WAITING_FINAL_RESPONSE,
    COMPLETED,
    FAILED,
    PAUSED
}

enum class ExecutionStepStatus {
    DONE,
    RUNNING,
    WAITING_USER,
    FAILED,
    PENDING
}

data class DeepLinkItemUiModel(
    val uri: String,
    val label: String,
    val guidance: String? = null,
    val primary: Boolean = false,
)

data class ExecutionStepUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val status: ExecutionStepStatus = ExecutionStepStatus.PENDING,
    val detail: String? = null,
    val debugInfo: String? = null,
    val isSystemStep: Boolean = false,
    val arguments: String? = null,
    val result: String? = null,
    val deepLinks: List<DeepLinkItemUiModel> = emptyList(),
)

data class AiExecutionUiModel(
    val phase: AiExecutionPhase = AiExecutionPhase.HIDDEN,
    val title: String = "",
    val summary: String? = null,
    val currentStepTitle: String? = null,
    val progressText: String? = null,
    val steps: List<ExecutionStepUiModel> = emptyList(),
    val primaryActionLabel: String? = null,
    val secondaryActionLabel: String? = null
) {
    val isVisible: Boolean
        get() = phase != AiExecutionPhase.HIDDEN && (title.isNotBlank() || steps.isNotEmpty())
}

