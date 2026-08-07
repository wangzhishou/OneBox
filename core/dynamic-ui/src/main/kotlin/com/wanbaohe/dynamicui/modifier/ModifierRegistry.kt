package com.wanbaohe.dynamicui.modifier

import android.util.LruCache
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ModifierRegistry – maps modifier expression strings to Compose Modifier lambdas.
 *
 * ## Expression format
 * Each entry in the JSON `modifier` array is a string that mirrors the Compose Modifier API:
 * ```
 * "fillMaxSize"
 * "padding(16dp)"
 * "padding(16dp, 8dp)"          → horizontal=16dp, vertical=8dp
 * "padding(start=8dp, top=4dp)" → named args
 * "size(48dp)"
 * "size(100dp, 48dp)"
 * "background(#FF5733)"
 * "background(#FF5733, RoundedCornerShape(8dp))"
 * "clip(CircleShape)"
 * "border(2dp, #333333)"
 * "border(2dp, #333333, RoundedCornerShape(8dp))"
 * "alpha(0.5)"
 * "shadow(4dp)"
 * "zIndex(1)"
 * "offset(x=8dp, y=0dp)"
 * "aspectRatio(1)"
 * "weight(1)"         → used by Row/Column children (applied in RowScope/ColumnScope)
 * ```
 */
@Singleton
class ModifierRegistry @Inject constructor() {

    private val handlers = mutableMapOf<String, (List<String>) -> Modifier>()

    /** Cache parsed expression tokens: expression string → (name, args). */
    private val parseExprCache = LruCache<String, Pair<String, List<String>>>(256)

    init {
        registerBuiltins()
    }

    /**
     * Register a custom modifier handler.
     * @param name Expression name without parentheses (e.g. "shadow", "blur")
     * @param handler Lambda receiving tokenized args and returning a Modifier
     */
    fun register(name: String, handler: (args: List<String>) -> Modifier) {
        handlers[name] = handler
    }

    /**
     * Parse a single modifier expression string into a Compose [Modifier].
     * Returns [Modifier] (no-op) if the expression is unknown.
     */
    fun parse(expr: String): Modifier {
        val (name, args) = parseExprCached(expr)
        val handler = handlers[name] ?: return Modifier
        return runCatching { handler(args) }.getOrDefault(Modifier)
    }

    private fun parseExprCached(expr: String): Pair<String, List<String>> {
        parseExprCache.get(expr)?.let { return it }
        val result = parseExpr(expr)
        parseExprCache.put(expr, result)
        return result
    }

    private fun parseExpr(expr: String): Pair<String, List<String>> {
        val trimmed = expr.trim()
        val parenIdx = trimmed.indexOf('(')
        if (parenIdx == -1) return trimmed to emptyList()
        val name = trimmed.substring(0, parenIdx).trim()
        val argStr = trimmed.substring(parenIdx + 1, trimmed.lastIndexOf(')')).trim()
        val args = if (argStr.isEmpty()) emptyList()
        else splitArgs(argStr).map { it.trim() }
        return name to args
    }

    /** Split args by commas but respect nested parentheses (e.g. RoundedCornerShape(8dp)). */
    private fun splitArgs(args: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var start = 0
        for (i in args.indices) {
            when (args[i]) {
                '(' -> depth++
                ')' -> depth--
                ',' -> if (depth == 0) {
                    result.add(args.substring(start, i))
                    start = i + 1
                }
            }
        }
        result.add(args.substring(start))
        return result
    }

