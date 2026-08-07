package com.shifenmiao.model.automation

/**
 * Result of executing an [AIAction].
 *
 * Promoted from feature/visual-automation/model/AutomationResult.kt to core/model
 * so it can be referenced from AgentTool results without a module-level dependency.
 */
sealed class AutomationResult {
    data class Success(val action: AIAction) : AutomationResult()
    data class Failure(val message: String) : AutomationResult()
}