package io.noties.markwon.locator

import io.noties.prism4j.GrammarLocator
import io.noties.prism4j.Prism4j
import io.noties.markwon.prism4j.languages.Prism_brainfuck
import io.noties.markwon.prism4j.languages.Prism_c
import io.noties.markwon.prism4j.languages.Prism_clike
import io.noties.markwon.prism4j.languages.Prism_clojure
import io.noties.markwon.prism4j.languages.Prism_cpp
import io.noties.markwon.prism4j.languages.Prism_csharp
import io.noties.markwon.prism4j.languages.Prism_css
import io.noties.markwon.prism4j.languages.Prism_css_extras
import io.noties.markwon.prism4j.languages.Prism_dart
import io.noties.markwon.prism4j.languages.Prism_git
import io.noties.markwon.prism4j.languages.Prism_go
import io.noties.markwon.prism4j.languages.Prism_groovy
import io.noties.markwon.prism4j.languages.Prism_html
import io.noties.markwon.prism4j.languages.Prism_java
import io.noties.markwon.prism4j.languages.Prism_javascript
import io.noties.markwon.prism4j.languages.Prism_json
import io.noties.markwon.prism4j.languages.Prism_kotlin
import io.noties.markwon.prism4j.languages.Prism_latex
import io.noties.markwon.prism4j.languages.Prism_makefile
import io.noties.markwon.prism4j.languages.Prism_markdown
import io.noties.markwon.prism4j.languages.Prism_markup
import io.noties.markwon.prism4j.languages.Prism_python
import io.noties.markwon.prism4j.languages.Prism_scala
import io.noties.markwon.prism4j.languages.Prism_sql
import io.noties.markwon.prism4j.languages.Prism_swift
import io.noties.markwon.prism4j.languages.Prism_yaml

class PrismGrammarLocator : GrammarLocator {
    override fun grammar(
        prism4j: Prism4j,
        language: String,
    ): Prism4j.Grammar? {
        return when (language) {
            "html" -> Prism_html.create(prism4j)
            "brainfuck" -> Prism_brainfuck.create(prism4j)
            "c" -> Prism_c.create(prism4j)
            "clike" -> Prism_clike.create(prism4j)
            "clojure" -> Prism_clojure.create(prism4j)
            "cpp" -> Prism_cpp.create(prism4j)
            "csharp" -> Prism_csharp.create(prism4j)
            "css" -> Prism_css.create(prism4j)
            "css_extras" -> Prism_css_extras.create(prism4j)
            "dart" -> Prism_dart.create(prism4j)
            "git" -> Prism_git.create(prism4j)
            "go" -> Prism_go.create(prism4j)
            "groovy" -> Prism_groovy.create(prism4j)
            "java" -> Prism_java.create(prism4j)
            "javascript" -> Prism_javascript.create(prism4j)
            "json" -> Prism_json.create(prism4j)
            "kotlin" -> Prism_kotlin.create(prism4j)
            "latex" -> Prism_latex.create(prism4j)
            "makefile" -> Prism_makefile.create(prism4j)
            "markdown" -> Prism_markdown.create(prism4j)
            "markup" -> Prism_markup.create(prism4j)
            "python" -> Prism_python.create(prism4j)
            "scala" -> Prism_scala.create(prism4j)
            "sql" -> Prism_sql.create(prism4j)
            "swift" -> Prism_swift.create(prism4j)
            "yaml" -> Prism_yaml.create(prism4j)
            else -> return null
        }
    }

    override fun languages(): MutableSet<String> =
        mutableSetOf(
            "html",
            "brainfuck",
            "c",
            "clike",
            "clojure",
            "cpp",
            "csharp",
            "css",
            "css",
            "dart",
            "git",
            "go",
            "groovy",
            "java",
            "javascript",
            "json",
            "kotlin",
            "latex",
            "makefile",
            "markdown",
            "markup",
            "python",
            "scala",
            "sql",
            "swift",
            "yaml",
        )
}