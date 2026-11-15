package com.hrm.markdown.renderer.internal

import com.hrm.markdown.renderer.MarkdownRenderOptions
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry

internal data class RendererFacadeState(
    val theme: ResolvedMarkdownStyle,
    val config: MarkdownRenderOptions,
    val directiveRegistry: MarkdownDirectiveRegistry,
    val isStreaming: Boolean,
)
