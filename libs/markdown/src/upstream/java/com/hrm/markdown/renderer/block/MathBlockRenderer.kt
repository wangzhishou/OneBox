package com.hrm.markdown.renderer.block

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.hrm.latex.renderer.Latex
import com.hrm.markdown.renderer.LocalMarkdownLatexState
import com.hrm.markdown.renderer.LocalMarkdownStyle

/**
 * 数学公式块渲染器 ($$...$$)
 * 使用 LaTeX 库渲染数学公式，通过 LatexMeasurer 预测量精确尺寸，避免多余空白。
 *
 * 公式编号（`\tag{N}`）、环境自动编号（equation/align 等）、引用（`\ref`/`\eqref`）
 * 均由 LaTeX 渲染库原生处理，无需额外的编号展示逻辑。
 */
@Composable
internal fun MathBlockRenderer(
    latex: String,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    val trimmedLatex = latex.trim()
    val latexState = LocalMarkdownLatexState.current

    // 使用 LatexMeasurer 精确测量公式高度，避免容器产生多余空白
    val density = LocalDensity.current
    val dims = latexState.measurer.measure(trimmedLatex)

    val heightModifier = if (dims != null) {
        val heightDp = with(density) { dims.heightPx.toDp() }
        Modifier.height(heightDp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(theme.layout.blockPadding),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.widthIn(min = maxWidth),
                contentAlignment = Alignment.Center,
            ) {
                Latex(
                    latex = trimmedLatex,
                    modifier = heightModifier,
                    config = latexState.config,
                )
            }
        }
    }
}
