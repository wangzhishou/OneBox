package com.hrm.markdown.renderer

import androidx.compose.runtime.compositionLocalOf
import com.hrm.markdown.runtime.MarkdownSourceMap

/** Source mapping for the transformed Markdown currently being rendered. */
val LocalMarkdownSourceMap = compositionLocalOf<MarkdownSourceMap> {
    MarkdownSourceMap.Identity
}
