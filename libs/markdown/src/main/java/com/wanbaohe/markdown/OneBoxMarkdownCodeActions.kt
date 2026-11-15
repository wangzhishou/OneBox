package com.wanbaohe.markdown

interface OneBoxMarkdownCodeActions {
    fun copy(code: String)
    fun createWidget(language: String, code: String)
    fun run(language: String, code: String)
    fun save(language: String, code: String)

    fun canRun(language: String): Boolean = language.equals("html", ignoreCase = true)

    fun canSave(language: String): Boolean = language.trim().lowercase() in SAVEABLE_LANGUAGES
}

private val SAVEABLE_LANGUAGES = setOf(
    "html", "js", "javascript", "c", "cpp", "c++", "csharp", "c#", "cs", "css",
    "dart", "go", "golang", "groovy", "java", "kotlin", "kt", "latex", "tex",
    "makefile", "markdown", "md", "python", "py", "scala", "sql", "swift", "yaml",
    "yml", "clojure", "clj", "json", "xml", "shell", "sh", "bash", "zsh", "ruby",
    "rb", "rust", "rs", "php", "typescript", "ts", "lua", "perl", "r", "objectivec",
    "objc", "brainfuck",
)
