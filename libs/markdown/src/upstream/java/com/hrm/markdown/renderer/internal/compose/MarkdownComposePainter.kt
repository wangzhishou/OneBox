package com.hrm.markdown.renderer.internal.compose

import androidx.compose.runtime.Composable
import com.hrm.markdown.renderer.internal.layout.engine.MarkdownLayoutSource

internal interface MarkdownComposePainter {
    @Composable
    fun Paint(
        document: MarkdownLayoutSource,
        environment: ComposeRenderEnvironment,
    )
}
