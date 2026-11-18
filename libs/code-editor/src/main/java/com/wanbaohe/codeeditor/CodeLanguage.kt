/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.codeeditor

/**
 * 代码编辑器支持的语言。
 *
 * [lexerKey] 对应 codehighlight-parser 的 LanguageRegistry 语言键（未知键会回退成纯文本词法器），
 * [key] 是 OneBox 内部语言标识，沿用 [com.t8rin.imagetoolbox.core.domain.model.MimeType.fromLanguage]
 * 认得的名字，另存为时才能生成正确的扩展名。
 *
 * [lineComment] / [blockCommentStart]/[blockCommentEnd] 只服务于「注释选中行」这一条命令；
 * 没有行注释的语言（html/xml/css）走块注释包裹。
 */
@Suppress("LongParameterList") // 语言表：每行就是一个语言的完整描述，拆开反而更难读
enum class CodeLanguage(
    val key: String,
    val lexerKey: String,
    val displayName: String,
    val extensions: List<String>,
    val lineComment: String? = null,
    val blockCommentStart: String? = null,
    val blockCommentEnd: String? = null,
) {
    Kotlin(
        key = "kotlin",
        lexerKey = "kotlin",
        displayName = "Kotlin",
        extensions = listOf("kt", "kts", "gradle"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Java(
        key = "java",
        lexerKey = "java",
        displayName = "Java",
        extensions = listOf("java"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    JavaScript(
        key = "javascript",
        lexerKey = "javascript",
        displayName = "JavaScript",
        extensions = listOf("js", "jsx", "mjs", "cjs"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    TypeScript(
        key = "typescript",
        lexerKey = "typescript",
        displayName = "TypeScript",
        extensions = listOf("ts", "tsx"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Python(
        key = "python",
        lexerKey = "python",
        displayName = "Python",
        extensions = listOf("py"),
        lineComment = "#",
    ),
    Go(
        key = "go",
        lexerKey = "go",
        displayName = "Go",
        extensions = listOf("go"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Rust(
        key = "rust",
        lexerKey = "rust",
        displayName = "Rust",
        extensions = listOf("rs"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    C(
        key = "c",
        lexerKey = "c",
        displayName = "C",
        extensions = listOf("c", "h"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Cpp(
        key = "cpp",
        lexerKey = "cpp",
        displayName = "C++",
        extensions = listOf("cpp", "cc", "cxx", "hpp", "hh"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Swift(
        key = "swift",
        lexerKey = "swift",
        displayName = "Swift",
        extensions = listOf("swift"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Dart(
        key = "dart",
        lexerKey = "dart",
        displayName = "Dart",
        extensions = listOf("dart"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Scala(
        key = "scala",
        lexerKey = "scala",
        displayName = "Scala",
        extensions = listOf("scala", "sc"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Ruby(
        key = "ruby",
        lexerKey = "ruby",
        displayName = "Ruby",
        extensions = listOf("rb"),
        lineComment = "#",
    ),
    Php(
        key = "php",
        lexerKey = "php",
        displayName = "PHP",
        extensions = listOf("php", "phtml"),
        lineComment = "//",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Lua(
        key = "lua",
        lexerKey = "lua",
        displayName = "Lua",
        extensions = listOf("lua"),
        lineComment = "--",
        blockCommentStart = "--[[",
        blockCommentEnd = "]]",
    ),
    Haskell(
        key = "haskell",
        lexerKey = "haskell",
        displayName = "Haskell",
        extensions = listOf("hs"),
        lineComment = "--",
        blockCommentStart = "{-",
        blockCommentEnd = "-}",
    ),
    Elixir(
        key = "elixir",
        lexerKey = "elixir",
        displayName = "Elixir",
        extensions = listOf("ex", "exs"),
        lineComment = "#",
    ),
    R(
        key = "r",
        lexerKey = "rscript",
        displayName = "R",
        extensions = listOf("r"),
        lineComment = "#",
    ),
    Json(
        key = "json",
        lexerKey = "json",
        displayName = "JSON",
        extensions = listOf("json"),
    ),
    Toml(
        key = "toml",
        lexerKey = "toml",
        displayName = "TOML",
        extensions = listOf("toml"),
        lineComment = "#",
    ),
    Yaml(
        key = "yaml",
        lexerKey = "yaml",
        displayName = "YAML",
        extensions = listOf("yaml", "yml"),
        lineComment = "#",
    ),
    Xml(
        key = "xml",
        lexerKey = "xml",
        displayName = "XML",
        extensions = listOf("xml", "svg", "xhtml"),
        blockCommentStart = "<!--",
        blockCommentEnd = "-->",
    ),
    Html(
        key = "html",
        lexerKey = "html",
        displayName = "HTML",
        extensions = listOf("html", "htm", "xhtml"),
        blockCommentStart = "<!--",
        blockCommentEnd = "-->",
    ),
    Css(
        key = "css",
        lexerKey = "css",
        displayName = "CSS",
        extensions = listOf("css", "scss", "less"),
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Sql(
        key = "sql",
        lexerKey = "sql",
        displayName = "SQL",
        extensions = listOf("sql"),
        lineComment = "--",
        blockCommentStart = "/*",
        blockCommentEnd = "*/",
    ),
    Bash(
        key = "shell",
        lexerKey = "bash",
        displayName = "Shell",
        extensions = listOf("sh", "bash", "zsh"),
        lineComment = "#",
    ),
    Dockerfile(
        key = "dockerfile",
        lexerKey = "dockerfile",
        displayName = "Dockerfile",
        extensions = listOf("dockerfile"),
        lineComment = "#",
    ),
    Diff(
        key = "diff",
        lexerKey = "diff",
        displayName = "Diff",
        extensions = listOf("diff", "patch"),
    ),
    Markdown(
        key = "markdown",
        lexerKey = "plaintext",
        displayName = "Markdown",
        extensions = listOf("md", "markdown", "mdown"),
        blockCommentStart = "<!--",
        blockCommentEnd = "-->",
    ),
    PlainText(
        key = "plaintext",
        lexerKey = "plaintext",
        displayName = "Plain Text",
        extensions = listOf("txt", "log", "ini", "conf", "cfg", "properties", "csv", "gitignore", "editorconfig"),
    ),
    ;

    /** 工具坞收起态只露 2 个字符，给每个语言一个可辨识的短标记。 */
    val shortLabel: String
        get() = when (this) {
            Kotlin -> "KT"
            Java -> "JA"
            JavaScript -> "JS"
            TypeScript -> "TS"
            Python -> "PY"
            Go -> "GO"
            Rust -> "RS"
            C -> "C"
            Cpp -> "C+"
            Swift -> "SW"
            Dart -> "DA"
            Scala -> "SC"
            Ruby -> "RB"
            Php -> "PH"
            Lua -> "LU"
            Haskell -> "HS"
            Elixir -> "EX"
            R -> "R"
            Json -> "{}"
            Toml -> "TM"
            Yaml -> "YM"
            Xml -> "<>"
            Html -> "H>"
            Css -> "#"
            Sql -> "SQ"
            Bash -> "$"
            Dockerfile -> "DK"
            Diff -> "±"
            Markdown -> "MD"
            PlainText -> "T"
        }

    companion object {
        /** 与旧 WebView 版 LanguageDetector 的行为保持一致：按扩展名判定，认不出就是纯文本。 */
        fun fromFileName(name: String?): CodeLanguage {
            val fileName = name?.takeIf { it.isNotBlank() }?.substringAfterLast('/') ?: return PlainText
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return if (extension.isEmpty()) {
                // Dockerfile 这类无扩展名的文件名
                if (fileName.lowercase() == "dockerfile") Dockerfile else PlainText
            } else {
                entries.firstOrNull { extension in it.extensions } ?: PlainText
            }
        }

        fun fromKey(key: String?): CodeLanguage =
            entries.firstOrNull { it.key == key } ?: PlainText
    }
}
