package com.wanbaohe.dynamicui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * ValueExprResolver – resolves expression strings to concrete values at render time.
 *
 * ## Expression syntax
 * | Expression                  | Resolved to                                      |
 * |-----------------------------|--------------------------------------------------|
 * | `"Hello"`                   | Literal string "Hello"                           |
 * | `"${state.title}"`          | `scope["title"]`                                 |
 * | `"${state.user.name}"`      | `scope["user"]` navigated to `.name`             |
 * | `"${item.id}"`              | `itemContext["id"]` (inside ForEach/LazyColumn)  |
 * | `"${env.appName}"`          | `envContext["appName"]`                          |
 * | `"!${state.loading}"`       | Negation of bool value                           |
 * | `"${state.count} > 0"`      | Comparison expression → Boolean                  |
 *
 * ## Compose integration
 * All resolution via [resolve] inside a @Composable is wrapped in [derivedStateOf] so that
 * only nodes whose specific state keys change will recompose.
 */
@Stable
object ValueExprResolver {

    // Full-match: "${state.key}" or "${item.key}"
    private val EXPR_REGEX = Regex("""^\$\{([^}]+)\}$""")
    // Full-match negated: "!${state.key}"
    private val NEGATED_EXPR_REGEX = Regex("""^!\$\{([^}]+)\}$""")
    // Comparison expression: "${state.x} > 0", "true == false", etc.
    private val COMPARISON_REGEX = Regex("""^(.+?)\s*(==|!=|>=|<=|>|<)\s*(.+)$""")
    // Embedded tokens inside a string: "Hello ${state.name}!"
    private val EMBEDDED_EXPR_REGEX = Regex("""\$\{([^}]+)\}""")

    // ── Optimized resolution (bypassing Regex for common cases) ─────────────
    
    fun resolve(
        expr: String?,
        scope: UiStateScope,
        itemContext: Map<String, Any?> = emptyMap(),
        envContext: Map<String, Any?> = emptyMap(),
    ): Any? {
        if (expr == null) return null

        val trimmed = expr.trim()
        
        // Fast path 1: direct state expression "${state.key}" or "${item.key}"
        if (trimmed.startsWith("\${") && trimmed.endsWith("}") && trimmed.indexOf("\${", 2) == -1) {
            val path = trimmed.substring(2, trimmed.length - 1).trim()
            if (!path.contains(" ") && !path.contains("==") && !path.contains(">") && !path.contains("<")) {
                return resolvePath(path, scope, itemContext, envContext)
            }
        }

        // Fast path 2: negated direct expression "!${state.key}"
        if (trimmed.startsWith("!\${") && trimmed.endsWith("}") && trimmed.indexOf("\${", 3) == -1) {
            val path = trimmed.substring(3, trimmed.length - 1).trim()
            if (!path.contains(" ") && !path.contains("==") && !path.contains(">") && !path.contains("<")) {
                val value = resolvePath(path, scope, itemContext, envContext)
                return !isTruthy(value)
            }
        }

        // Slow path: Comparison or Embedded expressions
        val expanded = expandEmbedded(trimmed, scope, itemContext, envContext)
        COMPARISON_REGEX.find(expanded)?.let { match ->
            return evalComparison(
                match.groupValues[1].trim(),
                match.groupValues[2].trim(),
                match.groupValues[3].trim(),
            )
        }

        return expanded
    }

    /** Convenience overload returning a String (coerces nulls to ""). */
    fun resolveString(
        expr: String?,
        scope: UiStateScope,
        itemContext: Map<String, Any?> = emptyMap(),
        envContext: Map<String, Any?> = emptyMap(),
    ): String = resolve(expr, scope, itemContext, envContext)?.toString() ?: ""

    /** Convenience overload returning Boolean (coerces to false if not resolvable). */
    fun resolveBool(
        expr: String?,
        scope: UiStateScope,
        itemContext: Map<String, Any?> = emptyMap(),
        envContext: Map<String, Any?> = emptyMap(),
    ): Boolean = isTruthy(resolve(expr, scope, itemContext, envContext))

