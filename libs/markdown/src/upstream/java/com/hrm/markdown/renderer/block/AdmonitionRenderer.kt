package com.hrm.markdown.renderer.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrm.markdown.parser.ast.Admonition
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.MarkdownBlockChildren
import com.hrm.markdown.renderer.internal.core.model.AdmonitionBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens

/**
 * Admonition 渲染器 (> [!NOTE], > [!WARNING] 等)。
 */
@Composable
internal fun AdmonitionRenderer(
    node: Admonition,
    modifier: Modifier = Modifier,
) {
    RenderAdmonitionContainer(
        type = node.type,
        title = node.title,
        modifier = modifier,
    ) {
        if (node.children.isNotEmpty()) {
            MarkdownBlockChildren(
                parent = node,
                modifier = Modifier.padding(top = LayoutTokens.ContainerContentSpacing),
            )
        }
    }
}

@Composable
internal fun RenderAdmonitionBlockModel(
    model: AdmonitionBlockModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    RenderAdmonitionContainer(
        type = model.type,
        title = model.title,
        modifier = modifier,
        content = {
            if (model.children.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = LayoutTokens.ContainerContentSpacing)) {
                    content()
                }
            } else {
                content()
            }
        },
    )
}

@Composable
private fun RenderAdmonitionContainer(
    type: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalMarkdownStyle.current
    val style = theme.colors.admonitions[type.uppercase()]
        ?: theme.colors.admonitions["NOTE"]
        ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(style.backgroundColor)
            .drawBehind {
                drawLine(
                    color = style.borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx(),
                )
            }
            .padding(
                start = LayoutTokens.ContainerStartPadding,
                top = LayoutTokens.ContainerVerticalPadding,
                end = LayoutTokens.ContainerEndPadding,
                bottom = LayoutTokens.ContainerVerticalPadding,
            ),
    ) {
        // 标题行
        Row {
            Text(
                text = LocalMarkdownComponents.current.admonitionIcons[type.uppercase()] ?: "ℹ️",
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = title.ifEmpty { type.uppercase() },
                style = theme.bodyStyle.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = style.titleColor,
                ),
            )
        }

        content()
    }
}
