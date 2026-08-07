package io.noties.markwon.plugins.codeblock

import android.graphics.Bitmap
import java.io.File

interface CodeBlockClickListener {
    // ...existing code...
    var isHighlighted: Boolean

    fun onWidgetButtonClicked(language: String, code: String): Boolean

    /**
     * Called when the copy button in a code block is clicked
     * @param code The code content to be copied
     * @return true if the event was handled
     */
    fun onCopyButtonClicked(code: String): Boolean

    /**
     * Called when the run button in a code block is clicked
     * @param language The programming language of the code
     * @param code The code content to be executed
     * @return true if the event was handled
     */
    fun onRunButtonClicked(language: String, code: String): Boolean

    /**
     * Determines if a language is supported for execution
     * @param language The programming language to check
     * @return true if the language can be executed
     */
    fun isLanguageRunnable(language: String): Boolean = language.lowercase() in setOf(
        "html"
    )

    /**
     * Determines if the language is Mermaid diagram
     */
    fun isMermaid(language: String): Boolean = language.lowercase() == "mermaid"

    /**
     * Determines if a language is safe to save
     */
    fun isLanguageSave(language: String): Boolean = language.lowercase() in setOf(
        "html",
        "js", "javascript",
        "c",
        "cpp", "c++",
        "csharp", "c#", "cs",
        "css",
        "dart",
        "go", "golang",
        "groovy",
        "java",
        "kotlin", "kt",
        "latex", "tex",
        "makefile",
        "markdown", "md",
        "python", "py",
        "scala",
        "sql",
        "swift",
        "yaml", "yml",
        "clojure", "clj",
        "json",
        "xml",
        "shell", "sh", "bash", "zsh",
        "ruby", "rb",
        "rust", "rs",
        "php",
        "typescript", "ts",
        "lua",
        "perl",
        "r",
        "objectivec", "objc",
        "brainfuck"
    )

    /**
     * Called when the save button in a code block is clicked
     * @param language The programming language of the code
     * @param code The code content to be saved
     * @return true if the event was handled
     */
    fun onSaveButtonClicked(language: String, code: String): Boolean

    /**
     * Called when the Mermaid diagram save button is clicked.
     * Captures the rendered diagram as a bitmap and saves it.
     *
     * @param code   Mermaid source code
     * @param bitmap The rendered diagram bitmap
     * @return true if the event was handled
     */
    fun onMermaidSaveClicked(code: String, bitmap: Bitmap): Boolean = false

    /**
     * Called when the Mermaid diagram fullscreen button is clicked.
     * Captures the rendered diagram as a bitmap and opens it in image viewer.
     *
     * @param code   Mermaid source code
     * @param bitmap The rendered diagram bitmap
     * @return true if the event was handled
     */
    fun onMermaidFullscreenClicked(code: String, bitmap: Bitmap): Boolean = false

    /**
     * Called when the Mermaid diagram save button is clicked (file-based).
     * Directly provides the cached SVG file, avoiding SVG→Bitmap conversion.
     *
     * @param code Mermaid source code
     * @param file The cached SVG file
     * @return true if the event was handled
     */
    fun onMermaidSaveFile(code: String, file: File): Boolean = false

    /**
     * Called when the Mermaid diagram fullscreen button is clicked (file-based).
     * Directly provides the cached SVG file for viewing in image viewer.
     *
     * @param code Mermaid source code
     * @param file The cached SVG file
     * @return true if the event was handled
     */
    fun onMermaidFullscreenFile(code: String, file: File): Boolean = false

    /**
     * A2UI 表单提交回调。
     * 当 a2ui 代码块中的表单用户点击提交时调用。
     *
     * @param formData PromptCollector 收集的表单数据文本
     * @return true 表示已处理
     */
    fun onA2uiSubmit(formData: String): Boolean = false
}