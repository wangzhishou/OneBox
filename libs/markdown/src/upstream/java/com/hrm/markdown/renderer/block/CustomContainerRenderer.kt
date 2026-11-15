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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrm.markdown.parser.ast.CustomContainer
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.MarkdownBlockChildren
import com.hrm.markdown.renderer.internal.core.model.CustomContainerBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens

/**
 * 自定义容器渲染器 (::: type ... :::)。
 *
 * 如果容器类型匹配已知的 Admonition 样式（NOTE/TIP/WARNING 等），
 * 则使用 Admonition 风格渲染；否则使用通用容器样式。
 */
@Composable
internal fun CustomContainerRenderer(
    node: CustomContainer,
    modifier: Modifier = Modifier,
) {
    RenderCustomContainerContainer(
        type = node.type,
        title = node.title,
        modifier = modifier,
    ) {
        if (node.children.isNotEmpty()) {
            MarkdownBlockChildren(
                parent = node,
                modifier = Modifier.padding(
                    top = if (node.title.isNotEmpty() || node.type.isNotEmpty()) {
                        LayoutTokens.ContainerContentSpacing
                    } else {
                        0.dp
                    }
                ),
            )
        }
    }
}

@Composable
internal fun RenderCustomContainerBlockModel(
    model: CustomContainerBlockModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    RenderCustomContainerContainer(
        type = model.type,
        title = model.title,
        modifier = modifier,
        content = {
            if (model.children.isNotEmpty() && (model.title.isNotEmpty() || model.type.isNotEmpty())) {
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
private fun RenderCustomContainerContainer(
    type: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalMarkdownStyle.current

    // 尝试匹配 Admonition 样式
    val admonitionStyle = theme.colors.admonitions[type.uppercase()]

    val borderColor = admonitionStyle?.borderColor ?: theme.colors.blockQuoteBorder
    val backgroundColor = admonitionStyle?.backgroundColor ?: theme.colors.containerBackground
    val iconText = LocalMarkdownComponents.current.admonitionIcons[type.uppercase()] ?: "📦"
    val titleColor = admonitionStyle?.titleColor ?: theme.colors.text

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .drawBehind {
                drawLine(
                    color = borderColor,
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
        // 标题行（如果有类型名或标题）
        val displayTitle = title.ifEmpty {
            type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        if (displayTitle.isNotEmpty()) {
            Row {
                Text(
                    text = iconText,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = displayTitle,
                    style = theme.bodyStyle.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                    ),
                )
            }
        }

        // 内容
        content()
    }
}