    private fun registerBuiltins() {
        // ── Size ────────────────────────────────────────────────────────────
        handlers["fillMaxSize"] = { _ -> Modifier.fillMaxSize() }
        handlers["fillMaxWidth"] = { _ -> Modifier.fillMaxWidth() }
        handlers["fillMaxHeight"] = { _ -> Modifier.fillMaxHeight() }
        handlers["wrapContentSize"] = { _ -> Modifier.wrapContentSize() }
        handlers["wrapContentWidth"] = { _ -> Modifier.wrapContentWidth() }
        handlers["wrapContentHeight"] = { _ -> Modifier.wrapContentHeight() }

        handlers["size"] = { args ->
            val w = parseDp(args.getOrNull(0)) ?: 0.dp
            val h = parseDp(args.getOrNull(1)) ?: w
            Modifier.size(w, h)
        }
        handlers["width"] = { args -> Modifier.width(parseDp(args.getOrNull(0)) ?: 0.dp) }
        handlers["height"] = { args -> Modifier.height(parseDp(args.getOrNull(0)) ?: 0.dp) }
        handlers["aspectRatio"] = { args ->
            val ratio = args.getOrNull(0)?.toFloatOrNull() ?: 1f
            Modifier.aspectRatio(ratio)
        }

        // ── Padding ─────────────────────────────────────────────────────────
        handlers["padding"] = { args ->
            val named = args.associate { a ->
                if ('=' in a) a.substringBefore('=').trim() to a.substringAfter('=').trim()
                else "all" to a
            }
            when {
                named.containsKey("start") || named.containsKey("top") ||
                        named.containsKey("end") || named.containsKey("bottom") -> {
                    Modifier.padding(
                        start = parseDp(named["start"]) ?: 0.dp,
                        top = parseDp(named["top"]) ?: 0.dp,
                        end = parseDp(named["end"]) ?: 0.dp,
                        bottom = parseDp(named["bottom"]) ?: 0.dp,
                    )
                }
                named.containsKey("horizontal") || named.containsKey("vertical") -> {
                    Modifier.padding(
                        horizontal = parseDp(named["horizontal"]) ?: 0.dp,
                        vertical = parseDp(named["vertical"]) ?: 0.dp,
                    )
                }
                args.size == 1 -> Modifier.padding(all = parseDp(args[0]) ?: 0.dp)
                args.size == 2 -> Modifier.padding(
                    horizontal = parseDp(args[0]) ?: 0.dp,
                    vertical = parseDp(args[1]) ?: 0.dp,
                )
                args.size >= 4 -> Modifier.padding(
                    start = parseDp(args[0]) ?: 0.dp,
                    top = parseDp(args[1]) ?: 0.dp,
                    end = parseDp(args[2]) ?: 0.dp,
                    bottom = parseDp(args[3]) ?: 0.dp,
                )
                else -> Modifier
            }
        }

        // ── Background & clip ───────────────────────────────────────────────
        handlers["background"] = { args ->
            val color = parseColor(args.getOrNull(0))
            val shape = parseShape(args.getOrNull(1))
            if (color != null && shape != null) Modifier.background(color, shape)
            else if (color != null) Modifier.background(color)
            else Modifier
        }
        handlers["clip"] = { args ->
            val shape = parseShape(args.getOrNull(0))
            if (shape != null) Modifier.clip(shape) else Modifier
        }

        // ── Border ──────────────────────────────────────────────────────────
        handlers["border"] = { args ->
            val width = parseDp(args.getOrNull(0)) ?: 1.dp
            val color = parseColor(args.getOrNull(1)) ?: Color.Gray
            val shape = parseShape(args.getOrNull(2))
            if (shape != null) Modifier.border(width, color, shape)
            else Modifier.border(width, color)
        }

        // ── Shadow ──────────────────────────────────────────────────────────
        handlers["shadow"] = { args ->
            val elevation = parseDp(args.getOrNull(0)) ?: 0.dp
            val shape = parseShape(args.getOrNull(1))
            if (shape != null) Modifier.shadow(elevation, shape)
            else Modifier.shadow(elevation)
        }

        // ── Alpha / zIndex / offset ─────────────────────────────────────────
        handlers["alpha"] = { args -> Modifier.alpha(args.getOrNull(0)?.toFloatOrNull() ?: 1f) }
        handlers["zIndex"] = { args -> Modifier.zIndex(args.getOrNull(0)?.toFloatOrNull() ?: 0f) }
        handlers["offset"] = { args ->
            val named = args.associate { a ->
                if ('=' in a) a.substringBefore('=').trim() to a.substringAfter('=').trim()
                else "x" to a
            }
            Modifier.offset(
                x = parseDp(named["x"] ?: args.getOrNull(0)) ?: 0.dp,
                y = parseDp(named["y"] ?: args.getOrNull(1)) ?: 0.dp,
            )
        }

        // ── Clickable (no-op here; action wiring is done by the renderer) ──
        // We register it so it doesn't emit an "unknown modifier" warning.
        handlers["clickable"] = { _ -> Modifier }
    }

    // ── Value parsers ──────────────────────────────────────────────────────────

    companion object {
        fun parseDp(raw: String?): androidx.compose.ui.unit.Dp? {
            raw ?: return null
            val s = raw.trim()
            return when {
                s.endsWith("dp") -> s.dropLast(2).trim().toFloatOrNull()?.dp
                s.endsWith("sp") -> s.dropLast(2).trim().toFloatOrNull()?.dp // approx
                else -> s.toFloatOrNull()?.dp
            }
        }

        fun parseColor(raw: String?): Color? {
            raw ?: return null
            val s = raw.trim()
            return when {
                s.startsWith("#") -> runCatching {
                    Color(android.graphics.Color.parseColor(s))
                }.getOrNull()
                s.startsWith("0x", ignoreCase = true) -> runCatching {
                    Color(s.substring(2).toLong(16).toInt())
                }.getOrNull()
                s.equals("transparent", ignoreCase = true) -> Color.Transparent
                s.equals("white", ignoreCase = true) -> Color.White
                s.equals("black", ignoreCase = true) -> Color.Black
                s.equals("red", ignoreCase = true) -> Color.Red
                s.equals("blue", ignoreCase = true) -> Color.Blue
                s.equals("green", ignoreCase = true) -> Color.Green
                else -> null
            }
        }

        fun parseShape(raw: String?): androidx.compose.ui.graphics.Shape? {
            raw ?: return null
            val s = raw.trim()
            return when {
                s.equals("CircleShape", ignoreCase = true) -> CircleShape
                s.startsWith("RoundedCornerShape", ignoreCase = true) -> {
                    val dp = parseDp(s.substringAfter('(').substringBefore(')')) ?: 0.dp
                    RoundedCornerShape(dp)
                }
                else -> null
            }
        }
    }
}

