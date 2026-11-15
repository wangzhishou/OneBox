package com.wanbaohe.markdown

import android.util.Base64

class OneBoxMarkdownCodeFenceTransformer(
    override val id: String,
    private val directiveName: String,
    private val languages: Set<String>,
) : OneBoxMarkdownInputTransformer {
    override fun transform(input: String): String = FENCE_REGEX.replace(input) { match ->
        val language = match.groupValues[2].lowercase()
        if (language !in languages) return@replace match.value
        val payload = Base64.encodeToString(
            match.groupValues[3].toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP,
        )
        "\n{% $directiveName payload=\"$payload\" %}\n"
    }

    private companion object {
        val FENCE_REGEX = Regex(
            pattern = """^[ \t]{0,3}(`{3,}|~{3,})[ \t]*([A-Za-z][\w+-]*)[^\r\n]*\r?\n(.*?)(?:\r?\n^[ \t]{0,3}\1[ \t]*(?=\r?$)|\z)""",
            options = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL),
        )
    }
}