    // ── Internal path navigation ───────────────────────────────────────────────

    private fun resolvePath(
        path: String,
        scope: UiStateScope,
        itemContext: Map<String, Any?>,
        envContext: Map<String, Any?>,
    ): Any? {
        val segments = path.split(".")
        val namespace = segments.firstOrNull() ?: return null
        val tail = segments.drop(1)

        return when (namespace) {
            "state" -> if (tail.isEmpty()) scope.snapshot() else scope.getByPath(tail.joinToString("."))
            "item" -> if (tail.isEmpty()) itemContext else navigateTail(itemContext, tail)
            "env" -> if (tail.isEmpty()) envContext else navigateTail(envContext, tail)
            else -> scope.getByPath(path)  // no namespace prefix → treat as direct state path
        }
    }

    /** Navigate a value chain like `user → name` through nested Maps. */
    @Suppress("UNCHECKED_CAST")
    fun navigateTail(root: Any?, segments: List<String>): Any? {
        var current: Any? = root
        for (seg in segments) {
            current = when (current) {
                is Map<*, *> -> (current as Map<String, Any?>)[seg]
                is List<*> -> seg.toIntOrNull()?.let { current.getOrNull(it) }
                else -> null
            }
        }
        return current
    }

    /** Expand all `${…}` tokens inside a multi-token string. */
    private fun expandEmbedded(
        expr: String,
        scope: UiStateScope,
        itemContext: Map<String, Any?>,
        envContext: Map<String, Any?>,
    ): String {
        return EMBEDDED_EXPR_REGEX.replace(expr) { match ->
            resolvePath(match.groupValues[1], scope, itemContext, envContext)?.toString() ?: ""
        }
    }

    private fun evalComparison(left: String, op: String, right: String): Boolean {
        val l = left.toDoubleOrNull()
        val r = right.toDoubleOrNull()
        return if (l != null && r != null) {
            when (op) {
                "==" -> l == r
                "!=" -> l != r
                ">" -> l > r
                ">=" -> l >= r
                "<" -> l < r
                "<=" -> l <= r
                else -> false
            }
        } else {
            when (op) {
                "==" -> left == right
                "!=" -> left != right
                else -> false
            }
        }
    }

    fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is String -> value.isNotEmpty() && value != "false" && value != "0"
        is Number -> value.toDouble() != 0.0
        is Collection<*> -> value.isNotEmpty()
        else -> true
    }
}

// ── Compose-friendly extension ─────────────────────────────────────────────────

/**
 * Reads [expr] from [scope] inside a [derivedStateOf], ensuring this node only
 * recomposes when the specific state key(s) referenced by the expression change.
 *
 * If [expr] is not a String (e.g. a pre-typed value from `Map<String, Any?>` props),
 * returns it directly without expression resolution.
 */
@Composable
fun rememberResolvedValue(
    expr: Any?,
    scope: UiStateScope,
    itemContext: Map<String, Any?> = emptyMap(),
): Any? {
    if (expr !is String?) return expr
    val resolved by remember(expr, itemContext) {
        derivedStateOf { ValueExprResolver.resolve(expr, scope, itemContext) }
    }
    return resolved
}

@Composable
fun rememberResolvedString(
    expr: Any?,
    scope: UiStateScope,
    itemContext: Map<String, Any?> = emptyMap(),
): String {
    if (expr !is String?) return expr.toString()
    val resolved by remember(expr, itemContext) {
        derivedStateOf { ValueExprResolver.resolveString(expr, scope, itemContext) }
    }
    return resolved
}

@Composable
fun rememberResolvedBool(
    expr: Any?,
    scope: UiStateScope,
    itemContext: Map<String, Any?> = emptyMap(),
): Boolean {
    if (expr !is String?) return ValueExprResolver.isTruthy(expr)
    val resolved by remember(expr, itemContext) {
        derivedStateOf { ValueExprResolver.resolveBool(expr, scope, itemContext) }
    }
    return resolved
}
