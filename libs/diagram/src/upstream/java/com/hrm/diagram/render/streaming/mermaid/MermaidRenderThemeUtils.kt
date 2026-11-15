package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.color.ColorTokenParser

internal object MermaidRenderThemeUtils {
    fun decodeRawThemeTokens(encoded: String?): Map<String, String> {
        if (encoded.isNullOrBlank()) return emptyMap()
        val out = LinkedHashMap<String, String>()
        for (line in encoded.lines()) {
            val idx = line.indexOf('=')
            if (idx <= 0) continue
            val key = line.substring(0, idx)
            val value = line.substring(idx + 1)
            if (key.startsWith("raw.")) out[key.removePrefix("raw.")] = value
        }
        return out
    }

    fun parseThemeColor(text: String?) = ColorTokenParser.parseColor(text)
}
