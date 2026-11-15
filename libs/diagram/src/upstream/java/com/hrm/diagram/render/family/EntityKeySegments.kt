package com.hrm.diagram.render.family

internal fun normalizedEntityPrefix(value: String): String =
    stableSegment(value).ifBlank { "diagram" }

internal fun stableSegment(value: String): String =
    value
        .trim()
        .lowercase()
        .map { ch ->
            when {
                ch.isLetterOrDigit() -> ch
                ch == '.' || ch == '-' || ch == '_' -> ch
                else -> '_'
            }
        }
        .joinToString("")
        .trim('.', '-', '_')
