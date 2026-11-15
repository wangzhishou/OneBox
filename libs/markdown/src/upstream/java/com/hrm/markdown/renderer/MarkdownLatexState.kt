package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.latex.renderer.measure.rememberLatexMeasurer
import com.hrm.latex.renderer.model.LatexConfig

/** 配置和测量器一起创建，供文档布局、行内内容与公式块共用。 */
internal class MarkdownLatexState(
    val config: LatexConfig,
    val measurer: LatexMeasurerState,
)

internal val LocalMarkdownLatexState = compositionLocalOf<MarkdownLatexState> {
    error("MarkdownLatexState must be provided by MarkdownDocumentRenderer")
}

/** 字体解析完全交给 LaTeX：显式配置优先，否则自动加载可选 KaTeX 资源，失败时回退系统字体。 */
@Composable
internal fun rememberMarkdownLatexState(config: LatexConfig): MarkdownLatexState {
    val measurer = rememberLatexMeasurer(config)
    return remember(config, measurer) { MarkdownLatexState(config, measurer) }
}
