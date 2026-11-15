package com.wanbaohe.markdown.internal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hrm.codehigh.renderer.InlineCode
import com.hrm.markdown.renderer.MarkdownInlineCodeInfo
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.markdown.LocalOneBoxMarkdownCodeStyle

@Composable
internal fun OneBoxMarkdownBlockSurface(
    content: @Composable () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
        color = LocalOneBoxMarkdownCodeStyle.current.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        content()
    }
}

@Composable
internal fun OneBoxMarkdownInlineCode(info: MarkdownInlineCodeInfo) {
    GlassSurface(
        style = GlassStyle.Regular,
        color = LocalOneBoxMarkdownCodeStyle.current.surface,
        shape = info.layout.inlineShape,
    ) {
        InlineCode(
            text = info.text,
            colors = info.colors,
            typography = info.typography,
            layout = info.layout,
        )
    }
}
