/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.style

data class RichTextMarkdownInlineRule(
    val order: Int,
    val exclusive: Boolean = false,
    val wrap: (String) -> String,
    val prefix: String? = null,
    val suffix: String? = null,
) {
    companion object {
        fun delimited(
            prefix: String,
            suffix: String = prefix,
            order: Int,
        ) = RichTextMarkdownInlineRule(
            order = order,
            prefix = prefix,
            suffix = suffix,
            wrap = { text -> prefix + text + suffix },
        )

        fun inlineCode(order: Int = 100) = RichTextMarkdownInlineRule(
            order = order,
            exclusive = true,
            wrap = { text ->
                val longestRun = Regex("`+").findAll(text).maxOfOrNull { it.value.length } ?: 0
                val delimiter = "`".repeat(longestRun + 1)
                val padding = if (text.startsWith('`') || text.endsWith('`')) " " else ""
                delimiter + padding + text + padding + delimiter
            },
        )
    }
}
