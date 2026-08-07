package com.wanbaohe.dynamicui.action

import com.wanbaohe.dynamicui.ir.ActionSpec
import com.wanbaohe.dynamicui.state.ValueExprResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActionEngine – dispatches [ActionSpec] events through the registered [ActionHandler] chain.
 *
 * ## Responsibility chain
 * Handlers are tried in insertion order. The first non-[ActionResult.Unhandled] wins.
 * If no handler matches, the error is written to internal error state.
 *
 * ## Action chaining
 * After a successful [ActionSpec.onSuccess] or on failure [ActionSpec.onError] sub-actions are
 * dispatched automatically, enabling http → setState → navigate chains without nesting config.
 *
 * ## Expression resolution in params
 * All param values containing `${…}` are resolved against the current [ActionContext.stateScope]
 * and [ActionContext.itemContext] before being passed to handlers. This lets action params
 * reference dynamic values: `{ "id": "${item.id}" }`.
 */
@Singleton
class ActionEngine @Inject constructor(
    private val registry: ActionRegistry,
) {
    data class DispatchResult(
        val action: ActionSpec,
        val handled: Boolean,
        val success: Boolean,
    )

    /**
     * Dispatch an action within a coroutine scope.
     * Safe to call from `onClick` lambdas in Composables.
     */
    fun dispatch(
        actionSpec: ActionSpec,
        context: ActionContext,
        scope: CoroutineScope,
    ) {
        scope.launch {
            dispatchSuspending(actionSpec, context)
        }
    }

    /** Suspending dispatch – call directly from coroutine contexts (ViewModel, LaunchedEffect). */
    suspend fun dispatchSuspending(actionSpec: ActionSpec, context: ActionContext): DispatchResult {
        // Resolve expression params before dispatch
        val resolvedSpec = resolveParams(actionSpec, context)
        val handlers = registry.handlersFor(resolvedSpec.type)
        var handled = false
        var success = false
        var errorMessage: String? = null
        for (handler in handlers) {
            when (val result = handler.handle(resolvedSpec, context)) {
                ActionResult.Unhandled -> Unit
                ActionResult.Success -> {
                    handled = true
                    success = true
                    break
                }
                is ActionResult.Error -> {
                    handled = true
                    success = false
                    errorMessage = result.message
                    break
                }
            }
        }
        if (handled && success) {
            resolvedSpec.onSuccess?.let { dispatchSuspending(it, context) }
        } else if (handled) {
            val message = errorMessage ?: "Action failed: ${resolvedSpec.type}"
            context.stateScope.setByPath(DynamicUiInternalState.ERROR, message)
            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_ERROR, message)
            resolvedSpec.onError?.let { dispatchSuspending(it, context) }
        } else {
            val message = "No handler for action type '${resolvedSpec.type}'"
            context.stateScope.setByPath(DynamicUiInternalState.ERROR, message)
            context.stateScope.setByPath(DynamicUiInternalState.LEGACY_ERROR, message)
            resolvedSpec.onError?.let { dispatchSuspending(it, context) }
        }
        return DispatchResult(action = resolvedSpec, handled = handled, success = success)
    }

    private fun resolveParams(spec: ActionSpec, ctx: ActionContext): ActionSpec {
        val resolved = spec.params.mapValues { (_, v) ->
            ValueExprResolver.resolveString(v, ctx.stateScope, ctx.itemContext)
        }
        return spec.copy(params = resolved)
    }
}

/**
 * ActionRegistry – maintains the ordered list of [ActionHandler] instances.
 * Supports both imperative registration and Hilt @IntoMap injection.
 */
@Singleton
class ActionRegistry @Inject constructor(
    // Hilt @IntoMap handlers injected here
    private val hiltHandlers: Map<String, @JvmSuppressWildcards ActionHandler> = emptyMap(),
) {
    private val customHandlers = mutableListOf<ActionHandler>()

    /** Register an ad-hoc handler at runtime. */
    fun register(handler: ActionHandler) {
        customHandlers.add(0, handler) // highest priority
    }

    /** Returns all handlers that support the given type, priority-ordered. */
    fun handlersFor(type: String): List<ActionHandler> {
        val results = mutableListOf<ActionHandler>()
        customHandlers.filterTo(results) { type in it.supportedTypes }
        hiltHandlers.values.filterTo(results) { type in it.supportedTypes }
        return results
    }
}
