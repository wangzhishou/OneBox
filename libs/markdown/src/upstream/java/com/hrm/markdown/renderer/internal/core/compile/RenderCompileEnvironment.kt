package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.renderer.MarkdownSoftBreakPolicy

/** 仅包含构建不可变渲染模型时实际使用的语义选项。 */
data class RenderCompileEnvironment(
    val enableHeadingNumbering: Boolean = false,
    val softBreakPolicy: MarkdownSoftBreakPolicy = MarkdownSoftBreakPolicy.Space,
)
