package com.wanbaohe.dynamicui.action

import com.wanbaohe.dynamicui.ir.ActionSpec

/**
 * ActionHandler – the extension point for every type of side-effect.
 *
 * Implement this interface to add new action types. Register via [ActionRegistry]
 * (or Hilt @IntoMap for automatic discovery).
 *
 * [handle] returns [ActionResult]:
 * - [ActionResult.Success]: consumed and succeeded
 * - [ActionResult.Error]: consumed but failed (triggers onError chain)
 * - [ActionResult.Unhandled]: let next handler try
 */
interface ActionHandler {
    /** The action type strings this handler can process (e.g. ["navigate", "push"]). */
    val supportedTypes: Set<String>

    /** Execute the action and return an explicit dispatch result. */
    suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult
}

sealed interface ActionResult {
    data object Success : ActionResult
    data class Error(val message: String? = null) : ActionResult
    data object Unhandled : ActionResult
}
