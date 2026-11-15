package com.hrm.markdown.renderer.block

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hrm.markdown.renderer.LocalMarkdownStyle

/**
 * 水平分割线渲染器 (---, ***, ___)
 */
@Composable
internal fun ThematicBreakRenderer(
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = theme.layout.dividerThickness,
        color = theme.colors.divider,
    )
}
