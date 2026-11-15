package com.wanbaohe.markdown

private val SpecializedCodeBlockRegex = Regex(
    """(?m)^[ \t]*(?:`{3,}|~{3,})[ \t]*([A-Za-z0-9_+-]+)"""
)
private val LegacyCodeBlockLanguages = setOf(
    "a2ui", "uijson", "mermaid", "mmd", "plantuml", "puml", "uml", "dot", "gv", "graphviz",
)

fun requiresLegacyMarkdown(markdown: String): Boolean =
    SpecializedCodeBlockRegex.findAll(markdown).any { match ->
        match.groupValues[1].lowercase() in LegacyCodeBlockLanguages
    }
