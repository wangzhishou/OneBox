package com.wanbaohe.habittracker.ai.tool

/** 习惯工具 markdown 输出的小工具(与 feature/ai 内部实现解耦,避免跨模块依赖) */
internal fun buildMarkdownLink(label: String, deeplink: String): String = "[$label]($deeplink)"

/** 去掉 markdown 特殊字符,防止用户输入破坏结果格式 */
internal fun sanitizeMarkdownText(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("\n", " ")
}
